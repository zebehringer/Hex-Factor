package catan.impl;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

import catan.Board;
import catan.BuildListener;
import catan.Catan;
import catan.Catan.Product;
import catan.Player;
import catan.PlayerInfo;
import catan.PlayerListener;
import catan.ProductBuildEvent;
import catan.ResourcesEarnedEvent;
import catan.ResourcesExchangedEvent;
import catan.ResourcesTakenEvent;
import catan.Road;
import catan.RobberOutcome;
import catan.RollOutcome;
import catan.Site;
import catan.State;
import catan.StateChangeEvent;
import catan.StateChangeListener;
import catan.impl.card.Knight;
import catan.impl.card.VictoryPointCard;


public class Game implements Catan.Game<SiteImpl,BoardImpl>, Serializable {

	private GameParams params;
	private PlayerImpl[] players;
	private Turn turn;
	private BoardImpl board;
	private State currentState;
	private Queue<DevelopmentCard> devCards;
	private HashMap<Integer,DevelopmentCard> cardTypes;
	private List<BuildListener<Game>> buildListeners;
	private List<StateChangeListener<Game>> stateChangeListeners;
	private List<PlayerListener<Game>> playerListeners;
	private List<Player> unreadyPlayers;

	private static abstract class ProductBuilder {
		@Override
		public String toString() {
			return getProduct().name();
		}
		public abstract Product getProduct();
		public abstract void apply(Game game, PlayerImpl player, SiteImpl[] sites) throws IllegalStateException, IllegalArgumentException;
	}

	private static class RoadBuilder extends ProductBuilder {
		@Override
		public Product getProduct() {
			return Product.ROAD;
		}
		@Override
		public void apply(Game game, PlayerImpl player, SiteImpl[] sites) throws IllegalArgumentException {
			validateRoad(game, player, sites);
			validateConnectingRoad(game, player, sites);
			player.subtractResources(Catan.ResourceType.Brick,1);
			player.subtractResources(Catan.ResourceType.Wood, 1);
			buildRoad(game, player, sites);
		}
		public static void validateRoad(Game game, PlayerInfo player, Site[] sites) {
			if (sites == null || sites.length < 2) {
				throw new IllegalArgumentException("No edge specified");
			}
			//find a tile with this edge
			Board.Tile tile = game.getBoard().getTile(sites);
			if (tile == null) {
				throw new IllegalArgumentException("There is no edge that connects the two specified sites");
			}
			//there cannot already be a road on this edge
			for (PlayerInfo p : game.getPlayers()) {
				for (Road road : p.getRoads()) {
					if (road.contains(sites[0],sites[1])) {
						throw new IllegalArgumentException("There is already a road on the specified edge");
					}
				}
			}
		}
		public static void validateConnectingRoad(Game game, PlayerInfo player, SiteImpl[] sites) throws IllegalArgumentException {
			//the active player must have a building or a road at one end of the specified edge
			boolean found = false;
			for (Road road : player.getRoads()) {
				if (road.connectsTo(sites[0],sites[1])) {
					found = true;
				}
			}
			if (!found) {
				for (Site site : game.getBoard().getSites(player)) {
					if (site == sites[0] || site == sites[1]) {
						found = true;
					}
				}
			}
			if (!found) {
				throw new IllegalArgumentException("The proposed road does not connect to an existing road or building");
			}
		}
		public static void buildRoad(Game game, PlayerImpl player, SiteImpl[] sites) throws IllegalArgumentException {
			player.addRoad(sites[0], sites[1]);
		}
	}
	
	private static class SettlementBuilder extends ProductBuilder {
		@Override
		public Product getProduct() {
			return Product.SETTLEMENT;
		}
		public void apply(Game game, PlayerImpl player, SiteImpl[] sites) throws IllegalArgumentException {
			validateSettlement(game, player, sites);
			validateConnectingRoad(game, player, sites[0]);
			player.subtractResources(Catan.ResourceType.Brick,1);
			player.subtractResources(Catan.ResourceType.Wood, 1);
			player.subtractResources(Catan.ResourceType.Sheep,1);
			player.subtractResources(Catan.ResourceType.Wheat, 1);
			buildSettlement(game, player, sites);
		}
		public static void validateSettlement(Game game, PlayerInfo player, Site[] sites) throws IllegalArgumentException {
			if (sites == null || sites.length < 1) {
				throw new IllegalArgumentException("No site specified");
			}
			//there must not be any building currently on the site
			if (sites[0].getBuilding() != null) {
				throw new IllegalArgumentException("Site already has a settlement");
			}
			for (Site site : game.getBoard().getConnectedSites(sites[0])) {
				if (site.getBuilding() != null) {
					throw new IllegalArgumentException("Proposed building too close to another building");
				}
			}
		}
		public static void validateConnectingRoad(Game game, PlayerInfo player, Site buildingSite) throws IllegalArgumentException {
			for (Road road : player.getRoads()) {
				for (Site site : road.getSites()) {
					if (site == buildingSite) {
						return;
					}
				}
			}
			throw new IllegalArgumentException("Proposed building does not lie on any of the player's roads");
		}
		public static void buildSettlement(Game game, PlayerInfo player, SiteImpl[] sites) throws IllegalArgumentException {
			sites[0].setBuilding(new BuildingImpl(Catan.SETTLEMENT, player));
		}
	}
	
	private static class CityBuilder extends ProductBuilder {
		@Override
		public Product getProduct() {
			return Product.CITY;
		}
		public void apply(Game game, PlayerImpl player, SiteImpl[] sites) throws IllegalArgumentException {
			if (sites == null || sites.length < 1) {
				throw new IllegalArgumentException("No site specified");
			}
			//active player must have a settlement on specified site
			for (Site site : game.getBoard().getSites(player)) {
				if (site == sites[0]) {
					if (site.getBuilding() != null && site.getBuilding().getType() == Catan.CITY) {
						throw new IllegalArgumentException("There is already a city on the specified site");
					}
					player.subtractResources(Catan.ResourceType.Wheat, 2);
					player.subtractResources(Catan.ResourceType.Ore, 3);
					sites[0].setBuilding(new BuildingImpl(Catan.CITY, player));
					return;
				}
			}
			throw new IllegalArgumentException("There is no settlement on the specified site that can be upgraded");
		}
	}
	
	private static class DevCardBuilder extends ProductBuilder {
		@Override
		public Product getProduct() {
			return Product.DEV_CARD;
		}
		public void apply(Game game, PlayerImpl player, SiteImpl[] sites) throws IllegalArgumentException {
			player.subtractResources(Catan.ResourceType.Sheep,1);
			player.subtractResources(Catan.ResourceType.Wheat, 1);
			player.subtractResources(Catan.ResourceType.Ore, 1);
			DevelopmentCard card = game.drawDevelopmentCard();
			player.addDevelopmentCard(card);
		}
	}
	
	public Game(PlayerImpl[] players, GameParams params) {
		this.players = players;
		this.params = params;
		//int start = (int)Math.random()*players.length;
		/*if (players.length > 4) {
			board = new LargeBoard();
		}
		else {*/
			board = SmallBoard.create();
		//}
		List<DevelopmentCard> availDevCards = new ArrayList<DevelopmentCard>();
		addDevCard(availDevCards, Knight.class, 20);
		addDevCard(availDevCards, VictoryPointCard.class, 4);
		Collections.shuffle(availDevCards);
		devCards = new LinkedList<DevelopmentCard>(availDevCards);
		cardTypes = new HashMap<Integer,DevelopmentCard>();
		cardTypes.put(1, new Knight());
		cardTypes.put(2, new VictoryPointCard());
		buildListeners = new ArrayList<BuildListener<Game>>();
		stateChangeListeners = new ArrayList<StateChangeListener<Game>>();
		playerListeners = new ArrayList<PlayerListener<Game>>();
		unreadyPlayers = new ArrayList<Player>();
		for (PlayerImpl player : players) {
			unreadyPlayers.add(player);
		}
	}
	
	protected void start() {
		setState(new SetupState(players[0], 1), null);
	}
	
	public void playerReady(Player player) {
		if (unreadyPlayers != null) {
			for (Iterator<Player> it = unreadyPlayers.iterator(); it.hasNext(); ) {
				if (it.next().equals(player)) {
					it.remove();
					break;
				}
			}
			if (unreadyPlayers.isEmpty()) {
				unreadyPlayers = null;
				start();
			}
		}
	}
	
	public DevelopmentCard findDevelopmentCard(int id) {
		return cardTypes.get(id);
	}
	
	private void addDevCard(List<DevelopmentCard> availDevCards, Class<?> c, int count) {
		try {
			DevelopmentCard card = (DevelopmentCard)c.newInstance();
			for (int i=0; i<count; i++) {
				availDevCards.add(card);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void validateCurrentState(State state) throws IllegalStateException {
		if (state != currentState) {
			throw new IllegalStateException("The current state does not match the requesting state");
		}
	}
	
	public BoardImpl getBoard() {
		return board;
	}
	
	public Iterable<? extends PlayerInfo> getPlayers() {
		return Arrays.asList(players);
	}
	
	private PlayerImpl[] getPlayerImpls() {
		return players;
	}
	
	private PlayerImpl getPlayerImpl(PlayerInfo p) {
		for (PlayerImpl impl : getPlayerImpls()) {
			if (impl.getUniqueId().equals(p.getUniqueId())) {
				return impl;
			}
		}
		return null;
	}
	
	private SetupState getNextSetupState(PlayerImpl current, int direction) {
		int currentIndex = getPlayerIndex(current);
		if (direction > 0) {
			if (currentIndex == players.length-1) {
				return new SetupState(current, -1);
			}
			return new SetupState(players[currentIndex+1], 1);
		}
		else if (currentIndex == 0) {
			return null;
		}
		return new SetupState(players[currentIndex-1], -1);
	}
	
	private int getPlayerIndex(PlayerInfo player) {
		for (int i=0; i<players.length; i++) {
			if (players[i] == player) {
				return i;
			}
		}
		return -1;
	}
	
	public Turn getTurn() {
		return turn;
	}
	
	private void startFirstTurn() {
		Turn nextTurn = new Turn(players[0]);
		turn = nextTurn;
		setState(new WaitingForTurn(turn.getPlayer()), null);
	}
	
	private void startNextTurn(PlayerInfo current) {
		Turn nextTurn = new Turn(players[(getPlayerIndex(current)+1)%players.length]);
		turn = nextTurn;
		setState(new WaitingForTurn(turn.getPlayer()), null);
	}

	private RollOutcome roll(WaitingForTurn state) throws IllegalStateException {
		validateCurrentState(state);
		int d1 = 1+((int)Math.round(Math.random()*5));
		int d2 = 1+((int)Math.round(Math.random()*5));
		int sum = d1+d2;
		if (!params.isRobberUsed()) {
			while (sum == 7) {
				d1 = 1+((int)Math.round(Math.random()*5));
				d2 = 1+((int)Math.round(Math.random()*5));
				sum = d1+d2;
			}
		}
		
		if (sum == 7) {
			RobberState robber = new RobberState(state.getPlayerImpl());
			RollOutcome outcome = new RollOutcome(robber);
			setState(robber, outcome);
			return outcome;
		}
		Map<PlayerInfo,Map<Catan.ResourceType,Integer>> outcome = new LinkedHashMap<PlayerInfo,Map<Catan.ResourceType,Integer>>();
		for (PlayerInfo player : players) {
			outcome.put(player, new TreeMap<Catan.ResourceType,Integer>());
		}
		ArrayList<ResourceEarnedEvent> resourcesEarned = new ArrayList<ResourceEarnedEvent>();
		for (Board.Tile t : board.getTiles()) {
			if (t.getNumber() == sum && !board.isBlocked(t)) {
				for (Site s : t.getSites()) {
					if (s.getBuilding() != null) {
						int count = s.getBuilding().getType().yieldResources(s);
						addResources(outcome.get(s.getBuilding().getOwner()), t.getResourceType(), count);
						((PlayerImpl)s.getBuilding().getOwner()).addResources(t.getResourceType(), count);
						resourcesEarned.add(new ResourceEarnedEvent(s.getBuilding().getOwner(), t.getResourceType(), count, s));
					}
				}
			}
		}
		//remember the outcome
		//change state
		WaitingForYield waitingForYield = new WaitingForYield(state.getPlayerImpl());
		RollOutcome rollOutcome = new RollOutcome(sum, waitingForYield, outcome);
		setState(waitingForYield, rollOutcome);
		for (ResourceEarnedEvent event : resourcesEarned) {
			fireResourcesEarned(event.earner, event.type, event.count, event.source);
		}
		return rollOutcome;
	}
	
	private DevelopmentCard drawDevelopmentCard() {
		return devCards.remove();
	}
	
	protected void playDevCard(catan.DevelopmentCard card) {
		((DevelopmentCard)card).play(this);
	}
	
	public void addPlayerListener(PlayerListener listener) {
		playerListeners.add(listener);
	}

	public void removePlayerListener(PlayerListener listener) {
		playerListeners.remove(listener);
	}

	protected void fireResourceTaken(PlayerInfo taker, PlayerInfo victim, Catan.ResourceType type, int count) {
		ResourcesTakenEvent event = new ResourcesTakenEvent(taker, victim, type, count);
		for (PlayerListener<Game> listener : playerListeners) {
			listener.resourcesTaken(this, event);
		}
	}
	
	private static class ResourceEarnedEvent implements Serializable {
		public PlayerInfo earner;
		public Catan.ResourceType type;
		public int count;
		public Site source;
		public ResourceEarnedEvent(PlayerInfo earner, Catan.ResourceType type, int count, Site source) {
			this.earner = earner;
			this.type = type;
			this.count = count;
			this.source = source;
		}
	};

	protected void fireResourcesEarned(PlayerInfo earner, Catan.ResourceType type, int count, Site source) {
		ResourcesEarnedEvent event = new ResourcesEarnedEvent(earner,type,count,source);
		for (PlayerListener<Game> listener : playerListeners) {
			listener.resourcesEarned(this, event);
		}
	}
	
	protected void fireResourcesExchanged(PlayerInfo player, Catan.ResourceType given, int givenCount, Catan.ResourceType received, int receivedCount) {
		ResourcesExchangedEvent event = new ResourcesExchangedEvent(player, given, givenCount, received, receivedCount);
		for (PlayerListener<Game> listener : playerListeners) {
			listener.resourcesExchanged(this, event);
		}
	}
	
	private static void addResources(Map<Catan.ResourceType,Integer> resources, Catan.ResourceType type, int count) {
		if (resources.containsKey(type)) {
			resources.put(type, resources.get(type)+count);
		}
		else {
			resources.put(type, count);
		}
	}

	private RobberOutcome moveRobber(RobberState state, Board.Tile tile, PlayerInfo victim, State nextState) throws IllegalStateException {
		validateCurrentState(state);
		getBoard().placeRobber(tile);
		try {
			PlayerImpl v = getPlayerImpl(victim);
			Catan.ResourceType resource = v.takeRandomResource();
			if (resource != null) {
				state.getPlayerImpl().addResources(resource, 1);
				fireResourceTaken(state.getPlayer(), victim, resource, 1);
			}
		}
		catch (IllegalStateException ignore) { } //nothing to take
		return new RobberOutcome(nextState,tile,victim);
	}
	
	private void beginTrade(WaitingForYield state) throws IllegalStateException {
		validateCurrentState(state);
		setState(new TradeState(getTurn().getPlayer()), null);
	}
	
	private void bankTrade(Catan.ResourceType in, Catan.ResourceType out) throws IllegalArgumentException {
		PlayerImpl player = getTurn().getPlayer();
		for (Site site : getBoard().getSites(player)) {
			if (site.getTradePort() != null && site.getTradePort().acceptInput(in)) {
				if (player.countResources(in) >= site.getTradePort().getInputCount()) {
					player.subtractResources(in, site.getTradePort().getInputCount());
					player.addResources(out, 1);
					fireResourcesExchanged(player, in, site.getTradePort().getInputCount(), out, 1);
					return;
				}
			}
		}
		if (player.countResources(in) >= 4) {
			player.subtractResources(in,4);
			player.addResources(out, 1);
			fireResourcesExchanged(player, in, 4, out, 1);
			return;
		}
		throw new IllegalStateException("Insufficient resources");
	}
	
	private void endTrade(TradeState state) throws IllegalStateException {
		validateCurrentState(state);
		setState(new WaitingForYield(getTurn().getPlayer()), null);
	}
	
	public void addProductBuildListener(BuildListener listener) {
		buildListeners.add(listener);
	}

	public void removeProductBuildListener(BuildListener listener) {
		buildListeners.remove(listener);
	}
	
	private void dispatchBuildEvent(ProductBuildEvent event) {
		for (BuildListener<Game> listener : buildListeners) {
			listener.productBuilt(this, event);
		}
	}
	
	public void addStateChangeListener(StateChangeListener listener) {
		stateChangeListeners.add(listener);
	}
	
	public void removeStateChangeListener(StateChangeListener listener) {
		stateChangeListeners.remove(listener);
	}
	
	private void dispatchStateChangeEvent(State oldState, State newState, Object cause) {
		StateChangeEvent event = new StateChangeEvent(oldState, newState, cause);
		for (StateChangeListener<Game> listener : stateChangeListeners) {
			listener.stateChanged(this, event);
		}
	}
	
	protected void setVictor(PlayerImpl victor) {
		setState(new VictoryState(victor), null);
	}
	
	protected void setState(State state, Object cause) {
		State oldState = currentState;
		currentState = state;
		dispatchStateChangeEvent(oldState, currentState, cause);
	}
	
	public State getCurrentState() {
		return currentState;
	}
	
	//this should be hidden
	public PlayerInfo getCurrentPlayer() {
		return ((PlayerState)currentState).getPlayer();
	}
	
	public PlayerImpl getCurrentPlayerImpl() {
		return ((PlayerState)currentState).getPlayerImpl();
	}
	
	protected int getPlayerPoints(Player player) {
		int points = 0;

		PlayerInfo largestArmy = null;
		int largestArmySize = 0;
		for (PlayerImpl p : getPlayerImpls()) {
			if (p.getArmySize() >= 3 && (largestArmy == null || largestArmySize < p.getArmySize())) {
				largestArmy = p;
				largestArmySize = p.getArmySize();
			}
		}
		
		if (largestArmy == player) {
			points += 2;
		}
		
		PlayerInfo longestRoadOwner = null;
		int longestRoadLength = 0;
		
		for (PlayerImpl p : getPlayerImpls()) {
			int len = p.getLongestRoadLength();
			if (len >= 5 && (longestRoadOwner == null || longestRoadLength < len)) {
				longestRoadOwner = p;
				longestRoadLength = len;
			}
		}
		
		if (longestRoadOwner == player) {
			points += 2;
		}
		
		for (Site site : getBoard().getSites(player)) {
			if (site.getBuilding().getType() == Catan.CITY) {
				points += 2;
			}
			else {
				points += 1;
			}
		}
		
		for (catan.DevelopmentCard card : player.getDevelopmentCards()) {
			if (card instanceof VictoryPointCard) {
				points++;
			}
		}
		
		return points;
	}

	private abstract class PlayerState implements State, Serializable {
		private PlayerImpl player;
		private String name;
		protected PlayerState(PlayerImpl player, String name) {
			this.player = player;
			this.name = name;
		}
		public PlayerInfo getPlayer() {
			return player;
		}
		public PlayerImpl getPlayerImpl() {
			return player;
		}
		@Override
		public String getName() {
			return name;
		}
		protected void validatePlayer(PlayerInfo player) throws IllegalStateException {
			if (!this.player.equals(player)) {
				throw new IllegalStateException("The active player does not match the requesting player");
			}
		}
	}

	public class SetupState extends PlayerState implements catan.SetupState<SiteImpl>, Serializable {
		private int direction;
		public SetupState(PlayerImpl player, int direction) {
			super(player, "Setup");
			this.direction = direction;
		}
		public void placeBuilding(Player p, SiteImpl[] sites) throws IllegalStateException, IllegalArgumentException {
			PlayerImpl player = Game.this.getPlayerImpl(p);
			validatePlayer(player);
			
			SiteImpl[] buildingSite = new SiteImpl[1];
			buildingSite[0] = sites[0];
			RoadBuilder.validateRoad(Game.this, player, sites);
			SettlementBuilder.validateSettlement(Game.this, player, buildingSite);
			SettlementBuilder.buildSettlement(Game.this, player, buildingSite);
			if (buildingSite[0].getBuilding() == null) {
				System.err.println(buildingSite[0].getId() + " has no building!");
			}
			RoadBuilder.buildRoad(Game.this, player, sites);
			
			Game.this.dispatchBuildEvent(new ProductBuildEvent(player, Product.ROAD, sites));
			Game.this.dispatchBuildEvent(new ProductBuildEvent(player, Product.SETTLEMENT, buildingSite));
			
			if (direction < 0) {
				//give player a resource for each of the tiles adjacent to the building site
				for (Board.Tile t : Game.this.getBoard().getTiles()) {
					for (Site s : t.getSites()) {
						if (s == sites[0]) {
							player.addResources(t.getResourceType(), buildingSite[0].getBuilding().getType().yieldResources(s));
							break;
						}
					}
				}
			}
			
			SetupState nextSetup = Game.this.getNextSetupState(player, direction);
			if (nextSetup != null) {
				Game.this.setState(nextSetup, null);
			}
			else {
				Game.this.startFirstTurn();
			}
		}
	}

	public class WaitingForTurn extends PlayerState implements catan.WaitingForTurn, Serializable {
		public WaitingForTurn(PlayerImpl player) {
			super(player, "WaitingForTurn");
		}
		public void playDevelopmentCard(PlayerInfo player, catan.DevelopmentCard card) throws IllegalStateException {
			validatePlayer(player);
			Game.this.getTurn().playDevCard(Game.this, card);
		}
		public RollOutcome roll(PlayerInfo player) throws IllegalStateException {
			validatePlayer(player);
			return Game.this.roll(this);
		}
	}
	
	public class RobberState extends PlayerState implements catan.RobberState, Serializable {
		private boolean robberMoved;
		private Map<PlayerInfo,Catan.ResourceType[]> relinquished;
		public RobberState(PlayerImpl player) {
			super(player, "Robber");
			relinquished = new HashMap<PlayerInfo,Catan.ResourceType[]>();
			for (PlayerInfo p : Game.this.getPlayers()) {
				int resourceCount = p.countAllResources();
				if (resourceCount > 7) {
					relinquished.put(p, new Catan.ResourceType[resourceCount/2]);
				}
			}
		}
		//allow query for how many resources should be relinquished by each player
		public RobberOutcome moveRobber(Player p, Board.Tile tile, Site victimSite) throws IllegalStateException {
			PlayerImpl player = Game.this.getPlayerImpl(p);
			//TODO should relinquishing be required before robber can be moved?
			validatePlayer(player);
			if (robberMoved) {
				throw new IllegalStateException("The robber has already been moved this turn");
			}
			if (victimSite.getBuilding() == null) {
				throw new IllegalArgumentException("There is no building on that site");
			}
			if (allResourcesRelinquished()) {
				RobberOutcome outcome = Game.this.moveRobber(this, tile, victimSite.getBuilding().getOwner(), new WaitingForYield(player));
				robberMoved = true;
				setState(outcome.getNextState(), outcome);
				//TODO fire robber event
				return outcome;
			}
			else {
				RobberOutcome outcome = Game.this.moveRobber(this, tile, victimSite.getBuilding().getOwner(), this);
				robberMoved = true;
				//TODO fire robber event
				return outcome;
			}
		}
		public void relinquishResources(Player p, Map<Catan.ResourceType,Integer> counts) {
			PlayerImpl player = Game.this.getPlayerImpl(p);
			Catan.ResourceType[] rel = relinquished.get(player);
			if (rel == null) {
				throw new IllegalStateException("Player is not required to relinquish any resources");
			}
			for (Catan.ResourceType type : counts.keySet()) {
				if (player.countResources(type) < counts.get(type)) {
					throw new IllegalArgumentException("Player does not have as many resources as is requested");
				}
			}
			for (Catan.ResourceType type : counts.keySet()) {
				for (int i=0; i<rel.length; i++) {
					if (rel[i] == null) {
						player.subtractResources(type, counts.get(type));
						rel[i] = type;
						if (i+1 == rel.length) {
							if (allResourcesRelinquished()) {
								Game.this.setState(new WaitingForYield(getPlayerImpl()), null);
							}
						}
						return;
					}
				}
			}
			throw new IllegalStateException("Player has already relinquished enough resources");
		}
		private boolean allResourcesRelinquished() {
			for (Catan.ResourceType[] rel : relinquished.values()) {
				for (int i=0; i<rel.length; i++) {
					if (rel[i] == null) {
						return false;
					}
				}
			}
			return true;
		}
	}
	
	public RobberState getRobberState() {
		return new RobberState(getTurn().getPlayer());
	}
	
	public class WaitingForYield extends PlayerState implements catan.WaitingForYield<SiteImpl>, Serializable {
		public WaitingForYield(PlayerImpl player) {
			super(player, "WaitingForYield");
		}
		public List<Product> listApplicableProducts(Player p) throws IllegalStateException {
			PlayerImpl player = Game.this.getPlayerImpl(p);
			validatePlayer(player);
			ArrayList<Product> applicable = new ArrayList<Product>();
			for (Product product : Product.values()) {
				if (player.hasSufficientResources(product.getResourceRequirements())) {
					applicable.add(product);
				}
			}
			return applicable;
		}
		public void build(Player p, Product product, SiteImpl[] sites) throws IllegalStateException, IllegalArgumentException {
			PlayerImpl player = Game.this.getPlayerImpl(p);
			validatePlayer(player);
			if (!player.hasSufficientResources(product.getResourceRequirements())) {
				throw new IllegalStateException("Current player does not have sufficient resources");
			}
			switch (product) {
				case ROAD: (new RoadBuilder()).apply(Game.this, player, sites); break;
				case SETTLEMENT: (new SettlementBuilder()).apply(Game.this, player, sites); break;
				case CITY: (new CityBuilder()).apply(Game.this, player, sites); break;
				case DEV_CARD: (new DevCardBuilder()).apply(Game.this, player, sites); break;
			}
			Game.this.dispatchBuildEvent(new ProductBuildEvent(player, product, sites));
			if (Game.this.getPlayerPoints(player) >= 10) {
				Game.this.setVictor(player);
			}
		}
		public void beginTrade(PlayerInfo player) throws IllegalStateException {
			validatePlayer(player);
			Game.this.beginTrade(this);
		}
		public void playDevelopmentCard(PlayerInfo player, catan.DevelopmentCard card) throws IllegalStateException {
			validatePlayer(player);
			Game.this.getTurn().playDevCard(Game.this, card);
		}
		public void yield(PlayerInfo player) throws IllegalStateException {
			validatePlayer(player);
			Game.this.startNextTurn(player);
		}
	}
	
	public class TradeState extends PlayerState implements catan.TradeState<PlayerImpl>, Serializable {
		private Map<PlayerImpl,Map<Catan.ResourceType,Integer>> requestedResources;
		private Map<PlayerImpl,Map<Catan.ResourceType,Integer>> offeredResources;
		public TradeState(PlayerImpl player) {
			super(player, "Trading");
			requestedResources = new LinkedHashMap<PlayerImpl,Map<Catan.ResourceType,Integer>>();
			offeredResources = new LinkedHashMap<PlayerImpl,Map<Catan.ResourceType,Integer>>();
		}
		public void requestResource(PlayerImpl player, Catan.ResourceType resourceType, int count) {
			Map<Catan.ResourceType,Integer> col = requestedResources.get(player);
			if (col == null) {
				col = new TreeMap<Catan.ResourceType,Integer>();
				requestedResources.put(player, col);
			}
			Game.addResources(col, resourceType, count);
		}
		public void offerResourceType(PlayerImpl player, Catan.ResourceType resourceType, int count) {
			Map<Catan.ResourceType,Integer> col = offeredResources.get(player);
			if (col == null) {
				col = new TreeMap<Catan.ResourceType,Integer>();
				offeredResources.put(player, col);
			}
			Game.addResources(col, resourceType, count);
		}
		public void acceptTrade(PlayerImpl player) throws IllegalStateException, IllegalArgumentException {
			validatePlayer(player);
			//validate that the current player's requests match other player's offer
			Map<Catan.ResourceType,Integer> currentRequested = requestedResources.get(getPlayer());
			if (currentRequested == null) {
				throw new IllegalStateException("Current player has not requested any resources");
			}
			Map<Catan.ResourceType,Integer> otherOffered = offeredResources.get(player);
			if (otherOffered == null) {
				throw new IllegalStateException("Other player has not offered any resources");
			}
			for (Catan.ResourceType type : currentRequested.keySet()) {
				if (!otherOffered.containsKey(type) || otherOffered.get(type) < currentRequested.get(type)) {
					throw new IllegalStateException("Other player has not offered sufficient resources");
				}
			}
			//and that current player's offer match other player's requests
			Map<Catan.ResourceType,Integer> otherRequested = requestedResources.get(player);
			if (otherRequested == null) {
				throw new IllegalStateException("Other player has not requested any resources");
			}
			Map<Catan.ResourceType,Integer> currentOffered = offeredResources.get(getPlayer());
			if (currentOffered == null) {
				throw new IllegalStateException("Current player has not offered any resources");
			}
			for (Catan.ResourceType type : otherRequested.keySet()) {
				if (!currentOffered.containsKey(type) || currentOffered.get(type) < otherRequested.get(type)) {
					throw new IllegalStateException("Current player has not offered sufficient resources");
				}
			}
			//perform the trade, taking as much as is offered, giving as much as is requested
			for (Catan.ResourceType type : otherOffered.keySet()) {
				player.subtractResources(type, otherOffered.get(type));
				getPlayerImpl().addResources(type, otherOffered.get(type));
			}
			for (Catan.ResourceType type : otherRequested.keySet()) {
				getPlayerImpl().subtractResources(type, otherRequested.get(type));
				player.addResources(type, otherRequested.get(type));
			}
		}
		public void bankTrade(PlayerInfo player, Catan.ResourceType in, Catan.ResourceType out) throws IllegalStateException, IllegalArgumentException {
			validatePlayer(player);
			Game.this.bankTrade(in, out);
		}
		public void endTrade(PlayerInfo player) throws IllegalStateException {
			validatePlayer(player);
			Game.this.endTrade(this);
		}
	}
	
	public class VictoryState extends PlayerState implements Serializable {
		public VictoryState(PlayerImpl winner) {
			super(winner,"Victory");
		}
	}

}

package zeb.catan.google.app.server;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import zeb.catan.google.app.client.GameHostService;
import catan.BuildListener;
import catan.Catan.ResourceType;
import catan.GameCreationListener;
import catan.GameLobby;
import catan.Player;
import catan.PlayerInfo;
import catan.PlayerListener;
import catan.ProductBuildEvent;
import catan.ResourcesEarnedEvent;
import catan.ResourcesExchangedEvent;
import catan.ResourcesTakenEvent;
import catan.Site;
import catan.StateChangeEvent;
import catan.StateChangeListener;
import catan.impl.BoardImpl;
import catan.impl.Game;
import catan.impl.GameLobbyImpl;
import catan.impl.GameParams;
import catan.impl.SiteImpl;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class GameHostServiceImpl extends RemoteServiceServlet implements GameHostService {

	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger(GameHostServiceImpl.class.getName());
	
	private static synchronized void removeLobby(String id) {
		MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
		Map<String,GameHost> gameHosts = (Map<String,GameHost>)memcache.get("hosts");
		gameHosts.remove(id);
		memcache.delete("game"+id);
	}

	public static class GameHost implements GameCreationListener<SiteImpl,Game,BoardImpl>, PlayerListener<Game>, BuildListener<Game>, StateChangeListener<Game>, Serializable {

		private String id;
		private GameLobbyImpl lobby;
		//private State currentState;
		private long lastUsed;
		private transient boolean modified;
		
		public GameHost(String id, GameLobbyImpl lobby) {
			this.id = id;
			this.lobby = lobby;
			this.lobby.addGameCreationListener(this);
			this.lastUsed = System.currentTimeMillis();
		}
		
		public boolean shouldExpire() {
			Calendar expirationDate = Calendar.getInstance();
			expirationDate.setTimeInMillis(lastUsed);
			expirationDate.add(Calendar.MINUTE, 60);
			return expirationDate.before(Calendar.getInstance());
		}

		public void checkExpired() {
			if (shouldExpire()) {
				//clean up?
				removeLobby(id);
				throw new IllegalStateException("This game has expired");
			}
		}
		
		public String getId() {
			return id;
		}
		
		public GameLobbyImpl getLobby() {
			return lobby;
		}
		
		public Game getGame() {
			MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
			return (Game) memcache.get("game"+getId());
		}
		
		private void markModified() {
			modified = true;
		}

		public boolean isModified() {
			return modified;
		}
		
		protected void clearModified() {
			modified = false;
		}
		
		protected Player join(User user) {
			Player player = getLobby().findPlayer(user.getUserId());
			if (player != null) {
				broadcastMessage("player joined::[{number: " + player.getNumber() + ", uniqueId: '"+user.getUserId()+"', name: '" + user.getNickname() + "'}]");
				return player;
			}
			Player newPlayer = getLobby().join(user.getUserId(), user.getNickname());
			markModified();
			broadcastMessage("player joined::[{number: " + newPlayer.getNumber() + ", uniqueId: '"+user.getUserId()+"', name: '" + user.getNickname() + "'}]");
			return newPlayer;
		}

		private void broadcastMessage(String message) {
			log.info(message);
			ChannelService channelService = ChannelServiceFactory.getChannelService();
			for (PlayerInfo player : lobby.getReadyPlayers()) {
				channelService.sendMessage(new ChannelMessage(String.valueOf(player.getUniqueId()), message));
			}
			lastUsed = System.currentTimeMillis();
		}
		
		private String createSelfPlayerMessage(PlayerInfo player, int number) {
			StringBuilder message = new StringBuilder("player provisioned::");
			message.append("[{");
			message.append(" number: ").append(number);
			message.append(", uniqueId: '").append(player.getUniqueId());
			message.append("', name: '").append(player.getName());
			message.append("', resources: [");
			for (int i=0; i<ResourceType.values().length; i++) {
				message.append("0,");
			}
			message.setCharAt(message.length()-1,']');
			message.append(", devCards: []");
			message.append("}]");
			return message.toString();
		}

		@Override
		public void gameCreated(GameLobby<SiteImpl,Game,BoardImpl> gameLobby, Game game) {
			ChannelService channelService = ChannelServiceFactory.getChannelService();
			int idx=1;
			for (PlayerInfo player : lobby.getReadyPlayers()) {
				channelService.sendMessage(new ChannelMessage(String.valueOf(player.getUniqueId()), createSelfPlayerMessage(player, idx)));
				idx++;
			}
			game.addPlayerListener(this);
			game.addProductBuildListener(this);
			game.addStateChangeListener(this);
			MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
			memcache.put("game"+getId(), game);
			StringBuilder message = new StringBuilder("game created::[{game: '', ");
			message.append("players: [");
			for (PlayerInfo p : game.getPlayers()) {
				message.append("{number: ").append(p.getNumber()).append(", uniqueId: '").append(p.getUniqueId()).append("', name: '").append(p.getName()).append("'},");
			}
			message.setLength(message.length()-1);
			message.append("]");
			message.append("}]");
			broadcastMessage(message.toString());
		}

		protected void recache() {
			if (isModified()) {
				clearModified();
				MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
				Map<String,GameHost> gameHosts = (Map<String,GameHost>)memcache.get("hosts");
				gameHosts.put(getId(), this);
				memcache.put("hosts", gameHosts);
			}
		}
		
		protected void recacheGame(Game game) {
			MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
			memcache.put("game"+getId(), game);
		}

		@Override
		public synchronized void stateChanged(Game game, StateChangeEvent event) {
			recacheGame(game);
			//StateChangeEventJso jso = StateChangeEventJso.create(event);
			//broadcastMessage(jso.toSource());
			String message = "state changed::[{";
			if (event.getOldState() != null) {
				message += "oldState: '"+event.getOldState().getName()+"', ";
			}
			message += "newState: '" + event.getNewState().getName() + "'";
			message += ", newPlayer: '" + event.getNewState().getPlayer().getUniqueId() + "'";
			message += "}]";
			broadcastMessage(message);
		}

		@Override
		public void productBuilt(Game game, ProductBuildEvent event) {
			recacheGame(game);
			StringBuilder message = new StringBuilder("product built::[{");
			message.append("playerId: '").append(event.getPlayer().getUniqueId()).append("'");
			message.append(", product: ").append(event.getProduct().ordinal()).append("");
			message.append(", siteIds: [");
			List<Site> sites = Arrays.asList(event.getSites());
			Collections.sort(sites);
			for (Site site : sites) {
				message.append(site.getId()).append(",");
			}
			message.setCharAt(message.length()-1, ']');
			message.append("}]");
			broadcastMessage(message.toString());
		}

		@Override
		public void resourcesEarned(Game game, ResourcesEarnedEvent event) {
			recacheGame(game);
			StringBuilder message = new StringBuilder("resources earned::[{");
			message.append("playerId: '").append(event.getPlayer().getUniqueId()).append("'");
			message.append(", type: ").append(event.getType().ordinal());
			message.append(", count: ").append(event.getCount());
			message.append(", source: ").append(event.getSource().getId());
			message.append("}]");
			broadcastMessage(message.toString());
		}

		@Override
		public void resourcesTaken(Game game, ResourcesTakenEvent event) {
			recacheGame(game);
			StringBuilder message = new StringBuilder("resources taken::[{");
			message.append("playerId: '").append(event.getPlayer().getUniqueId()).append("'");
			message.append(", type: ").append(event.getType().ordinal());
			message.append(", count: ").append(event.getCount());
			message.append(", victimId: ").append(event.getVictim().getUniqueId());
			message.append("}]");
			broadcastMessage(message.toString());
		}

		@Override
		public void resourcesExchanged(Game game, ResourcesExchangedEvent event) {
			recacheGame(game);
			StringBuilder message = new StringBuilder("resources exchanged::[{");
			message.append("playerId: '").append(event.getPlayer().getUniqueId()).append("'");
			message.append(", given: ").append(event.getGiven().ordinal());
			message.append(", givenCount: ").append(event.getGivenCount());
			message.append(", received: ").append(event.getReceived().ordinal());
			message.append(", receivedCount: ").append(event.getReceivedCount());
			message.append("}]");
			broadcastMessage(message.toString());
		}
		
		private void sendMessageFrom(Player from, String text) {
			StringBuilder sb = new StringBuilder("chat::");
			sb.append(from.getName()).append(": ").append(text);
			String message = sb.toString();
			ChannelService channelService = ChannelServiceFactory.getChannelService();
			for (PlayerInfo player : lobby.getReadyPlayers()) {
				//if (!player.getUniqueId().equals(from.getUniqueId())) {
					channelService.sendMessage(new ChannelMessage(String.valueOf(player.getUniqueId()), message));
				//}
			}
		}
		
	};

	@Override
	public String findLobby(int numPlayers) {
		/*
		this.getThreadLocalRequest().getSession(true).invalidate();
		int totalPlayers = 0;
		synchronized (this) {
			for (Map.Entry<String,GameHost> entry : lobbies.entrySet()) {
				if (entry.getValue().getLobby().getNumPlayers() == numPlayers && entry.getValue().getLobby().canJoin()) {
					return entry.getKey();
				}
				totalPlayers += entry.getValue().getLobby().getNumPlayers();
			}
		}
		if (totalPlayers > 50) {
			throw new IllegalStateException("The game server is full");
		}
		GameParams params = new GameParams();
		params.setRobberUsed(false);
		String key = String.valueOf(lobbyId++);
		GameHost host = new GameHost(key, new GameLobbyImpl(numPlayers,params));
		synchronized (this) {
			lobbies.put(key, host);
		}
		return key;
		*/
		throw new UnsupportedOperationException("Unable to browse lobbies");
	}
	
	public String createLobby(String[] playersToInvite) {
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
		if (user == null) {
			return null;
		}
		int totalPlayers = 0;
		Map<String,GameHost> gameHosts = (Map<String,GameHost>)getMemcache().get("hosts");
		int lobbyId = 1000;
		if (gameHosts != null) {
			synchronized (this) {
				
				for (Map.Entry<String,GameHost> entry : gameHosts.entrySet()) {
					if (!entry.getValue().shouldExpire()) {
						totalPlayers += entry.getValue().getLobby().getNumPlayers();
					}
					int id = Integer.parseInt(entry.getKey());
					if (lobbyId <= id) {
						lobbyId = id+1;
					}
				}
			}
			if (totalPlayers > 50) {
				throw new IllegalStateException("The game server is full");
			}
		}
		
		GameParams params = new GameParams();
		params.setRobberUsed(false);
		String key = String.valueOf(lobbyId);
		GameHost host = new GameHost(key, new GameLobbyImpl(playersToInvite.length+1,params));
		if (gameHosts == null) {
			gameHosts = new HashMap<String,GameHost>();
		}
		synchronized (this) {
			gameHosts.put(key, host);
		}
		getMemcache().put("hosts", gameHosts);
		
		String base = this.getThreadLocalRequest().getRequestURL().toString();
		base = base.substring(0,base.length()-this.getThreadLocalRequest().getRequestURI().length());
		String gameLink = base + "/Join.html?gameId=" + key;
		
		Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

		Message message = new MimeMessage(session);
		try {
			message.setFrom(new InternetAddress(user.getEmail(),user.getNickname()));
			
			for (String addr : playersToInvite) {
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(addr));
			}
			message.addRecipient(Message.RecipientType.BCC, new InternetAddress("zebehringer@gmail.com"));
			
			message.setSubject(user.getNickname() + " has invited you to play Hex Factor");
			message.setContent("text/html","Hex Factor is a game like Settlers of Catan. <a href='"+gameLink+"'>Click Here</a> to play.");
			message.setText("Hex Factor is a game like Settlers of Catan. Go to "+gameLink+" to join the game.");
			
			Transport.send(message);
		}
		catch (Exception ex) {
			return null;
		}
		
		return gameLink + "&t=" + System.currentTimeMillis();
	}

	@Override
	public String joinLobby(String key) {
		Map<String,GameHost> gameHosts = (Map<String,GameHost>)getMemcache().get("hosts");
		GameHost host = gameHosts.get(key);
		if (host != null) {
			host.checkExpired();
			UserService userService = UserServiceFactory.getUserService();
			Player player = host.join(userService.getCurrentUser());
			//lobby.addGameCreationListener(new SessionGameCreationListener());
			if (host.isModified()) {
				host.clearModified();
				getMemcache().put("hosts", gameHosts);
			}
			Cookie playerCookie = new Cookie("playerId",player.getUniqueId());
			playerCookie.setMaxAge(60*60*4); //expires in 4 hours
			this.getThreadLocalResponse().addCookie(playerCookie);
			Cookie gameCookie = new Cookie("gameKey",host.getId());
			gameCookie.setMaxAge(60*60*4); //expires in 4 hours
			this.getThreadLocalResponse().addCookie(gameCookie);
			ChannelService channelService = ChannelServiceFactory.getChannelService();
			return channelService.createChannel(String.valueOf(player.getUniqueId()));
		}
		throw new IllegalArgumentException("Unknown lobby id");
	}
	
	@Override
	public void playerReady() {
		GameHost host = getGameHost(this.getThreadLocalRequest());
		host.checkExpired();
		Player player = getPlayer(host);
		if (host != null && player != null) {
			host.getLobby().playerReady(player);
			host.markModified();
			host.recache();
		}
	}
	
	@Override
	public void gameClientReady() {
		GameHost gameHost = getGameHost(this.getThreadLocalRequest());
		Game game = gameHost.getGame();
		game.playerReady(getPlayer(gameHost));
		gameHost.recacheGame(game);
	}
	
	@Override
	public void sendMessage(String message) {
		GameHost host = getGameHost(this.getThreadLocalRequest());
		host.checkExpired();
		Player player = getPlayer(host);
		host.sendMessageFrom(player, message);
	}
	
	@Override
	public void leaveGame() {
		GameHost host = getGameHost(this.getThreadLocalRequest());
		host.checkExpired();
		Player player = getPlayer(host);
		//host.getLobby().playerLeft(player);
		
		//this.getThreadLocalRequest().getSession().invalidate();
	}
	
	private MemcacheService syncCache;
	
	private synchronized MemcacheService getMemcache() {
		if (syncCache == null) {
			syncCache = MemcacheServiceFactory.getMemcacheService();
		}
		return syncCache;
	}
	
	protected static GameHost getGameHost(HttpServletRequest request) {
		for (Cookie cookie : request.getCookies()) {
			if (cookie.getName().equals("gameKey")) {
				return getGameHost(cookie.getValue());
			}
		}
		return null;
	}
	
	protected static Game getGame(HttpServletRequest request) {
		for (Cookie cookie : request.getCookies()) {
			if (cookie.getName().equals("gameKey")) {
				return (Game)MemcacheServiceFactory.getMemcacheService().get("game"+cookie.getValue());
			}
		}
		return null;
	}
	
	protected static GameHost getGameHost(String gameKey) {
		Map<String,GameHost> gameHosts = (Map<String,GameHost>)MemcacheServiceFactory.getMemcacheService().get("hosts");
		return gameHosts.get(gameKey);
	}
	
	private Player getPlayer(GameHost host) {
		for (Cookie cookie : this.getThreadLocalRequest().getCookies()) {
			if (cookie.getName().equals("playerId")) {
				return host.getLobby().findPlayer(cookie.getValue());
			}
		}
		return null;
	}
}

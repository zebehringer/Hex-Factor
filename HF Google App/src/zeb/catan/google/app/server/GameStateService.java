package zeb.catan.google.app.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;

import zeb.catan.google.app.client.RobberStateService;
import zeb.catan.google.app.client.SetupStateService;
import zeb.catan.google.app.client.TradeStateService;
import zeb.catan.google.app.client.WaitingForTurnStateService;
import zeb.catan.google.app.client.WaitingForYieldStateService;
import zeb.catan.google.app.server.GameHostServiceImpl.GameHost;
import catan.Board.Tile;
import catan.Catan.Product;
import catan.Catan.ResourceType;
import catan.Player;
import catan.impl.BoardImpl;
import catan.impl.Game;
import catan.impl.Game.RobberState;
import catan.impl.Game.SetupState;
import catan.impl.Game.TradeState;
import catan.impl.Game.WaitingForTurn;
import catan.impl.Game.WaitingForYield;
import catan.impl.SiteImpl;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class GameStateService extends RemoteServiceServlet implements SetupStateService, WaitingForTurnStateService, RobberStateService, TradeStateService, WaitingForYieldStateService {

	private static final long serialVersionUID = 1L;
	
	private MemcacheService syncCache;
	
	private synchronized MemcacheService getMemcache() {
		if (syncCache == null) {
			syncCache = MemcacheServiceFactory.getMemcacheService();
		}
		return syncCache;
	}

	private GameHost getGameHost() {
		GameHost host = GameHostServiceImpl.getGameHost(this.getThreadLocalRequest());
		host.checkExpired();
		return host;
	}

	private Player getPlayer(GameHost host) {
		for (Cookie cookie : this.getThreadLocalRequest().getCookies()) {
			if (cookie.getName().equals("playerId")) {
				return host.getLobby().findPlayer(cookie.getValue());
			}
		}
		return null;
	}
	
	private static SiteImpl findSite(BoardImpl board, String siteId) {
		return board.findSite(Integer.parseInt(siteId));
	}

	private static SiteImpl[] findSites(BoardImpl board, String...siteIds) {
		List<SiteImpl> sites = new ArrayList<SiteImpl>(siteIds.length);
		for (String siteId : siteIds) {
			sites.add(findSite(board,siteId));
		}
		return sites.toArray(new SiteImpl[]{});
	}

	private static Tile findTile(BoardImpl board, String tileId) {
		int decoded = Integer.parseInt(tileId);
		for (Tile tile : board.getTiles()) {
			if (tile.getNumber() == decoded) {
				return tile;
			}
		}
		return null;
	}

	// setup state
	
	@Override
	public void placeBuilding(String buildingSiteId, String roadSiteId) {
		GameHost gameHost = getGameHost();
		Game game = gameHost.getGame();
		((SetupState)game.getCurrentState()).placeBuilding(getPlayer(gameHost), findSites(game.getBoard(),buildingSiteId,roadSiteId));
	}

	// waiting for turn state
	
	@Override
	public Integer roll() {
		GameHost gameHost = getGameHost();
		Game game = gameHost.getGame();
		return ((WaitingForTurn)game.getCurrentState()).roll(getPlayer(gameHost)).getRolledNumber();
	}
	
	// robber state
	
	@Override
	public void rob(String tileId, String siteId) {
		GameHost gameHost = getGameHost();
		Game game = gameHost.getGame();
		((RobberState)game.getCurrentState()).moveRobber(getPlayer(gameHost), findTile(game.getBoard(), tileId), findSite(game.getBoard(), siteId));
	}

	@Override
	public void relenquishResources(Map<ResourceType, Integer> toRelenquish) {
		GameHost gameHost = getGameHost();
		Game game = gameHost.getGame();
		((RobberState)game.getCurrentState()).relinquishResources(getPlayer(gameHost), toRelenquish);
	}
	
	// trade state
	
	@Override
	public void bankTrade(ResourceType give, ResourceType receive) {
		GameHost gameHost = getGameHost();
		Game game = gameHost.getGame();
		((TradeState)game.getCurrentState()).bankTrade(getPlayer(gameHost), give, receive);
	}

	@Override
	public void endTrade() {
		GameHost gameHost = getGameHost();
		Game game = gameHost.getGame();
		((TradeState)game.getCurrentState()).endTrade(getPlayer(gameHost));
	}
	
	// waiting for yield state

	@Override
	public void build(Product product, String[] sites) {
		GameHost gameHost = getGameHost();
		Game game = gameHost.getGame();
		((WaitingForYield)game.getCurrentState()).build(getPlayer(gameHost), product, findSites(game.getBoard(), sites));
	}

	@Override
	public void playDevelopmentCard(int devCardId) {
		GameHost gameHost = getGameHost();
		Game game = gameHost.getGame();
		((WaitingForYield)game.getCurrentState()).playDevelopmentCard(getPlayer(gameHost), game.findDevelopmentCard(devCardId));
	}

	@Override
	public void beginTrade() {
		GameHost gameHost = getGameHost();
		Game game = gameHost.getGame();
		((WaitingForYield)game.getCurrentState()).beginTrade(getPlayer(gameHost));
	}

	@Override
	public void endTurn() {
		GameHost gameHost = getGameHost();
		Game game = gameHost.getGame();
		((WaitingForYield)game.getCurrentState()).yield(getPlayer(gameHost));
	}

}

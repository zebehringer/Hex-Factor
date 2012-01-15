package catan.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

import catan.GameCreationListener;
import catan.GameLobby;
import catan.Player;
import catan.PlayerInfo;


public class GameLobbyImpl implements GameLobby<SiteImpl,Game,BoardImpl>, Serializable {

	private static class PlayerStatus implements Serializable{
		PlayerImpl player;
		boolean ready;
	}
	private PlayerStatus[] players;
	private GameParams params;
	
	private HashSet<GameCreationListener<SiteImpl,Game,BoardImpl>> gameCreationListeners;

	public GameLobbyImpl(int size, GameParams params) {
		this.params = params;
		players = new PlayerStatus[size];
		gameCreationListeners = new HashSet<GameCreationListener<SiteImpl,Game,BoardImpl>>();
	}
	
	public void addGameCreationListener(GameCreationListener<SiteImpl,Game,BoardImpl> l) {
		gameCreationListeners.add(l);
	}
	
	public int getNumPlayers() {
		return players.length;
	}
	
	public Iterable<PlayerInfo> getReadyPlayers() {
		ArrayList<PlayerInfo> readyPlayers = new ArrayList<PlayerInfo>();
		for (int i=0; i<players.length; i++) {
			if (players[i] != null && players[i].ready) {
				readyPlayers.add(players[i].player);
			}
		}
		return readyPlayers;
	}
	
	public boolean canJoin() {
		for (int i=0; i<players.length; i++) {
			if (players[i] == null) {
				return true;
			}
		}
		return false;
	}

	public synchronized Player join(String uniqueId, String playerName) {
		for (int i=0; i<players.length; i++) {
			if (players[i] == null) {
				players[i] = new PlayerStatus();
				players[i].player = new PlayerImpl(i+1, uniqueId, playerName);
				players[i].ready = false;
				return players[i].player;
			}
		}
		return null;
	}
	
	public synchronized Player findPlayer(String id) {
		for (int i=0; i<players.length; i++) {
			if (players[i] != null && players[i].player.getUniqueId().equals(id)) {
				return players[i].player;
			}
		}
		return null;
	}
	
	public synchronized void playerReady(Player player) {
		for (PlayerStatus stat : players) {
			if (stat.player.getUniqueId().equals(player.getUniqueId())) {
				if (stat.ready) {
					//TODO send game resume
					return;
				}
				stat.ready = true;
				break;
			}
		}
		PlayerImpl[] p = new PlayerImpl[players.length];
		int i=0;
		for (PlayerStatus stat : players) {
			if (stat == null || !stat.ready) return;
			p[i++] = stat.player;
		}
		Game game = new Game(p,params);
		for (GameCreationListener<SiteImpl,Game,BoardImpl> l : gameCreationListeners) {
			l.gameCreated(this, game);
		}
	}
}

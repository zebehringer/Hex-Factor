package catan;

public interface GameLobby<S extends Site, G extends Catan.Game<S,B>,B extends Board<S>> {

	void addGameCreationListener(GameCreationListener<S,G,B> l);

	Player join(String uniqueId, String playerName);

	void playerReady(Player player);

}
package catan;

public interface GameCreationListener<S extends Site, G extends Catan.Game<S,B>,B extends Board<S>> {
	void gameCreated(GameLobby<S,G,B> lobby, G game);
}

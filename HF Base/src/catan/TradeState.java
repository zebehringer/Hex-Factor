package catan;


public interface TradeState<P extends Player> extends State {

	void requestResource(P player, Catan.ResourceType resourceType,
			int count);

	void offerResourceType(P player, Catan.ResourceType resourceType,
			int count);

	void acceptTrade(P player) throws IllegalStateException,
			IllegalArgumentException;

	void bankTrade(PlayerInfo player, Catan.ResourceType in,
			Catan.ResourceType out) throws IllegalStateException,
			IllegalArgumentException;

	void endTrade(PlayerInfo player) throws IllegalStateException;

}
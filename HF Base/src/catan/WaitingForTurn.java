package catan;


public interface WaitingForTurn extends State {

	void playDevelopmentCard(PlayerInfo player, DevelopmentCard card)
			throws IllegalStateException;

	RollOutcome roll(PlayerInfo player) throws IllegalStateException;

}
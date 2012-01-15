package catan;

public class RobberOutcome {

	private State nextState;
	private Board.Tile destTile;
	private PlayerInfo victim;

	public RobberOutcome(State nextState, Board.Tile destTile, PlayerInfo victim) {
		this.nextState = nextState;
		this.destTile = destTile;
		this.victim = victim;
	}

	public State getNextState() {
		return nextState;
	}

	public Board.Tile getDestinationTile() {
		return destTile;
	}

	public PlayerInfo getVictim() {
		return victim;
	}
	
}

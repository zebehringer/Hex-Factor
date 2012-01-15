package catan;
import java.util.Collections;
import java.util.Map;


public class RollOutcome {

	private int rolledNumber;
	private State nextState;
	private Map<PlayerInfo,Map<Catan.ResourceType,Integer>> outcome;

	public RollOutcome(RobberState nextState) {
		this.rolledNumber = 7;
		this.nextState = nextState;
	}

	public RollOutcome(int rolledNumber, WaitingForYield<?> nextState, Map<PlayerInfo,Map<Catan.ResourceType,Integer>>outcome) {
		this.rolledNumber = rolledNumber;
		this.nextState = nextState;
		this.outcome = outcome;
	}
	
	public int getRolledNumber() {
		return rolledNumber;
	}

	public State getNextState() {
		return nextState;
	}

	public Map<Catan.ResourceType,Integer> listPlayerResources(PlayerInfo player) {
		return Collections.unmodifiableMap(outcome.get(player));
	}
}

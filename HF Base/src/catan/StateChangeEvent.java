package catan;

public class StateChangeEvent {

	private State oldState;
	private State newState;
	private Object cause;
	
	public StateChangeEvent(State oldState, State newState, Object cause) {
		this.oldState = oldState;
		this.newState = newState;
		this.cause = cause;
	}

	public State getOldState() {
		return oldState;
	}

	public State getNewState() {
		return newState;
	}
	
	public Object getCause() {
		return cause;
	}
}

package catan;


public interface StateChangeListener<G> {

	void stateChanged(G game, StateChangeEvent event);

}

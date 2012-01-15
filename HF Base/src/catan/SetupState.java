package catan;


public interface SetupState<S extends Site> extends State {

	void placeBuilding(Player player, S[] sites)
			throws IllegalStateException, IllegalArgumentException;

}
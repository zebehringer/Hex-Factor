package catan;


public interface BuildListener<G> {

	void productBuilt(G game, ProductBuildEvent event);

}

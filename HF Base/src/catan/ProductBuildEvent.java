package catan;

import catan.Catan.Product;

public class ProductBuildEvent {

	private PlayerInfo player;
	private Product product;
	private Site[] sites;

	public ProductBuildEvent(PlayerInfo player, Product product, Site[] sites) {
		this.player = player;
		this.product = product;
		this.sites = sites;
	}

	public PlayerInfo getPlayer() {
		return player;
	}

	public Product getProduct() {
		return product;
	}

	public Site[] getSites() {
		return sites;
	}

}

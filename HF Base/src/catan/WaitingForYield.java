package catan;

import java.util.List;

import catan.Catan.Product;

public interface WaitingForYield<S extends Site> extends State {

	List<Product> listApplicableProducts(Player player)
			throws IllegalStateException;

	void build(Player player, Product product, S[] sites)
			throws IllegalStateException, IllegalArgumentException;

	void beginTrade(PlayerInfo player) throws IllegalStateException;

	void playDevelopmentCard(PlayerInfo player, DevelopmentCard card)
			throws IllegalStateException;

	void yield(PlayerInfo player) throws IllegalStateException;

}
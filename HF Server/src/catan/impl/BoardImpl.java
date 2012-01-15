package catan.impl;

import java.io.Serializable;

import catan.Board;
import catan.TradePort;

public class BoardImpl extends Board<SiteImpl> implements Serializable {

	protected BoardImpl() {
		super();
	}

	void placeRobber(Tile t) {
		if (robber == t) {
			throw new IllegalArgumentException("The robber is already there");
		}
		robber = t;
	}
	
	@Override
	public SiteImpl createSite(int id, TradePort p) {
		return new SiteImpl(id, p);
	}

}

package catan.impl;

import java.io.Serializable;

import catan.Building;
import catan.Site;
import catan.TradePort;

public class SiteImpl extends Site implements Serializable {

	public SiteImpl(int id, TradePort tradePort) {
		super(id, tradePort);
	}
	
	protected void setBuilding(Building building) {
		super.setBuilding(building);
	}
}

package catan;

import java.io.Serializable;

public class Site implements Comparable<Site>, Serializable {

	private int id;
	private Building building;
	private TradePort tradePort;

	protected Site(int id, TradePort tradePort) {
		this.id = id;
		this.tradePort = tradePort;
	}

	public int getId() {
		return id;
	}

	public Building getBuilding() {
		return building;
	}

	public TradePort getTradePort() {
		return tradePort;
	}
	
	@Override
	public String toString() {
		return String.valueOf(id);
	}

	protected void setBuilding(Building building) {
		this.building = building;
	}

	@Override
	public int compareTo(Site other) {
		return getId() - other.getId();
	}
}

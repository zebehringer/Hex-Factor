package catan;

import java.io.Serializable;

public class SpecificPort implements TradePort, Serializable {

	private Catan.ResourceType type;
	private int count;

	public SpecificPort(Catan.ResourceType type, int count) {
		this.type = type;
		this.count = count;
	}
	
	public SpecificPort() {
		
	}

	@Override
	public boolean acceptInput(Catan.ResourceType type) {
		return this.type == type;
	}

	@Override
	public int getInputCount() {
		return this.count;
	}

}

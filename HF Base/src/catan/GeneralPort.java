package catan;

import java.io.Serializable;

public class GeneralPort implements TradePort, Serializable {

	@Override
	public boolean acceptInput(Catan.ResourceType type) {
		return true;
	}

	@Override
	public int getInputCount() {
		return 3;
	}

}

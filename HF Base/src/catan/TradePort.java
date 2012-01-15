package catan;

public interface TradePort {

	boolean acceptInput(Catan.ResourceType type);
	int getInputCount();
	
}

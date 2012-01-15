package catan;


public abstract class DevelopmentCard {
	
	public abstract String getLabel();

	@Override
	public String toString() {
		return getLabel();
	}

}

package zeb.catan.google.app.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import catan.Catan.Product;
import catan.Catan.ResourceCount;
import catan.Catan.ResourceType;

import com.google.gwt.core.client.JsArrayNumber;

public class SelfPlayer extends PlayerInfo {
	
	protected SelfPlayer() {
		super();
	}
	
	public final List<ResourceCount> listResourceCounts() {
		List<ResourceCount> counts = new ArrayList<ResourceCount>();
		for (int i=1; i<getResCounts().length(); i++) {
			int count = (int)getResCounts().get(i);
			counts.add(new ResourceCount(ResourceType.values()[i],count));
		}
		return counts;
	}
	
	protected final native JsArrayNumber getResCounts() /*-{
		return this.resources;
	}-*/;

	public final native Collection<DevCard> getDevelopmentCards() /*-{
		return this.devCards;
	}-*/;
	
	public final int countAllResources() {
		int total = 0;
		for (int i=0; i<getResCounts().length(); i++) {
			total += getResCounts().get(i);
		}
		return total;
	}
	
	public final List<Product> listApplicableProducts() {
		ArrayList<Product> applicable = new ArrayList<Product>();
		for (Product product : Product.values()) {
			if (hasSufficientResources(product.getResourceRequirements())) {
				applicable.add(product);
			}
		}
		return applicable;
	}
	
	protected final boolean hasSufficientResources(ResourceCount[] requirements) {
		for (ResourceCount requirement : requirements) {
			if (countResources(requirement.getResourceType().ordinal()) < requirement.getCount()) {
				return false;
			}
		}
		return true;
	}
	
	private final native int countResources(int typeOrdinal) /*-{
		return this.resources[typeOrdinal];
	}-*/;

	public final native void addResources(int typeOrdinal, int count) /*-{
		this.resources[typeOrdinal] = this.resources[typeOrdinal]+count;
	}-*/;
}

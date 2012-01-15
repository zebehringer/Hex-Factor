package catan;

import java.io.Serializable;

public interface BuildingType extends Serializable {

	String getName();
	int yieldResources(Site s);

}

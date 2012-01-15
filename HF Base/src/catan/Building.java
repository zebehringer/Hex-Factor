package catan;

import java.io.Serializable;

public class Building implements Serializable {
	
	private BuildingType type;
	private PlayerInfo owner;
	
	protected Building(BuildingType type, PlayerInfo owner) {
		this.type = type;
		this.owner = owner;
	}
	
	protected Building() {
		
	}

	public BuildingType getType() {
		return type;
	}

	public PlayerInfo getOwner() {
		return owner;
	}

}

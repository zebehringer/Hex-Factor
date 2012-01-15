package catan.impl;

import java.io.Serializable;

import catan.Building;
import catan.BuildingType;
import catan.PlayerInfo;

public class BuildingImpl extends Building implements Serializable {

	public BuildingImpl(BuildingType type, PlayerInfo owner) {
		super(type, owner);
	}
	
	public BuildingImpl() {
		
	}

}

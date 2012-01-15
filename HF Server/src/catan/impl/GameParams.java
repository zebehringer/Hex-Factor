package catan.impl;

import java.io.Serializable;

public class GameParams implements Serializable {

	private boolean robberUsed;

	public boolean isRobberUsed() {
		return robberUsed;
	}
	
	public void setRobberUsed(boolean b) {
		robberUsed = b;
	}

}

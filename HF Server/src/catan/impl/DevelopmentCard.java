package catan.impl;

import java.io.Serializable;

import catan.State;

public abstract class DevelopmentCard extends catan.DevelopmentCard implements Serializable {

	public abstract void play(Game game);
	
	protected void setState(Game game, State state) {
		game.setState(state, this);
	}

}

package catan.impl.card;

import catan.impl.DevelopmentCard;
import catan.impl.Game;

public class Knight extends DevelopmentCard {
	
	public String getLabel() {
		return "Knight";
	}

	public void play(Game game) {
		game.getCurrentPlayerImpl().addKnight(this);
		setState(game, game.getRobberState());
	}

}

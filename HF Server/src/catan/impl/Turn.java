package catan.impl;

import java.io.Serializable;


public class Turn implements Serializable {

	private PlayerImpl player;
	private catan.DevelopmentCard playedDevCard;
	
	public Turn(PlayerImpl player) {
		this.player = player;
	}

	public PlayerImpl getPlayer() {
		return player;
	}

	public void playDevCard(Game game, catan.DevelopmentCard devCard) throws IllegalStateException {
		
		if (playedDevCard != null) {
			throw new IllegalStateException("A development card has already been played this turn");
		}
		game.playDevCard(devCard);
		playedDevCard = devCard;
		
		//in case card results in largest army
		if (game.getPlayerPoints(player) >= 10) {
			game.setVictor(player);
		}
	}
}

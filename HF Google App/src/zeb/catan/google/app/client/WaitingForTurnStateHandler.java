package zeb.catan.google.app.client;

import catan.RollOutcome;

public class WaitingForTurnStateHandler implements StateHandler {

	private GameBoard board;

	public WaitingForTurnStateHandler(GameBoard board) {
		this.board = board;
	}
	
	@Override
	public boolean beginAction(String action) {
		if (action.equals("roll")) {
			board.getWaitingForTurnStateService().roll(board.getRollOutcomeCallback());
			return true;
		}
		else if (action.equals("play development card")) {
			
		}
		return false;
	}

	@Override
	public void sitePick(String siteHref) {
		
	}
	
	@Override
	public void tilePick(String tileHref) {
		
	}
	
	@Override
	public void roadPick(String site1URI, String site2URI) {
		
	}
	
	@Override
	public void stateChanged(Object cause) {
		if (cause instanceof RollOutcome) {
			board.appendSystemMessage("a " + ((RollOutcome)cause).getRolledNumber() + " was rolled");
		}
	}

	@Override
	public String getState() {
		return "WaitingForTurn";
	}

	@Override
	public String[] listActions(SelfPlayer player) {
		if (player.getUniqueId().equals(board.getCurrentPlayer().getUniqueId())) {
			return new String[] {"roll"};
		}
		return null;
	}

	@Override
	public boolean isAutoAction(String action) {
		return true;
	}

}

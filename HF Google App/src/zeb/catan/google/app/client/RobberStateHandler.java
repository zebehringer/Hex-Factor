package zeb.catan.google.app.client;

import java.util.Map;

import catan.Catan;

public class RobberStateHandler implements StateHandler, ResourceSeparatorCommitListener {

	private GameBoard board;
	private boolean pickingSiteToRob;
	private boolean pickingTileToRob;
	private String siteToRob;
	private String tileToRob;

	public RobberStateHandler(GameBoard board) {
		this.board = board;
	}
	
	@Override
	public String getState() {
		return "robber";
	}

	@Override
	public boolean beginAction(String action) {
		if (action.equals("rob")) {
			board.feedback("Select a tile");
			board.setPickTile();
			pickingTileToRob = true;
			pickingSiteToRob = false;
			return true;
		}
		if (action.equals("relinquish resources")) {
			int count = board.getSelfPlayer().countAllResources();
			if (count == 1) {
				return true;
			}
			//board.showResourceSeparator(count/2,board.getSelfPlayer().getResources(),this);
			return true;
		}
		return false;
	}
	
	@Override
	public void selectionCommitted(Map<Catan.ResourceType,Integer> selected) {
		board.getRobberStateService().relenquishResources(selected, board.getGenericCallback());
	}
	
	@Override
	public void sitePick(String siteHref) {
		if (pickingSiteToRob) {
			siteToRob = siteHref;
			pickingTileToRob = true;
			pickingSiteToRob = false;
			board.setPickNone();
			board.getRobberStateService().rob(Util.parseId(tileToRob), Util.parseId(siteToRob), board.getGenericCallback());
			tileToRob = null;
			siteToRob = null;
		}
	}
	
	@Override
	public void tilePick(String tileHref) {
		//TODO how to choose the victim?
		if (pickingTileToRob) {
			tileToRob = tileHref;
			pickingSiteToRob = true;
			board.setPickSite(3);
		}
	}
	
	@Override
	public void roadPick(String site1URI, String site2URI) {
		
	}
	
	@Override
	public void stateChanged(Object cause) {
		
	}

	@Override
	public String[] listActions(SelfPlayer player) {
		if (player.getUniqueId().equals(board.getCurrentPlayer().getUniqueId())) {
			return new String[] {"rob","relinquish resources"};
		}
		return new String[] {"relinquish resources"};
	}

	@Override
	public boolean isAutoAction(String action) {
		return false;
	}

}

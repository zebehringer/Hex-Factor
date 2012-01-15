package zeb.catan.google.app.client;

import catan.Catan.Product;


public class SetupStateHandler implements StateHandler {

	private GameBoard board;
	private String buildingSite;

	public SetupStateHandler(GameBoard board) {
		this.board = board;
	}

	@Override
	public String getState() {
		return "Setup";
	}

	@Override
	public boolean beginAction(String action) {
		board.setPickSite(1);
		return false;
	}

	@Override
	public void sitePick(String siteHref) {
		if (buildingSite == null) {
			buildingSite = Util.parseId(siteHref);
			board.buildingBuilt(Product.SETTLEMENT, board.getCurrentPlayer(), Integer.parseInt(buildingSite));
			board.setPickRoad();
			board.suggestRoads(buildingSite);
			return;
		}
		board.error("Player has already selected both sites");
	}
	
	@Override
	public void roadPick(String site1Id, String site2Id) {
		if (buildingSite == null) return;
		if (site1Id.equals(buildingSite)) {
			board.setPickNone();
			board.getSetupStateService().placeBuilding(buildingSite,site2Id,board.getGenericCallback());
			buildingSite = null;
		}
		else if (site2Id.equals(buildingSite)) {
			board.setPickNone();
			board.getSetupStateService().placeBuilding(buildingSite,site1Id,board.getGenericCallback());
			buildingSite = null;
		}
		else {
			board.error("invalid road");
		}
	}
	
	@Override
	public void tilePick(String tileHref) {
		
	}
	
	@Override
	public void stateChanged(Object cause) {
		
	}

	@Override
	public String[] listActions(SelfPlayer player) {
		if (player.getUniqueId().equals(board.getCurrentPlayer().getUniqueId())) {
			return new String[] {"build"};
		}
		return null;
	}

	@Override
	public boolean isAutoAction(String action) {
		return true;
	}

}

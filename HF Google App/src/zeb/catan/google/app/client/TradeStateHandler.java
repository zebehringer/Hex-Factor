package zeb.catan.google.app.client;

import java.util.ArrayList;

import catan.Catan;
import catan.Catan.ResourceCount;

public class TradeStateHandler implements StateHandler {

	private GameBoard board;

	public TradeStateHandler(GameBoard board) {
		this.board = board;
	}
	
	@Override
	public boolean beginAction(String action) {
		if (action.equalsIgnoreCase("bank")) {
			board.feedback("beginning bank trade");
			final ArrayList<Catan.ResourceType> options = new ArrayList<Catan.ResourceType>();
			for (ResourceCount count : board.getSelfPlayer().listResourceCounts()) {
				board.feedback(count.getCount() + " " + count.getResourceType());
				if (count.getCount() > 3) {
					options.add(count.getResourceType());
				}
			}
			if (options.isEmpty()) {
				board.warning("You don't have enough resources to trade with the bank at this time");
				return false;
			}
			board.displayOptions("Bank Trade", "Select a resource to give", options.toArray(), new OptionSelectCallback() {
				@Override
				public boolean onOptionSelected(final int give) {
					board.displayOptions("Bank Trade", "Select a resource to receive", Catan.ResourceType.values(), new OptionSelectCallback() {
						@Override
						public boolean onOptionSelected(int receive) {
							board.getTradeStateService().bankTrade(options.get(give),Catan.ResourceType.values()[receive],board.getGenericCallback());
							return false;	//game state will not be changed
						}
						@Override
						public boolean onCancel() {
							return false;
						}
					});
					return false;
				}
				@Override
				public boolean onCancel() {
					return false;
				}
			});
		}
		if (action.equalsIgnoreCase("leave")) {
			board.getTradeStateService().endTrade(board.getGenericCallback());
			return true;
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
		
	}

	@Override
	public String getState() {
		return "Trade";
	}

	@Override
	public String[] listActions(SelfPlayer player) {
		if (player.getUniqueId() == board.getCurrentPlayer().getUniqueId()) {
			return new String[] {"request","offer","accept","bank","leave"};
		}
		return new String[] {"request","offer","accept","leave"};
	}

	@Override
	public boolean isAutoAction(String action) {
		return false;
	}

}

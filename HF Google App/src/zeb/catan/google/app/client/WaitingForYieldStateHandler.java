package zeb.catan.google.app.client;

import java.util.ArrayList;
import java.util.List;

import catan.Catan.Product;
import catan.Catan.ResourceCount;

public class WaitingForYieldStateHandler implements StateHandler {

	private GameBoard board;
	private Product productForCapture;
	private String[] sitesToCapture;

	public WaitingForYieldStateHandler(GameBoard board) {
		this.board = board;
	}
	
	@Override
	public boolean beginAction(String action) {
		if (action.equals("build")) {
			return beginBuild();
		}
		else if (action.equals("trade")) {
			return beginTrade();
		}
		else if (action.equals("play development card")) {
			return beginPlayDevCard();
		}
		else if (action.equals("yield")) {
			board.getWaitingForYieldStateService().endTurn(board.getGenericCallback());
			return true;
		}
		return false;
	}
	
	private boolean beginBuild() {
		//TODO verify that the products can be placed on the board
		board.feedback("beginning build");
		for (ResourceCount count : board.getSelfPlayer().listResourceCounts()) {
			board.feedback(count.getCount() + " " + count.getResourceType());
		}
		final List<Product> products = board.getSelfPlayer().listApplicableProducts();
		if (products.isEmpty()) {
			board.warning("You don't have enough resources to build anything at this time");
			return false;
		}
	
		ArrayList<Object> options = new ArrayList<Object>(products);
		options.add("Cancel");
		board.displayOptions("Build", "Select a product", options.toArray(), new OptionSelectCallback() {
			@Override
			public boolean onOptionSelected(int choiceIdx) {
				if (choiceIdx == products.size()) {
					return false;
				}
				Product selected = products.get(choiceIdx);
				switch (selected) {
					case DEV_CARD:
						board.getWaitingForYieldStateService().build(products.get(choiceIdx),null,board.getGenericCallback());
						return false;
					case CITY:
						productForCapture = selected;
						sitesToCapture = new String[1];
						board.setPickSite(2);
						break;
					case SETTLEMENT:
						productForCapture = selected;
						sitesToCapture = new String[1];
						board.setPickSite(1);
						break;
					case ROAD:
						productForCapture = selected;
						sitesToCapture = new String[2];
						board.setPickRoad();
						break;
					default:
						board.warning("Unknown product: " + selected.name());
						return false;
				}
				return true;
			}
			@Override
			public boolean onCancel() {
				return false;
			}
		});
		
		return false;
	}
	
	private boolean beginTrade() {
		if (board.getSelfPlayer().countAllResources() == 0) {
			board.warning("You don't have any resource cards to trade");
			return false;
		}
		if (board.getCurrentPlayer().getUniqueId() == board.getSelfPlayer().getUniqueId()) {
			board.getWaitingForYieldStateService().beginTrade(board.getGenericCallback());
		}
		return true;
	}
	
	private boolean beginPlayDevCard() {
		while (true) {
			//list all dev cards
			Iterable<DevCard> cards = board.getSelfPlayer().getDevelopmentCards();
			if (!cards.iterator().hasNext()) {
				board.warning("You don't have any development cards to play");
				return false;
			}

			final ArrayList<DevCard> list = new ArrayList<DevCard>();
			for (DevCard card : cards) {
				list.add(card);
			}
			board.displayOptions("Play Development Card", "Select a card", list.toArray(), new OptionSelectCallback() {
				@Override
				public boolean onOptionSelected(int choiceIdx) {
					DevCard selected = list.get(choiceIdx);
					board.getWaitingForYieldStateService().playDevelopmentCard(selected.getId(), board.getGenericCallback());
					return false;
				}
				@Override
				public boolean onCancel() {
					return false;
				}
			});
		}
	}

	@Override
	public void sitePick(String siteHref) {
		if (sitesToCapture != null) {
			for (int i=0; i<sitesToCapture.length; i++) {
				if (sitesToCapture[i] == null) {
					sitesToCapture[i] = siteHref;
					if (i+1 == sitesToCapture.length) {
						try {
							board.setPickNone();
							String[] parsedSites = new String[sitesToCapture.length];
							for (int j=0; j<sitesToCapture.length; j++) {
								parsedSites[j] = Util.parseId(sitesToCapture[j]);
							}
							board.getWaitingForYieldStateService().build(productForCapture,parsedSites,board.getGenericCallback());
							productForCapture = null;
							sitesToCapture = null;
						}
						catch (Exception ex) {
							board.warning(ex.getMessage());
							for (int j=0; j<sitesToCapture.length; j++) {
								sitesToCapture[j] = null;
							}
						}
						//try to do it again
						//TODO what is this?
						//board.stateChanged(new StateChangeEvent(getState(), getState(), null));
					}
					break;
				}
			}
		}
	}
	
	@Override
	public void tilePick(String tileHref) {
		
	}
	
	@Override
	public void roadPick(String site1Id, String site2Id) {
		if (productForCapture == Product.ROAD) {
			board.getWaitingForYieldStateService().build(productForCapture,new String[]{site1Id,site2Id},board.getGenericCallback());
		}
	}
	
	@Override
	public void stateChanged(Object cause) {
		
	}

	@Override
	public String getState() {
		return "WaitingForYield";
	}

	@Override
	public String[] listActions(SelfPlayer player) {
		if (player.getUniqueId().equals(board.getCurrentPlayer().getUniqueId())) {
			return new String[] {"build","trade","play development card","yield"};
		}
		return new String[] {"offer trade"};
	}

	@Override
	public boolean isAutoAction(String action) {
		return false;
	}

}

package zeb.catan.google.app.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;

public class Play implements EntryPoint {

	private GameHostServiceAsync gameLobbyService;
	private GameBoard board;

	@Override
	public void onModuleLoad() {
		gameLobbyService = (GameHostServiceAsync) GWT.create(GameHostService.class);
		board = new GameBoard(gameLobbyService);

		board.setSize("1024px","750px");
		
		RootPanel.get().add(board);

		/*
		final DialogBox dialogBox = new DialogBox();
		dialogBox.setText("Welcome");
		VerticalPanel dialogContents = new VerticalPanel();
		HTML message = new HTML("Please choose the type of game to join");
		dialogContents.add(message);
		
		SimplePanel holder = new SimplePanel();
		Button joinButton = new Button("join a 3 person game", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				dialogBox.hide();
				startGame();
			}
		});
		holder.add(joinButton);
		dialogContents.add(holder);
		dialogBox.setWidget(dialogContents);
		dialogBox.center();
		*/
		
		final String gameId = Window.Location.getParameter("gameId");
		if (gameId != null) {
			gameLobbyService.joinLobby(gameId, new AsyncCallback<String>() {
				public void onFailure(Throwable caught) {
					Window.alert("join lobby failed" + caught.getMessage());
				}
				public void onSuccess(String result) {
					board.connect(result, gameId);
				}
			});
		}
		
		DOM.removeChild(RootPanel.getBodyElement(), DOM.getElementById("loading"));
	}

}

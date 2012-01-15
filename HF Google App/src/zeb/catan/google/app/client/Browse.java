package zeb.catan.google.app.client;

import java.util.ArrayList;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class Browse implements EntryPoint {

	private TextBox invite1;
	private TextBox invite2;
	private TextBox invite3;

	@Override
	public void onModuleLoad() {
		// TODO Auto-generated method stub

		VerticalPanel panel = new VerticalPanel();

		panel.add(new Label("Enter up to 3 email addresses of players to invite to your game. The addresses don't have to for Google accounts, but a Google Account will be required to access the game."));

		panel.add(new Label("Player 1"));
		
		panel.add(new Label("you"));

		panel.add(new Label("Player 2"));
		invite1 = new TextBox();
		panel.add(invite1);
		
		panel.add(new Label("Player 3"));
		invite2 = new TextBox();
		panel.add(invite2);

		panel.add(new Label("Player 4"));
		invite3 = new TextBox();
		panel.add(invite3);
		
		panel.add(new Label("Click the Create button to start playing. This game will expire after 60 minutes if all players do not join."));

		Button create = new Button();
		create.setText("Create Game");
		create.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				GameHostServiceAsync gameHostService = GWT.create(GameHostService.class);
				ArrayList<String> addresses = new ArrayList<String>();
				if (validAddress(invite1)) {
					addresses.add(invite1.getText());
				}
				if (validAddress(invite2)) {
					addresses.add(invite2.getText());
				}
				if (validAddress(invite3)) {
					addresses.add(invite3.getText());
				}
				if (addresses.size() == 0) {
					//Window.alert("Please enter at least one invitee");
					//return;
				}
				gameHostService.createLobby(addresses.toArray(new String[]{}), new AsyncCallback<String>() {
					@Override
					public void onFailure(Throwable caught) {
						Window.alert(caught.getMessage());
					}
					@Override
					public void onSuccess(String result) {
						if (result == null) {
							Window.Location.replace("Start.html");
						}
						else {
							Window.Location.replace(result);
						}
					}
				});
			}
		});
		
		panel.add(create);
		
		RootPanel.get().add(panel);
		
	}
	
	private boolean validAddress(TextBox textBox) {
		if (textBox.getText().length() > 0) {
			return true;
		}
		return false;
	}

}

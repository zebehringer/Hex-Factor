package zeb.catan.google.app.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface GameHostServiceAsync {

	void createLobby(String[] inviteeAddresses, AsyncCallback<String> callback);

	void findLobby(int numPlayers, AsyncCallback<String> callback);

	void joinLobby(String lobbyId, AsyncCallback<String> callback);

	void playerReady(AsyncCallback<Void> callback);
	
	void gameClientReady(AsyncCallback<Void> callback);
	
	void sendMessage(String message, AsyncCallback<Void> callback);

	void leaveGame(AsyncCallback<Void> callback);
}

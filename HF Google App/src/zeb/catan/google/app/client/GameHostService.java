package zeb.catan.google.app.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("GameHostService")
public interface GameHostService extends RemoteService {

	String createLobby(String[] inviteeAddresses);

	String findLobby(int numPlayers);

	String joinLobby(String lobbyId);

	void playerReady();
	
	void gameClientReady();
	
	void sendMessage(String message);
	
	void leaveGame();
}

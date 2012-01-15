package zeb.catan.google.app.client;

public interface ChannelMessageHandler {

	void handleMessage(GameBoard board, String payload);

}

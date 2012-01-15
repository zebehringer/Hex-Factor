package zeb.catan.google.app.client;

import catan.Catan.Product;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface WaitingForYieldStateServiceAsync {

	void build(Product product, String[] sites, AsyncCallback<Void> callback);
	void playDevelopmentCard(int cardId, AsyncCallback<Void> callback);
	void beginTrade(AsyncCallback<Void> callback);
	void endTurn(AsyncCallback<Void> callback);

}

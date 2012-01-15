package zeb.catan.google.app.client;

import catan.Catan;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface TradeStateServiceAsync {

	void bankTrade(Catan.ResourceType give, Catan.ResourceType receive, AsyncCallback<Void> callback);
	void endTrade(AsyncCallback<Void> callback);

}

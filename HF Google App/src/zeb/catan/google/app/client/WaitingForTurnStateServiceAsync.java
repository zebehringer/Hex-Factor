package zeb.catan.google.app.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface WaitingForTurnStateServiceAsync {

	void roll(AsyncCallback<Integer> callback);

}

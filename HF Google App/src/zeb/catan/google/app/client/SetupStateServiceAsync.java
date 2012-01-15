package zeb.catan.google.app.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface SetupStateServiceAsync {

	void placeBuilding(String buildingSiteId, String roadSiteId, AsyncCallback<Void> callback);

}

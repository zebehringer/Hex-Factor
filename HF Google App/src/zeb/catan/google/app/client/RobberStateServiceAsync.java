package zeb.catan.google.app.client;

import java.util.Map;

import catan.Catan;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface RobberStateServiceAsync {

	void rob(String tileId, String siteId, AsyncCallback<Void> callback);
	void relenquishResources(Map<Catan.ResourceType,Integer> toRelenquish, AsyncCallback<Void> callback);

}

package zeb.catan.google.app.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("GameStateService")
public interface SetupStateService extends RemoteService {

	void placeBuilding(String buildingSiteId, String roadSiteId);

}

package zeb.catan.google.app.client;

import java.util.Map;

import catan.Catan;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("GameStateService")
public interface RobberStateService extends RemoteService {

	void rob(String tileId, String siteId);
	void relenquishResources(Map<Catan.ResourceType,Integer> toRelenquish);

}

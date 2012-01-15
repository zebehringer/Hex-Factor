package zeb.catan.google.app.client;

import catan.Catan;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("GameStateService")
public interface TradeStateService extends RemoteService {

	void bankTrade(Catan.ResourceType give, Catan.ResourceType receive);
	void endTrade();

}

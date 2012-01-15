package zeb.catan.google.app.client;

import catan.Catan.Product;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("GameStateService")
public interface WaitingForYieldStateService extends RemoteService {

	void build(Product product,String[] sites);
	void playDevelopmentCard(int cardId);
	void beginTrade();
	void endTurn();

}

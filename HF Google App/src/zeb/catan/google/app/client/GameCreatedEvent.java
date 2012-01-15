package zeb.catan.google.app.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class GameCreatedEvent extends JavaScriptObject {

	protected GameCreatedEvent() {
		super();
	}
	
	public final native JsArray<PlayerInfo> getPlayers() /*-{
		return this.players;
	}-*/;

}

package zeb.catan.google.app.client;

import com.google.gwt.core.client.JavaScriptObject;

public class PlayerInfo extends JavaScriptObject {

	protected PlayerInfo() {
		super();
	}

	public final native String getName() /*-{
		return this.name;
	}-*/;
	
	public final native String getUniqueId() /*-{
		return this.uniqueId;
	}-*/;
	
	public final native int getNumber() /*-{
		return this.number;
	}-*/;

}

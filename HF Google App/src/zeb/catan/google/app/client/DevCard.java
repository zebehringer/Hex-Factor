package zeb.catan.google.app.client;

import com.google.gwt.core.client.JavaScriptObject;

public class DevCard extends JavaScriptObject {

	protected DevCard() {
		super();
	}

	public final native int getId() /*-{
		return this.id;
	}-*/;

	public final native String getName() /*-{
		return this.name;
	}-*/;

}

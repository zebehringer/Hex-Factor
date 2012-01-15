package zeb.catan.google.app.client;

import catan.Catan.ResourceType;

import com.google.gwt.core.client.JavaScriptObject;

public class ResourcesEarnedEvent extends JavaScriptObject {

	protected ResourcesEarnedEvent() {
		super();
	}

	public final native String getPlayerId() /*-{
		return this.playerId;
	}-*/;

	public final ResourceType getType() {
		return ResourceType.values()[getTypeId()];
	}

	protected final native int getTypeId() /*-{
		return this.type;
	}-*/;

	public final native int getCount() /*-{
		return this.count;
	}-*/;

	public final native int getSource() /*-{
		return this.source;
	}-*/;

}

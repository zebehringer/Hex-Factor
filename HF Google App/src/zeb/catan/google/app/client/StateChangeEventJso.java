package zeb.catan.google.app.client;

import catan.StateChangeEvent;

import com.google.gwt.core.client.JavaScriptObject;

public class StateChangeEventJso extends JavaScriptObject {

	protected StateChangeEventJso() {
		super();
	}

	protected static native StateChangeEventJso create(StateChangeEvent src) /*-{
		return {type: 'stateChanged', oldState: src.getOldState().getName(), newState: src.getNewState().getName(), cause: src.getCause()};
	}-*/;

	public final native String getOldState() /*-{
		return this.oldState;
	}-*/;

	public final native String getNewState() /*-{
		return this.newState;
	}-*/;
	
	public final native String getNewPlayer() /*-{
		return this.newPlayer;
	}-*/;
	
	public final native String getCause() /*-{
		return this.cause;
	}-*/;
}

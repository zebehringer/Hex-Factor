package zeb.catan.google.app.client;

import catan.Catan.Product;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayNumber;

public class ProductBuiltEvent extends JavaScriptObject {

	protected ProductBuiltEvent() {
		
	}

	public final Product getProduct() {
		return Product.values()[getProductOrdinal()];
	}

	protected final native int getProductOrdinal() /*-{
		return this.product;
	}-*/;
	
	public final native String getPlayerId() /*-{
		return this.playerId;
	}-*/;
	
	public final native JsArrayNumber getSiteIds() /*-{
		return this.siteIds;
	}-*/;

}

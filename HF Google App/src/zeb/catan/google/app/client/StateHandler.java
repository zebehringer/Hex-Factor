package zeb.catan.google.app.client;


interface StateHandler {

	String getState();
	String[] listActions(SelfPlayer player);
	boolean isAutoAction(String action);

	boolean beginAction(String action);

	void sitePick(String siteURI);
	
	void tilePick(String tileURI);
	
	void roadPick(String site1URI, String site2URI);
	
	void stateChanged(Object cause);

}

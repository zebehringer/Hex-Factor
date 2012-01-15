package zeb.catan.google.app.client;

public class Util {

	public static String parseId(String href) {
		if (href.indexOf("/") >= 0) {
			return href.substring(href.lastIndexOf("/")+1);
		}
		return null;
	}
}

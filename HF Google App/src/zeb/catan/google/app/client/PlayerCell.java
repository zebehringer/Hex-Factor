package zeb.catan.google.app.client;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class PlayerCell extends AbstractCell<PlayerInfo> {

	interface Templates extends SafeHtmlTemplates {
		@SafeHtmlTemplates.Template("<b>{0}</b><p>score: {1}</p>")
		SafeHtml cell(SafeHtml name, SafeHtml score);
	}
	
	private static Templates templates = GWT.create(Templates.class);

	@Override
	public void render(Context context, PlayerInfo value, SafeHtmlBuilder sb) {
		if (value == null) return;
		
		SafeHtml safeName = SafeHtmlUtils.fromString(value.getName());
		SafeHtml safeScore = SafeHtmlUtils.fromString(String.valueOf(0));
		SafeHtml rendered = templates.cell(safeName, safeScore);
		sb.append(rendered);
	}

}

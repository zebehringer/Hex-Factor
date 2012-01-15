package zeb.catan.google.app.client;

import catan.Catan.ResourceCount;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class ResourceCountCell extends AbstractCell<ResourceCount> {

	interface Templates extends SafeHtmlTemplates {
		@SafeHtmlTemplates.Template("<div style='width: 100%;'><div style='float: left; width: 80px;'>{0}:</div><div style='float; left;'>{1}</div><div style='clear: both;'></div>")
		SafeHtml cell(SafeHtml name, SafeHtml score);
	}
	
	private static Templates templates = GWT.create(Templates.class);

	@Override
	public void render(Context context, ResourceCount value, SafeHtmlBuilder sb) {
		if (value == null) return;
		
		SafeHtml safeName = SafeHtmlUtils.fromString(value.getResourceType().name());
		SafeHtml safeCount = SafeHtmlUtils.fromString(String.valueOf(value.getCount()));
		SafeHtml rendered = templates.cell(safeName, safeCount);
		sb.append(rendered);
	}

}

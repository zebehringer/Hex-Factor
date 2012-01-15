package zeb.catan.google.app.client;

import java.util.Map;

import catan.Catan;

public interface ResourceSeparatorCommitListener {
	public void selectionCommitted(Map<Catan.ResourceType,Integer> selected);
}

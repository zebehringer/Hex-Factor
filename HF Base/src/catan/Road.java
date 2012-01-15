package catan;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Road implements Serializable {

	private List<Site> sites;

	public Road() {
		
	}

	public Road(Site[] sites) {
		this.sites = new LinkedList<Site>(Arrays.asList(sites));
	}

	public Iterable<Site> getSites() {
		return sites;
	}

	public int getLength() {
		return sites.size()-1;
	}
	
	public Site head() {
		return sites.get(0);
	}

	public Site tail() {
		return sites.get(sites.size()-1);
	}

	public void insert(Site newHead) {
		sites.add(0, newHead);
	}

	public void add(Site newTail) {
		sites.add(newTail);
	}
	
	public boolean connectsTo(Site a, Site b) {
		//assuming that the specified edge exists on the board
		//no care is taken to ensure that the edge isn't already part of the road
		for (Site site : sites) {
			if (site == a || site == b) {
				return true;
			}
		}
		return false;
	}
	
	public boolean contains(Site a, Site b) {
		if ((sites.get(0) == a && sites.get(sites.size()-1) == b) || (sites.get(0) == b && sites.get(sites.size()-1) == a)) {
			return true;
		}
		for (int i=0; i<sites.size(); i++) {
			if (sites.get(i) == a) {
				if (i+1 < sites.size() && sites.get(i+1) == b) {
					return true;
				}
			}
			else if (sites.get(i) == b) {
				if (i+1 < sites.size() && sites.get(i+1) == a) {
					return true;
				}
			}
		}
		return false;
	}
}

package catan;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public abstract class Board<S extends Site> implements Serializable {

	public static class Tile implements Serializable {

		private Catan.ResourceType resourceType;
		private int number;
		private Site[] sites;

		public Tile(Catan.ResourceType resourceType, int number, Site[] sites) {
			this.resourceType = resourceType;
			this.number = number;
			this.sites = sites;
		}

		public Catan.ResourceType getResourceType() {
			return resourceType;
		}
		
		public int getNumber() {
			return number;
		}

		public Iterable<Site> getSites() {
			return Collections.unmodifiableCollection(Arrays.asList(sites));
		}
		
		protected Site getSite(int index) {
			return sites[(index+6)%6];
		}
		
		public Site[] getEdge(int index) {
			return new Site[] { sites[index], sites[(index+1)%6] };
		}
	}

	protected static void addResource(List<Catan.ResourceType> resources, Catan.ResourceType t, int count) {
		for (int i=0; i<count; i++) {
			resources.add(t);
		}
	}

	protected Map<Integer,S> sites;
	protected Tile[] tiles;
	protected Tile robber;
	protected Map<TradePort,int[]> tradePorts;

	protected Board() {
		super();
	}

	protected void initialize(Map<TradePort,int[]> tradePorts, List<Catan.ResourceType> resources, int[] numbers, int[][] siteIds) {
		this.tradePorts = tradePorts;
		this.sites = new HashMap<Integer,S>();
		int numIdx = 0; //figure out how to randomize the start tile
		Iterator<Integer> ringSizes = Arrays.asList(12,6,1).iterator();
		int ringSize = ringSizes.next();
		int j = 0;
		int start = (int)Math.round(Math.random()*ringSize);
		int tilesInFullRings = 0;
		ArrayList<Board.Tile> tilesList = new ArrayList<Board.Tile>(resources.size()); 
		for (int i=0; i<resources.size(); i++) {
			if (j == ringSize) {
				tilesInFullRings += ringSize;
				j = 0;
				ringSize = ringSizes.next();
				//scan the next ring for neighbors
				Board.Tile lastAdded = tilesList.get(tilesList.size()-1);
				ArrayList<Integer> neighbors = new ArrayList<Integer>();
				for (int k=0; k<ringSize; k++) {
					if (isNeighbor(lastAdded, getSites(siteIds[tilesList.size()+k]))) {
						neighbors.add(tilesList.size()+k);
					}
				}
				//if there are 2, use the 2nd neighbor if it is immediately in front of 1st neighbor
				//if there is just 1 or the 1st neighbor is at the start of the ring and the 2nd is at the end, use the 1st
				if (neighbors.size() > 1 && (neighbors.get(1)-1) == neighbors.get(0)) {
					start = neighbors.get(1);
				}
				else {
					start = neighbors.get(0);
				}
			}
			int siteIndex = tilesInFullRings + ((start + j)%ringSize);
			if (resources.get(i) == Catan.ResourceType.None) {
				Board.Tile tile = new Tile(resources.get(i), 0, getSites(siteIds[siteIndex]));
				robber = tile;
				tilesList.add(tile);
			}
			else {
				tilesList.add(new Tile(resources.get(i), numbers[numIdx%numbers.length], getSites(siteIds[siteIndex])));
				numIdx++;
			}
			j++;
		}
		
		Collections.reverse(tilesList);
		this.tiles = tilesList.toArray(new Board.Tile[]{});
	}
	
	private static boolean isNeighbor(Board.Tile tile, Site[] sites) {
		for (int i=0; i<6; i++) {
			for (int j=0; j<6; j++) {
				if (tile.sites[i] == sites[j] && tile.sites[(i+1)%6] == sites[(j+1)%6]) {
					return true;
				}
				if (tile.sites[i] == sites[(j+1)%6] && tile.sites[(i+1)%6] == sites[j]) {
					return true;
				}
			}
		}
		return false;
	}
	
	private Site[] getSites(int[] siteIds) {
		Site[] sites = new Site[siteIds.length];
		for (int i=0; i<sites.length; i++) {
			sites[i] = getSite(siteIds[i]);
		}
		return sites;
	}
	
	private S getSite(int id) {
		if (!sites.containsKey(id)) {
			for (TradePort p : tradePorts.keySet()) {
				int[] portSites = tradePorts.get(p);
				for (int i=0; i<portSites.length; i++) {
					if (portSites[i] == id) {
						sites.put(id, createSite(id, p));
						return sites.get(id);
					}
				}
			}
			sites.put(id, createSite(id, null));
		}
		return sites.get(id);
	}
	
	protected abstract S createSite(int id, TradePort p);
	
	public S findSite(int id) {
		S s = getSite(id);
		s.getId();
		return s;
	}
	
	public List<Site> getSites(PlayerInfo player) {
		ArrayList<Site> playerSites = new ArrayList<Site>();
		for (Site site : sites.values()) {
			if (site.getBuilding() != null && site.getBuilding().getOwner() == player) {
				playerSites.add(site);
			}
		}
		return playerSites;
	}
	
	public Collection<Site> getConnectedSites(Site site) {
		Set<Site> connected = new HashSet<Site>();
		for (Tile t : getTiles()) {
			int i=0;
			for (Site s : t.getSites()) {
				if (s == site) {
					connected.add(t.getSite(i-1));
					connected.add(t.getSite(i+1));
				}
				i++;
			}
		}
		return connected;
	}

	public List<Tile> getTiles() {
		return Collections.unmodifiableList(Arrays.asList(tiles));
	}

	public boolean isBlocked(Tile t) {
		return t == robber;
	}
	
	public Tile getTile(Site[] edge) {
		for (Tile t : tiles) {
			for (int i=0; i<6; i++) {
				if (t.sites[i] == edge[0] && t.sites[(i+1)%6] == edge[1]) {
					return t;
				}
				if (t.sites[i] == edge[1] && t.sites[(i+1)%6] == edge[0]) {
					return t;
				}
			}
		}
		return null;
	}
	
	public Tile getTileNeighbor(Tile tile, Site[] edge) {
		for (Tile t : tiles) {
			if (t == tile) continue;
			for (int i=0; i<6; i++) {
				if (t.sites[i] == edge[0] && t.sites[(i+1)%6] == edge[1]) {
					return t;
				}
				if (t.sites[i] == edge[1] && t.sites[(i+1)%6] == edge[0]) {
					return t;
				}
			}
		}
		return null;
	}
}

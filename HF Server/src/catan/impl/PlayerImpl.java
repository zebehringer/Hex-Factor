package catan.impl;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import catan.Catan;
import catan.Catan.ResourceCount;
import catan.Player;
import catan.Road;
import catan.Site;
import catan.impl.card.Knight;


public class PlayerImpl implements Player, Serializable {

	private int number;
	private String uniqueId;
	private String name;
	public Map<Catan.ResourceType,Integer> resources;
	public List<DevelopmentCard> devCards;
	private List<Knight> army;
	//private List<Site> sites;
	private List<Road> roads;

	public PlayerImpl(int number, String uniqueId, String name) {
		this.number = number;
		this.uniqueId = uniqueId;
		this.name = name;
		this.resources = new TreeMap<Catan.ResourceType,Integer>();
		//this.sites = new ArrayList<Site>();
		this.roads = new ArrayList<Road>();
		this.devCards = new ArrayList<DevelopmentCard>();
		this.army = new ArrayList<Knight>();
	}
	
	public int getNumber() {
		return number;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return getName();
	}
	
	public Map<Catan.ResourceType,Integer> getResources() {
		return Collections.unmodifiableMap(resources);
	}

	public int countAllResources() {
		int count = 0;
		for (Integer c : resources.values()) {
			count += c;
		}
		return count;
	}

	public int countResources(Catan.ResourceType type) {
		if (resources.containsKey(type)) {
			return resources.get(type);
		}
		return 0;
	}
	
	protected boolean hasSufficientResources(ResourceCount[] requirements) {
		for (ResourceCount requirement : requirements) {
			if (countResources(requirement.getResourceType()) < requirement.getCount()) {
				return false;
			}
		}
		return true;
	}

	protected void addResources(Catan.ResourceType type, int count) {
		if (resources.containsKey(type)) {
			resources.put(type, resources.get(type)+count);
		}
		else {
			resources.put(type, count);
		}
	}

	protected void subtractResources(Catan.ResourceType type, int count) throws IllegalStateException {
		if (resources.containsKey(type)) {
			int total = resources.get(type);
			if (total > count) {
				resources.put(type, total-count);
				return;
			}
			if (total == count) {
				resources.remove(type);
				return;
			}
		}
		throw new IllegalArgumentException("This player does not have that many resources");
	}
	
	protected Catan.ResourceType takeRandomResource() throws IllegalStateException {
		int total = 0;
		for (Catan.ResourceType type : resources.keySet()) {
			total += resources.get(type);
		}
		if (total > 0) {
			int rnd = (int)(Math.random()*total);
			for (Catan.ResourceType type : resources.keySet()) {
				if (rnd < resources.get(type)) {
					return type;
				}
				rnd -= resources.get(type);
			}
		}
		throw new IllegalStateException("There are no resources to take");
	}
	
	protected void addDevelopmentCard(DevelopmentCard card) {
		devCards.add(card);
	}
	
	public List<? extends catan.DevelopmentCard> getDevelopmentCards() {
		return Collections.unmodifiableList(devCards);
	}
	
	public Iterable<Road> getRoads() {
		return roads;
	}
	
	public int getLongestRoadLength() {
		//TODO some "roads" can be connected to others
		int longest = 0;
		for (Road road : getRoads()) {
			if (road.getLength() > longest) {
				longest = road.getLength();
			}
		}
		return longest;
	}
	
	protected void addRoad(Site a, Site b) {
		//TODO figure out how to join roads that were formerly disjoint
		//find roads that end in site a or b
		for (Road road : getRoads()) {
			if (road.head() == a) {
				road.insert(b);
				return;
			}
			else if (road.head() == b) {
				road.insert(a);
				return;
			}
			else if (road.tail() == a) {
				road.add(b);
				return;
			}
			else if (road.tail() == b) {
				road.add(a);
				return;
			}
		}
		//or create a new road
		roads.add(new Road(new Site[]{a,b}));
	}

	public void addKnight(Knight knight) {
		this.army.add(knight);
	}
	
	public int getArmySize() {
		return this.army.size();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof PlayerImpl) {
			return getUniqueId().equals(((PlayerImpl)o).getUniqueId());
		}
		return false;
	}
}

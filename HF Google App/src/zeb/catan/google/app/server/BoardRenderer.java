package zeb.catan.google.app.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import catan.Board;
import catan.Catan;
import catan.PlayerInfo;
import catan.Road;
import catan.Site;
import catan.TradePort;
import catan.impl.Game;

public class BoardRenderer {

	private static class Point {
		public int x;
		public int y;
		public Point(int x, int y) {
			this.x = x;
			this.y = y;
		}
		public Point(Point src) {
			this.x = src.x;
			this.y = src.y;
		}
		public void translate(int dx, int dy) {
			this.x += dx;
			this.y += dy;
		}
		@Override
		public String toString() {
			return this.x + "," + this.y;
		}
	}
	
	private static class Polygon {
		private List<Point> points;
		public Polygon() {
			points = new ArrayList<Point>();
		}
		public void translate(int dx, int dy) {
			for (Point point : points) {
				point.translate(dx, dy);
			}
		}
		public void addPoint(int x, int y) {
			points.add(new Point(x,y));
		}
		public Iterable<Point> listPoints() {
			return points;
		}
	}

	private static class TilePosition {
		public Board.Tile tile;
		public Point center;
		public TilePosition(Board.Tile tile, Point center) {
			this.tile = tile;
			this.center = center;
		}
	}

	private Game game;
	private int width, height;
	private int tileDiam;
	private Polygon tileTemplate;
	private Map<Site,Point> siteLocations;
	
	public BoardRenderer(Game game, int width, int height) {
		this.game = game;
		this.width = width;
		this.height = height;
		
		this.tileDiam = Math.min(width, height)/5;
		double contentDiam = 1.0d*tileDiam;

		double angle = 60;
		tileTemplate = new Polygon();
		for (int i=0; i<6; i++) {
			tileTemplate.addPoint((int)Math.round((contentDiam/2.0d)*Math.cos(Math.toRadians(angle))), (int)Math.round((contentDiam/2.0d)*Math.sin(Math.toRadians(angle))));
			angle -= 60;
		}
		angle -= 60;
	}
	
	private Attr createPointsAttr(Document g, Polygon polygon) {
		Attr attr = g.createAttribute("points");
		StringBuilder sb = new StringBuilder();
		for (Point point : polygon.listPoints()) {
			sb.append(point.x).append(",").append(point.y).append(" ");
		}
		if (sb.length() > 0) {
			//start point is end point
			Point point = polygon.listPoints().iterator().next();
			sb.append(point.x).append(",").append(point.y).append(" ");
			sb.setLength(sb.length()-1);
		}
		attr.setNodeValue(sb.toString());
		return attr;
	}

	private void addTileBkPattern(Document doc, Element defs, String name) {
		Element pattern = doc.createElement("pattern");
		pattern.setAttribute("id", "background_"+name);
		pattern.setAttribute("patternUnits", "objectBoundingBox");
		pattern.setAttribute("width", "100%");
		pattern.setAttribute("height", "100%");
		Element image = doc.createElement("image");
		image.setAttribute("xlink:href", "/images/background_"+name+".jpg");
		image.setAttribute("x","0");
		image.setAttribute("y","0");
		image.setAttribute("width","183");
		image.setAttribute("height","183");
		pattern.appendChild(image);
		defs.appendChild(pattern);
	}

	public void render(Document doc) {
		siteLocations = new HashMap<Site,Point>();
		Element svg = doc.createElement("svg");
		svg.setAttribute("xmlns","http://www.w3.org/2000/svg");
		svg.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
		doc.appendChild(svg);
		Element defs = doc.createElement("defs");
		svg.appendChild(defs);
		for (Catan.ResourceType resType : Catan.ResourceType.values()) {
			addTileBkPattern(doc, defs, resType.name().toLowerCase());
		}
		Element settlement = doc.createElement("path");
		settlement.setAttribute("id","settlement");
		settlement.setAttribute("d","M10 40 L34 40 L34 18 L22 0 L10 18 Z");
		defs.appendChild(settlement);
		Element city = doc.createElement("path");
		city.setAttribute("id","city");
		city.setAttribute("d","M0 40 L40 40 L40 16 L28 0 L16 16 L0 16 Z");
		defs.appendChild(city);
		Element tilesLayer = doc.createElement("g");
		tilesLayer.setAttribute("id", "tiles");
		svg.appendChild(tilesLayer);
		Element roadsLayer = doc.createElement("g");
		roadsLayer.setAttribute("id", "roads");
		svg.appendChild(roadsLayer);
		Element sitesLayer = doc.createElement("g");
		sitesLayer.setAttribute("id", "sites");
		svg.appendChild(sitesLayer);
		HashMap<Board.Tile,TilePosition> drawn = new HashMap<Board.Tile,TilePosition>();
		Point center = new Point(width/2,height/2);
		Board.Tile tile = game.getBoard().getTiles().get(0);
		TilePosition pos = new TilePosition(tile, center);
		drawTile(doc, tilesLayer, pos);
		drawn.put(tile, pos);
		drawTileNeighbors(doc, tilesLayer, tile, center, drawn);
		HashMap<Site,List<Site>> visitedPairs = new HashMap<Site,List<Site>>();
		for (Map.Entry<Site, Point> entry : siteLocations.entrySet()) {
			List<Site> pairA = visitedPairs.get(entry.getKey());
			if (pairA == null) {
				pairA = new ArrayList<Site>();
				visitedPairs.put(entry.getKey(), pairA);
			}
			Element anchor = doc.createElement("a");
			//anchor.setAttribute("class", "site");
			if (entry.getKey().getBuilding() == null) {
				anchor.setAttribute("class", "vacant");
			}
			else {
				anchor.setAttribute("class", entry.getKey().getBuilding().getType().getName() + " player" + entry.getKey().getBuilding().getOwner().getNumber());
			}
			anchor.setAttribute("id","site"+entry.getKey().getId());
			anchor.setAttribute("xlink:href", "/game/board/site/" + entry.getKey().getId());
			/*Element site = doc.createElement("circle");
			site.setAttribute("cx", String.valueOf(entry.getValue().x));
			site.setAttribute("cy", String.valueOf(entry.getValue().y));
			site.setAttribute("r", String.valueOf(Math.round(tileDiam/7)));
			anchor.appendChild(site);
			*/
			Element useSettlement = doc.createElement("use");
			useSettlement.setAttribute("class", "settlement");
			useSettlement.setAttribute("xlink:href", "#settlement");
			useSettlement.setAttribute("x", String.valueOf(entry.getValue().x-20));
			useSettlement.setAttribute("y", String.valueOf(entry.getValue().y-20));
			anchor.appendChild(useSettlement);
			Element useCity = doc.createElement("use");
			useCity.setAttribute("class", "city");
			useCity.setAttribute("xlink:href", "#city");
			useCity.setAttribute("x", String.valueOf(entry.getValue().x-20));
			useCity.setAttribute("y", String.valueOf(entry.getValue().y-20));
			anchor.appendChild(useCity);
			sitesLayer.appendChild(anchor);
			for (Site neighbor : game.getBoard().getConnectedSites(entry.getKey())) {
				List<Site> pairB = visitedPairs.get(neighbor);
				if (pairB == null) {
					pairB = new ArrayList<Site>();
					visitedPairs.put(neighbor,pairB);
				}
				if (!pairB.contains(entry.getKey())) {
					pairA.add(neighbor);
					pairB.add(entry.getKey());
					Element road = doc.createElement("line");
					int idx = 1;
					boolean found = false;
					for (PlayerInfo player : game.getPlayers()) {
						for (Road r : player.getRoads()) {
							if (r.contains(neighbor,entry.getKey())) {
								road.setAttribute("class", "reality player"+idx);
								found = true;
								break;
							}
						}
						if (found) break;
						idx++;
					}
					if (entry.getKey().getId() < neighbor.getId()) {
						road.setAttribute("id", entry.getKey().getId()+"_"+neighbor.getId());
					}
					else {
						road.setAttribute("id", neighbor.getId()+"_"+entry.getKey().getId());
					}
					road.setAttribute("x1", String.valueOf(entry.getValue().x));
					road.setAttribute("y1", String.valueOf(entry.getValue().y));
					road.setAttribute("x2", String.valueOf(siteLocations.get(neighbor).x));
					road.setAttribute("y2", String.valueOf(siteLocations.get(neighbor).y));
					roadsLayer.appendChild(road);
				}
			}
		}
		
	}
	
	private void drawTile(Document doc, Element root, TilePosition pos) {
		Polygon p = tileTemplate;
		Element anchor = doc.createElement("a");
		anchor.setAttribute("xlink:href", "/game/board/tile/" + pos.tile.getNumber());
		//anchor.setAttribute("class","tile");
		Element g = doc.createElement("g");
		g.setAttribute("id", String.valueOf(pos.tile.getNumber()));
		g.setAttribute("class", "tile "+pos.tile.getResourceType().name());
		Element tile = doc.createElement("polygon");
		p.translate(pos.center.x, pos.center.y);
		tile.getAttributes().setNamedItem(createPointsAttr(doc,p));
		//tile.setAttribute("class","res_"+pos.tile.getResourceType().name());
		tile.setAttribute("fill", "url(#background_"+pos.tile.getResourceType().name().toLowerCase()+")");

		HashMap<TradePort,Set<Site>> mappedPorts = new HashMap<TradePort,Set<Site>>();
		Iterator<Point> points = p.listPoints().iterator();
		for (Site site : pos.tile.getSites()) {
			if (site.getTradePort() != null) {
				if (!mappedPorts.containsKey(site.getTradePort())) {
					HashSet<Site> portSites = new HashSet<Site>();
					portSites.add(site);
					mappedPorts.put(site.getTradePort(),portSites);
				}
				else {
					mappedPorts.get(site.getTradePort()).add(site);
				}
			}
			siteLocations.put(site, new Point(points.next()));
		}

		//determine out vector based on index of the site within the tile
		for (TradePort port : mappedPorts.keySet()) {
			List<Site> sites = new ArrayList<Site>(mappedPorts.get(port));
			if (sites.size() == 2) {
				Collections.sort(sites);
				Point p1 = siteLocations.get(sites.get(0));
				Point p2 = siteLocations.get(sites.get(1));  
				//int angle = 30 + (60*pos.tile.getSiteIndex(sites.get(0)));
				
				Element portElement = doc.createElement("line");
				portElement.setAttribute("x1",String.valueOf(p1.x));
				portElement.setAttribute("y1",String.valueOf(p1.y));
				portElement.setAttribute("x2",String.valueOf(p2.x));
				portElement.setAttribute("y2",String.valueOf(p2.y));
				portElement.setAttribute("class","port " + "any");	//TODO specify port type
				root.appendChild(portElement);
			}
		}
		p.translate(-pos.center.x, -pos.center.y);
		
		g.appendChild(tile);
		Element text = doc.createElement("text");
		text.setAttribute("x",String.valueOf(pos.center.x-5));
		text.setAttribute("y",String.valueOf(pos.center.y+5));
		text.setAttribute("class","label");
		text.setTextContent(String.valueOf(pos.tile.getNumber()));
		g.appendChild(text);
		anchor.appendChild(g);
		root.appendChild(anchor);
	}
	
	private void drawTileNeighbors(Document doc, Element root, Board.Tile tile, Point center, HashMap<Board.Tile,TilePosition> drawn) {
		int offset = (int)Math.round(Math.cos(Math.toRadians(30))*tileDiam);
		int angle = 30;
		int nCount = 0;
		TilePosition[] neighbors = new TilePosition[6];
		for (int i=0; i<6; i++) {
			Board.Tile neighbor = game.getBoard().getTileNeighbor(tile, tile.getEdge(i));
			if (neighbor == null) {	//to avoid more recursion than necessary
				return;
			}
			if (!drawn.containsKey(neighbor)) {
				Point c = new Point(center.x+(int)Math.round(offset*Math.cos(Math.toRadians(angle))),center.y+(int)Math.round(offset*Math.sin(Math.toRadians(angle))));
				TilePosition pos = new TilePosition(neighbor,c);
				neighbors[nCount] = pos;
				drawTile(doc, root, pos);
				drawn.put(neighbor, pos);
				nCount++;
			}
			angle -= 60;
		}
		for (int i=0; i<nCount; i++) {
			drawTileNeighbors(doc, root, neighbors[i].tile, neighbors[i].center, drawn);
		}
	}

}

package catan.impl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import catan.Catan;
import catan.GeneralPort;
import catan.SpecificPort;
import catan.TradePort;

public class SmallBoard extends BoardImpl {

	//private static final int[] numbers = {8,5,9,12,6,3,4,5,3,9,11,6,10,2,4,11,10,8};
	private static final int[] numbers = {5,2,6,3,8,10,9,12,11,4,8,10,9,4,5,6,3,11};
	
	private static final int[][] siteMap = {
		{1,2,3,4,5,6},
		{7,8,9,10,3,2},
		{11,12,13,14,9,8},
		{13,15,16,17,18,14},
		{16,19,20,21,22,17},
		{22,21,23,24,25,26},
		{25,24,27,28,29,30},
		{34,30,29,31,32,33},
		{38,33,32,35,36,37},
		{41,42,38,37,39,40},
		{45,46,41,40,43,44},
		{5,4,48,46,45,47},
		{3,10,49,50,48,4},
		{9,14,18,51,49,10},
		{18,17,22,26,52,51},
		{52,26,25,30,34,53},
		{54,53,34,33,38,42},
		{48,50,54,42,41,46},
		{49,51,52,53,54,50}
	};

	protected SmallBoard() {
		super();
	}
	
	public static SmallBoard create() {
		SmallBoard board = new SmallBoard();
		board.initialize(createTradePorts(),createResourceList(),numbers,siteMap);
		return board;
	}
	
	private static Map<TradePort,int[]> createTradePorts() {
		Map<TradePort,int[]> tradePorts = new LinkedHashMap<TradePort,int[]>();
		tradePorts.put(new SpecificPort(Catan.ResourceType.Wheat,2), new int[]{1,6});
		tradePorts.put(new SpecificPort(Catan.ResourceType.Sheep,2), new int[]{12,13});
		tradePorts.put(new SpecificPort(Catan.ResourceType.Brick,2), new int[]{19,20});
		tradePorts.put(new SpecificPort(Catan.ResourceType.Wood,2), new int[]{29,31});
		tradePorts.put(new SpecificPort(Catan.ResourceType.Ore,2), new int[]{39,40});
		tradePorts.put(new GeneralPort(), new int[]{8,11});
		tradePorts.put(new GeneralPort(), new int[]{24,27});
		tradePorts.put(new GeneralPort(), new int[]{35,36});
		tradePorts.put(new GeneralPort(), new int[]{44,45});
		return tradePorts;
	}
	
	private static List<Catan.ResourceType> createResourceList() { 
		List<Catan.ResourceType> resources = new ArrayList<Catan.ResourceType>();
		addResource(resources, Catan.ResourceType.None, 1);
		addResource(resources, Catan.ResourceType.Wood, 4);
		addResource(resources, Catan.ResourceType.Brick, 3);
		addResource(resources, Catan.ResourceType.Wheat, 4);
		addResource(resources, Catan.ResourceType.Sheep, 4);
		addResource(resources, Catan.ResourceType.Ore, 3);
		Collections.shuffle(resources);
		
		return resources;
	}

}

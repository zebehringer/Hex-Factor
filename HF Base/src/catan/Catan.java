package catan;

import java.io.Serializable;

public class Catan {

	public enum ResourceType {
		None, Wood, Brick, Wheat, Sheep, Ore
	}

	public static BuildingType SETTLEMENT = new BuildingType() {

		@Override
		public String getName() {
			return "Settlement";
		}

		@Override
		public int yieldResources(Site s) {
			return 1;
		}
		
	};
	
	public static BuildingType CITY = new BuildingType() {

		@Override
		public String getName() {
			return "City";
		}

		@Override
		public int yieldResources(Site s) {
			return 2;
		}
		
	};

	public interface Game<S extends Site, B extends Board<S>> {
		void addPlayerListener(PlayerListener listener);
		void removePlayerListener(PlayerListener listener);
		void addProductBuildListener(BuildListener listener);
		void removeProductBuildListener(BuildListener listener);
		void addStateChangeListener(StateChangeListener listener);
		void removeStateChangeListener(StateChangeListener listener);
		
		Iterable<? extends PlayerInfo> getPlayers();
		B getBoard();
	}
	
	public static class ResourceCount implements Serializable {

		private static final long serialVersionUID = 1L;

		private Catan.ResourceType resourceType;
		private int count;

		public ResourceCount(Catan.ResourceType resourceType, int count) {
			this.resourceType = resourceType;
			this.count = count;
		}
		public Catan.ResourceType getResourceType() {
			return this.resourceType;
		}
		public int getCount() {
			return this.count;
		}
	}

	private static ResourceCount rrq(Catan.ResourceType type, int count) {
		return new ResourceCount(type,count);
	}
	
	public enum Product {
		ROAD(new String[]{"Road Start","Road End"},new ResourceCount[]{rrq(Catan.ResourceType.Brick,1),rrq(Catan.ResourceType.Wood,1)}),
		SETTLEMENT(new String[]{"Building Site"},new ResourceCount[]{rrq(Catan.ResourceType.Brick,1),rrq(Catan.ResourceType.Wood,1),rrq(Catan.ResourceType.Sheep,1),rrq(Catan.ResourceType.Wheat,1)}),
		CITY(new String[]{"Building Site"},new ResourceCount[]{rrq(Catan.ResourceType.Ore,3),rrq(Catan.ResourceType.Wheat,2)}),
		DEV_CARD(new String[]{},new ResourceCount[]{rrq(Catan.ResourceType.Ore,1),rrq(Catan.ResourceType.Sheep,1),rrq(Catan.ResourceType.Wheat,1)});
		private String[] siteRequirements;
		private ResourceCount[] resourceRequirements;

		private Product(String[] siteRequirements, ResourceCount[] resourceRequirements) {
			this.siteRequirements = siteRequirements;
			this.resourceRequirements = resourceRequirements;
		}

		public String[] getSiteRequirements() {
			return siteRequirements;
		}
		
		public ResourceCount[] getResourceRequirements() {
			return resourceRequirements;
		}
	}
}

package catan;

import catan.Catan.ResourceType;

public class ResourcesEarnedEvent {

	private PlayerInfo player;
	private ResourceType type;
	private int count;
	private Site source;

	public ResourcesEarnedEvent(PlayerInfo player, ResourceType type, int count, Site source) {
		this.player = player;
		this.type = type;
		this.count = count;
		this.source = source;
	}
	
	public PlayerInfo getPlayer() {
		return player;
	}

	public ResourceType getType() {
		return type;
	}

	public int getCount() {
		return count;
	}

	public Site getSource() {
		return source;
	}

}

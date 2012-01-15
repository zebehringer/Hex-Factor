package catan;

import catan.Catan.ResourceType;

public class ResourcesTakenEvent {

	private PlayerInfo player;
	private PlayerInfo victim;
	private ResourceType type;
	private int count;

	public ResourcesTakenEvent(PlayerInfo player, PlayerInfo victim, ResourceType type, int count) {
		this.player = player;
		this.victim = victim;
		this.type = type;
		this.count = count;
	}

	public PlayerInfo getPlayer() {
		return this.player;
	}

	public PlayerInfo getVictim() {
		return this.victim;
	}

	public ResourceType getType() {
		return this.type;
	}

	public int getCount() {
		return this.count;
	}
}

package catan;

import catan.Catan.ResourceType;

public class ResourcesExchangedEvent {

	private PlayerInfo player;
	private ResourceType given;
	private int givenCount;
	private ResourceType received;
	private int receivedCount;

	public ResourcesExchangedEvent(PlayerInfo player, ResourceType given, int givenCount, ResourceType received, int receivedCount) {
		this.player = player;
		this.given = given;
		this.givenCount = givenCount;
		this.received = received;
		this.receivedCount = receivedCount;
	}
	
	public PlayerInfo getPlayer() {
		return this.player;
	}

	public ResourceType getGiven() {
		return this.given;
	}

	public int getGivenCount() {
		return givenCount;
	}

	public ResourceType getReceived() {
		return this.received;
	}

	public int getReceivedCount() {
		return this.receivedCount;
	}

}

package catan;

import java.util.Map;

public interface Player extends PlayerInfo {

	public Iterable<? extends DevelopmentCard> getDevelopmentCards();

	public Map<Catan.ResourceType,Integer> getResources();

}

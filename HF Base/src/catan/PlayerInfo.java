package catan;

public interface PlayerInfo {

	int getNumber();

	String getUniqueId();

	String getName();

	Iterable<Road> getRoads();

	int countAllResources();

}

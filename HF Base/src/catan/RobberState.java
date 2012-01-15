package catan;

import java.util.Map;

public interface RobberState extends State {

	//allow query for how many resources should be relinquished by each player
	RobberOutcome moveRobber(Player player, Board.Tile tile,
			Site victimSite) throws IllegalStateException;

	void relinquishResources(Player player,
			Map<Catan.ResourceType, Integer> counts);

}
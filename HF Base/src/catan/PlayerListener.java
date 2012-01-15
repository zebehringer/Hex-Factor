/**
 * 
 */
package catan;

public interface PlayerListener<G> {
	void resourcesEarned(G game, ResourcesEarnedEvent event);
	void resourcesTaken(G game, ResourcesTakenEvent event);
	void resourcesExchanged(G game, ResourcesExchangedEvent event);
}
package vg.civcraft.mc.prisonpearl.managers;

import java.util.UUID;

import org.bukkit.entity.Player;

import vg.civcraft.mc.prisonpearl.PrisonPearlPlugin;
import vg.civcraft.mc.prisonpearl.database.interfaces.IPrisonPortaledStorage;

/**
 * This class is going to work a little differently then it used to and as such may seem
 * a little misleading.  With the introduction to BetterShards it is a little difficult
 * to truly know how a player got into a certain shard and because of runtime issues
 * we can't guarantee information given to us from BetterShards in time.
 * The solution to this is to add players who we know are pearled to the db and then from 
 * there if they are in the db and are not imprisoned we should kill them and have them
 * random spawn.
 * @author rourke750
 */
public class PrisonPortaledPlayerManager {
	
	private IPrisonPortaledStorage storage;
	
	public PrisonPortaledPlayerManager() {
		storage = PrisonPearlPlugin.getDBHandler().getStorageHandler().getPrisonPortaledStorage();
	}
	
	public boolean isPlayerPortaledToPrison(Player player) {
		return isPlayerPortaledToPrison(player.getUniqueId());
	}
	
    public boolean isPlayerPortaledToPrison(UUID playerid) {
		return storage.isPortaledPlayer(playerid);
	}
    
    public void addPlayerPortaled(UUID uuid) {
    	if (isPlayerPortaledToPrison(uuid))
    		return;
    	storage.addPortaledPlayer(uuid);
    }
    
    public void removePlayerPortaled(UUID uuid) {
    	storage.removePortaledPlayer(uuid);
    }
}

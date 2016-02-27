package vg.civcraft.mc.prisonpearl.managers;

import java.util.UUID;

import org.bukkit.entity.Player;

import vg.civcraft.mc.prisonpearl.PrisonPearlPlugin;
import vg.civcraft.mc.prisonpearl.database.interfaces.IPrisonPortaledStorage;

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
    	storage.addPortaledPlayer(uuid);
    }
    
    public void removePlayerPortaled(UUID uuid) {
    	storage.removePortaledPlayer(uuid);
    }
}

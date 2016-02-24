package vg.civcraft.mc.prisonpearl.database.interfaces;

import java.util.List;
import java.util.UUID;

public interface IPrisonPortaledStorage {

	public void addPortaledPlayer(UUID uuid);
	public void removePortaledPlayer(UUID uuid);
	public List<UUID> getAllPortaledPlayers();
	public boolean isPortaledPlayer(UUID uuid);
	public void save();
	public void load();
	
}

package vg.civcraft.mc.prisonpearl.database.interfaces;

import org.bukkit.Location;

public interface IWorldBorderStorage {

	public void save();
	public void load();
	public void addWorldBorder(Location loc);
	public void removeWorldBorder(Location loc);
	public boolean isWorldBorderLoc(Location loc);
}

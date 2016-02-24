package vg.civcraft.mc.prisonpearl.managers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.wimbli.WorldBorder.BorderData;
import com.wimbli.WorldBorder.Config;

import vg.civcraft.mc.prisonpearl.PrisonPearlConfig;
import vg.civcraft.mc.prisonpearl.PrisonPearlPlugin;
import vg.civcraft.mc.prisonpearl.database.interfaces.IWorldBorderStorage;

public class WorldBorderManager {

	private IWorldBorderStorage storage;

	public WorldBorderManager() {
		storage = PrisonPearlPlugin.getDBHandler().getStorageHandler().getWorldBorderStorage();
	}

	public boolean isMaxFeed(Location loc) {
		if (!PrisonPearlConfig.getShouldAutoFreeWorldBorder() || !PrisonPearlPlugin.isWorldBorderEnabled() || isOnWhiteList(loc)) {
			return false;
		}
		World world = loc.getWorld();
		BorderData border = Config.Border(world.getName());
		return !border.insideBorder(loc);
	}

	public boolean isOnWhiteList(Location loc) {
		return storage.isWorldBorderLoc(loc);
	}

	private Double stringToDouble(String str) throws NumberFormatException {
		return Double.parseDouble(str);
	}

	public boolean addWhitelistedLocation(Location loc) {
		if (isOnWhiteList(loc)) {
			return false;
		} else {
			storage.addWorldBorder(loc);
			return true;
		}
	}

	public boolean removeWhitelistedLocation(Location loc) {
		if (isOnWhiteList(loc)) {
			storage.removeWorldBorder(loc);
			return true;
		}
		return false;
	}

	public boolean addWhitelistedLocation(String world, String x, String y, String z) {
		try {
			Double xd = stringToDouble(x);
			Double yd = stringToDouble(y);
			Double zd = stringToDouble(z);
			return addWhitelistedLocation(new Location(Bukkit.getWorld(world), xd, yd, zd));
		} catch (NumberFormatException e) {
			return false;
		}

	}

	public boolean removeWhitelistedLocation(String world, String x, String y, String z) {
		try {
			Double xd = stringToDouble(x);
			Double yd = stringToDouble(y);
			Double zd = stringToDouble(z);
			if (removeWhitelistedLocation(new Location(Bukkit.getWorld(world), xd, yd, zd))) {
				return false;
			}
			return true;
		} catch (NumberFormatException e) {
			return false;
		}

	}
}

package vg.civcraft.mc.prisonpearl.managers;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import vg.civcraft.mc.prisonpearl.PrisonPearl;
import vg.civcraft.mc.prisonpearl.PrisonPearlPlugin;
import vg.civcraft.mc.prisonpearl.misc.FakeLocation;

import vg.civcraft.mc.bettershards.BetterShardsAPI;
import vg.civcraft.mc.bettershards.BetterShardsPlugin;
import vg.civcraft.mc.bettershards.events.PlayerChangeServerReason;
import vg.civcraft.mc.bettershards.misc.BedLocation;
import vg.civcraft.mc.bettershards.misc.PlayerStillDeadException;
import vg.civcraft.mc.bettershards.misc.TeleportInfo;

/**
 * Wraps BetterShards dependencies so main classes can load even if BetterShards isn't around.
 *
 * <i>please don't remove me this time, i matter</i>
 */
public class BetterShardsManager {

	private static boolean isBetterShardsEnabled;
	
	public BetterShardsManager() {
		isBetterShardsEnabled = PrisonPearlPlugin.isBetterShardsEnabled();
		if (!isBetterShardsEnabled) {
			return;
		}
	}

	public static void resetShardedBed(UUID imprisonedId, String server, String worldName) {
		if (!isBetterShardsEnabled) return;
		if (BetterShardsAPI.hasBed(imprisonedId)) {
			TeleportInfo info = new TeleportInfo(worldName, server, 0, 90, 0);
			BedLocation bed = new BedLocation(imprisonedId, info);
			BetterShardsAPI.addBedLocation(imprisonedId, bed);
		}
	}

	public static void freePearlCrossServer(PrisonPearl pp, String reason, String server) {
		if (!isBetterShardsEnabled) return;
		FakeLocation loc = (FakeLocation) pp.getLocation();
		// Raise pearl by 1 block.
		TeleportInfo info = new TeleportInfo(loc.getWorldName(), server, loc.getBlockX(),
				loc.getBlockY()+1, loc.getBlockZ());
		BetterShardsAPI.teleportPlayer(loc.getServerName(), pp.getImprisonedId(), info);
		try {
			BetterShardsAPI.connectPlayer(pp.getImprisonedPlayer(), server, PlayerChangeServerReason.PLUGIN);
		} catch (PlayerStillDeadException e) {
			PrisonPearlPlugin.getInstance().getLogger().log(Level.WARNING, "Player is still dead, cannot free", e);
		}
	}

	public static Location getRandomSpawn() {
		if (!isBetterShardsEnabled) return null;
		return BetterShardsPlugin.getRandomSpawn().getLocation();
	}
}

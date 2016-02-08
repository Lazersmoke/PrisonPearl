package vg.civcraft.mc.prisonpearl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.bettershards.BetterShardsAPI;
import vg.civcraft.mc.bettershards.events.PlayerChangeServerReason;
import vg.civcraft.mc.bettershards.misc.PlayerStillDeadException;
import vg.civcraft.mc.civmodcore.annotations.CivConfig;
import vg.civcraft.mc.civmodcore.annotations.CivConfigType;
import vg.civcraft.mc.mercury.MercuryAPI;
import vg.civcraft.mc.prisonpearl.database.interfaces.IPrisonPearlStorage;
import vg.civcraft.mc.prisonpearl.managers.MercuryManager;
import vg.civcraft.mc.prisonpearl.managers.PrisonPearlManager;
import vg.civcraft.mc.prisonpearl.managers.SummonManager;

public class PrisonPearlUtil {

	private static IPrisonPearlStorage pearls;
	private static PrisonPearlManager manager;
	private static SummonManager summon;
	
	public PrisonPearlUtil() {
		pearls = PrisonPearlPlugin.getDBHandler().getStorageHandler().getPrisonPearlStorage();
		manager = PrisonPearlPlugin.getPrisonPearlManager();
		summon = PrisonPearlPlugin.getSummonManager();
	}
	
	public static boolean respawnPlayerCorrectly(Player p) {
		return respawnPlayerCorrectly(p, null);
	}
	
	/**
	 * 
	 * @param p
	 * @param pp
	 * @return Return false if the player was not tpped in anyway.
	 */
	public static boolean respawnPlayerCorrectly(Player p, PrisonPearl pp) {
		// We want this method to deal with all cases: Respawn on death, Respawn on summoning, returning,
		// different shards transport, everything. 
		
		UUID uuid = p.getUniqueId();
		boolean freeToPearl = PrisonPearlConfig.shouldTpPearlOnFree();
		if (PrisonPearlPlugin.isBetterShardsEnabled() && PrisonPearlPlugin.isMercuryEnabled()) {
			if (pearls.isImprisoned(uuid)) {
				String server = MercuryAPI.serverName();
				String toServer = manager.getImprisonServer();
				if (!server.equals(toServer)) {
					if (PrisonPearlConfig.shouldPpsummonClearInventory()) {
						dropInventory(p, p.getLocation(), PrisonPearlConfig.shouldPpsummonLeavePearls());
					}
					try {
						Location loc = summon.
						String info = String.format("%s %s %s %d %d %d", p.getUniqueId().toString(), )
						BetterShardsAPI.teleportPlayer(info);
						BetterShardsAPI.connectPlayer(p, toServer, PlayerChangeServerReason.PLUGIN);
					} catch (PlayerStillDeadException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else {
					Location newLoc = manager.getPrisonSpawnLocation();
					p.teleport(newLoc);
				}
			}
			else if (pp != null && freeToPearl) {
				p.teleport(pp.getLocation());
			}
		}
		// This part will deal for when bettershards and mercury are not enabled.
		else if (pearls.isImprisoned(uuid)) {
			Location newLoc = manager.getPrisonSpawnLocation();
			p.teleport(newLoc);
		}
		else if (pp != null && freeToPearl) {
			p.teleport(pp.getLocation());
		}
	}
	
	public static void addCheckToReturnSummon(UUID uuid, Location loc) {
		checkToReturnSummon.put(uuid, loc);
	}
	
	public static Location getCheckToReturnSummon(UUID uuid) {
		return checkToReturnSummon.remove(uuid);
	}
	
	public static void addCheckToSummon(UUID uuid, Location loc) {
		checkToSummon.put(uuid,  loc);
	}
	
	public static Location getCheckToSummon(UUID uuid) {
		return checkToSummon.remove(uuid);
	}
	
	 /**
     * @param location - the location to serialize into user-friendly text.
     * @return the serialized user-friendly string representing location.
     */
    public static String serializeLocation(Location location) {
    	return String.format("%s, (%d, %d, %d)", location.getWorld().getName(),
    							location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
    
    public static void delayedTp(final Player player, final Location loc, final boolean dropInventory) {
		final boolean respawn = loc != null;
		final Location oldLoc = player.getLocation();
		if (respawn) {
			player.setHealth(0.0);
		}
		Bukkit.getScheduler().callSyncMethod(PrisonPearlPlugin.getInstance(), new Callable<Void>() {
			public Void call() {
				if (!respawn) {
					player.teleport(loc);
				}
				if (dropInventory) {
					dropInventory(player, oldLoc, PrisonPearlConfig.shouldPpsummonLeavePearls());
				}
				return null;
			}
		});
	}
    
    public static void dropInventory(Player player, Location loc, boolean leavePearls) {
		if (loc == null) {
			loc = player.getLocation();
		}
		World world = loc.getWorld();
		Inventory inv = player.getInventory();
		int end = inv.getSize();
		for (int i = 0; i < end; ++i) {
			ItemStack item = inv.getItem(i);
			if (item == null) {
				continue;
			}
			if (leavePearls && item.getType().equals(Material.ENDER_PEARL)
					&& item.getDurability() == 0) {
				continue;
			}
			inv.clear(i);
			world.dropItemNaturally(loc, item);
		}
	}
}

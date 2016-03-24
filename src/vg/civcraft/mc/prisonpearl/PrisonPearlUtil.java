package vg.civcraft.mc.prisonpearl;

import java.util.Random;
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
import vg.civcraft.mc.bettershards.misc.BedLocation;
import vg.civcraft.mc.bettershards.misc.PlayerStillDeadException;
import vg.civcraft.mc.bettershards.misc.TeleportInfo;
import vg.civcraft.mc.mercury.MercuryAPI;
import vg.civcraft.mc.prisonpearl.database.interfaces.IPrisonPearlStorage;
import vg.civcraft.mc.prisonpearl.managers.PrisonPearlManager;
import vg.civcraft.mc.prisonpearl.managers.PrisonPortaledPlayerManager;
import vg.civcraft.mc.prisonpearl.managers.SummonManager;
import vg.civcraft.mc.prisonpearl.misc.FakeLocation;

public class PrisonPearlUtil {

	private static PrisonPearlManager manager;
	private static SummonManager summon;
	private static PrisonPortaledPlayerManager portaled;
	
	private static Thread mainThread;
	
	public PrisonPearlUtil() {
		manager = PrisonPearlPlugin.getPrisonPearlManager();
		summon = PrisonPearlPlugin.getSummonManager();
		mainThread = Thread.currentThread();
	}
	
	public static boolean respawnPlayerCorrectly(Player p) {
		return respawnPlayerCorrectly(p, null);
	}
	
	/**
	 * 
	 * @param p
	 * @param pp
	 * @param passPearl Will mainly just deal if a player was just freed.
	 * @return Return false if the player was not tpped in anyway.
	 */
	public static boolean respawnPlayerCorrectly(Player p, PrisonPearl passPearl) { 
		// We want this method to deal with all cases: Respawn on death, Respawn on summoning, returning,
		// different shards transport, everything. 
		
		UUID uuid = p.getUniqueId();
		boolean freeToPearl = PrisonPearlConfig.shouldTpPearlOnFree();
		PrisonPearl pp = manager.getByImprisoned(uuid);
		if (PrisonPearlPlugin.isBetterShardsEnabled() && PrisonPearlPlugin.isMercuryEnabled()) {
			if (manager.isImprisoned(uuid)) {
				String server = MercuryAPI.serverName();
				String toServer = manager.getImprisonServer();
				// This check is incase the player is being summoned;
				if (summon.isSummoned(p) && (
						(summon.getSummon(p).isToBeReturned() && (summon.getSummon(p).getReturnLocation() instanceof FakeLocation)) 
						|| summon.getSummon(p).isJustCreated())
						&& toServer.equals(server)) {
					if (pp.getLocation() instanceof FakeLocation)
						toServer = ((FakeLocation) pp.getLocation()).getServerName();
				}
				if (!server.equals(toServer)) {
					try {
						FakeLocation loc = null;
						if (summon.isSummoned(p)) {
							Summon s = summon.getSummon(p);
							if (s.isToBeReturned()) {
								if (PrisonPearlConfig.getShouldPPReturnKill())
									p.setHealth(0);
								loc = (FakeLocation) s.getReturnLocation();
								TeleportInfo info = new TeleportInfo(loc.getWorldName(), loc.getServerName(), loc.getBlockX(), loc.getBlockY() + 1,
										loc.getBlockZ());
								BetterShardsAPI.teleportPlayer(info.getServer(), p.getUniqueId(), info);
								return BetterShardsAPI.connectPlayer(p, toServer, PlayerChangeServerReason.PLUGIN);
							}
							else if (s.isJustCreated()) {
								if (PrisonPearlConfig.shouldPpsummonClearInventory()) {
									dropInventory(p, p.getLocation(), PrisonPearlConfig.shouldPpsummonLeavePearls());
								}
								loc = (FakeLocation) s.getPearlLocation();
								TeleportInfo info = new TeleportInfo(loc.getWorldName(), loc.getServerName(), loc.getBlockX(), loc.getBlockY(),
										loc.getBlockZ());
								BetterShardsAPI.teleportPlayer(info.getServer(), p.getUniqueId(), info);
								return BetterShardsAPI.connectPlayer(p, toServer, PlayerChangeServerReason.PLUGIN);
							}
						}
						
						BetterShardsAPI.randomSpawnPlayer(toServer, p.getUniqueId());
						return BetterShardsAPI.connectPlayer(p, toServer, PlayerChangeServerReason.PLUGIN);
					} catch (PlayerStillDeadException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else { // For if pearl is on the same server.
					if (summon.isSummoned(p)) {
						Summon s = summon.getSummon(p);
						if (s.isToBeReturned()) {
							if (PrisonPearlConfig.getShouldPPReturnKill())
								p.setHealth(0);
							Location newLoc = s.getReturnLocation();
							newLoc.setY(newLoc.getY() + 1);
							p.teleport(newLoc);
						}
						else if (s.isJustCreated()) {
							if (PrisonPearlConfig.shouldPpsummonClearInventory()) {
								dropInventory(p, p.getLocation(), PrisonPearlConfig.shouldPpsummonLeavePearls());
							}
							p.teleport(s.getPearlLocation());
						}
					}
					else if (!p.getWorld().equals(manager.getPrisonSpawnLocation().getWorld()))
						p.teleport(manager.getPrisonSpawnLocation());
					return true;
				}
			}
			try {
				if (freeToPearl && passPearl != null && passPearl.getLocation() instanceof FakeLocation) { // Player was just freed and should be returned to his holder.
					FakeLocation loc = (FakeLocation) passPearl.getLocation();
					TeleportInfo info = new TeleportInfo(loc.getWorldName(), loc.getServerName(), loc.getBlockX(), loc.getBlockY(), 
							loc.getBlockZ());
					BetterShardsAPI.teleportPlayer(loc.getServerName(), uuid, info);
					return BetterShardsAPI.connectPlayer(p, loc.getServerName(), PlayerChangeServerReason.PLUGIN);
				}
				else if (passPearl != null){ // Some other cases.
					if (BetterShardsAPI.hasBed(uuid)) { // Like if a player has a bed ;)
						BedLocation bed = BetterShardsAPI.getBedLocation(uuid);
						BetterShardsAPI.teleportPlayer(bed.getServer(), uuid, bed.getTeleportInfo());
						return BetterShardsAPI.connectPlayer(p, bed.getServer(), PlayerChangeServerReason.PLUGIN);
					}
					// Randomly respawn the player on the server where the pearl was located.
					Random r = new Random();
					int num = r.nextInt(MercuryAPI.getAllConnectedServers().size());
					BetterShardsAPI.randomSpawnPlayer((String) MercuryAPI.getAllConnectedServers().toArray()[num], uuid);
					return true;
				}
			} catch (PlayerStillDeadException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}
		// This part will deal for when bettershards and mercury are not enabled.
		// This still needs work on, should get reports from non mercury/ BetterShards Servers and they should say whats wrong.
		else if (manager.isImprisoned(uuid)) {
			Location newLoc = manager.getPrisonSpawnLocation();
			p.teleport(newLoc);
			return true;
		}
		else if (pp != null && freeToPearl) {
			p.teleport(pp.getLocation());
			return true;
		}
		return false;
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
		final World world = loc.getWorld();
		Inventory inv = player.getInventory();
		int end = inv.getSize();
		for (int i = 0; i < end; ++i) {
			final ItemStack item = inv.getItem(i);
			if (item == null) {
				continue;
			}
			if (leavePearls && item.getType().equals(Material.ENDER_PEARL)
					&& item.getDurability() == 0) {
				continue;
			}
			inv.clear(i);
			if (!isMainThread(Thread.currentThread())) {
				final Location l = loc;
				Bukkit.getScheduler().runTask(PrisonPearlPlugin.getInstance(), new Runnable() {

					@Override
					public void run() {
						world.dropItemNaturally(l, item);
					}
					
				});
			}
			else
				world.dropItemNaturally(loc, item);
		}
	}
    
    public static boolean isMainThread(Thread t) {
    	return t.equals(mainThread);
    }
}
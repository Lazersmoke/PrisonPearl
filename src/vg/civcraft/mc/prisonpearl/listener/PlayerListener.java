package vg.civcraft.mc.prisonpearl.listener;

import java.util.UUID;
import java.util.concurrent.Callable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;

import net.minelink.ctplus.compat.api.NpcIdentity;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.prisonpearl.PrisonPearl;
import vg.civcraft.mc.prisonpearl.PrisonPearlPlugin;
import vg.civcraft.mc.prisonpearl.PrisonPearlUtil;
import vg.civcraft.mc.prisonpearl.managers.BanManager;
import vg.civcraft.mc.prisonpearl.managers.CombatTagManager;
import vg.civcraft.mc.prisonpearl.managers.PrisonPearlManager;
import vg.civcraft.mc.prisonpearl.managers.SummonManager;

import static vg.civcraft.mc.prisonpearl.PrisonPearlUtil.*;

public class PlayerListener implements Listener {

	private static final Location RESPAWN_PLAYER = new Location(null, 0, 0, 0);
	private static PrisonPearlManager pearls;
	private static CombatTagManager combatManager;
	private static BanManager ban;
	private static SummonManager summon;

	public PlayerListener() {
		Bukkit.getPluginManager().registerEvents(this, PrisonPearlPlugin.getInstance());
		pearls = PrisonPearlPlugin.getPrisonPearlManager();
		ban = PrisonPearlPlugin.getBanManager();
		summon = PrisonPearlPlugin.getSummonManager();
		combatManager = PrisonPearlPlugin.getCombatTagManager();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		pearls.updateAttachment(player);
		ban.checkBan(player.getUniqueId());
		UUID uuid = player.getUniqueId();

		// Incase a player comes from another server and has a pearl.
		for (ItemStack stack : player.getInventory().getContents()) {
			if (stack == null)
				continue;
			PrisonPearl pp = pearls.getPearlByItemStack(stack);
			if (pp == null)
				continue;
			pp.setHolder(player);
		}
		
		if (player.isDead())
			return;

		PrisonPearl pp = pearls.getByImprisoned(player);
		boolean shouldTP = respawnPlayerCorrectly(player, pp);
		if (shouldTP && pp == null) {
			player.sendMessage("While away, you were freed!");
		} else if (pp != null) {
			prisonMotd(player);
		}
	}

	// don't let people escape through the end portal
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerPortalEvent(PlayerPortalEvent event) {
		Player player = event.getPlayer();

		if (pearls.isImprisoned(player) && !summonman.isSummoned(player)) { // if
																			// in
																			// prison
																			// but
																			// not
																			// imprisoned
			Location toLoc = event.getTo();
			if (toLoc != null && toLoc.getWorld() != getPrisonWorld()) {
				prisonMotd(player);
				delayedTp(player, getPrisonSpawnLocation(), false);
			}
		}
	}

	// remove permission attachments and record the time players log out
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event) {
		PermissionAttachment attachment = pearls.removeAttachments(event.getPlayer());
		if (attachment != null)
			attachment.remove();
	}

	// adjust spawnpoint if necessary
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		prisonMotd(event.getPlayer());
		Location newloc = getRespawnLocation(event.getPlayer(), event.getRespawnLocation());
		if (newloc != null && newloc != RESPAWN_PLAYER)
			event.setRespawnLocation(newloc);
	}

	// called when a player joins or spawns
	private void prisonMotd(Player player) {
		if (pearls.isImprisoned(player) && !summonman.isSummoned(player)) 
			for (String line : getConfig().getStringList("prison_motd"))
				player.sendMessage(line);
			player.sendMessage(pearls.getByImprisoned(player).getMotd());
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onEntityDeath(EntityDeathEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;
		
		Player player = (Player)event.getEntity();
		UUID uuid = player.getUniqueId();
		String playerName = player.getName();
		
		if (combatTagManager.isCombatTagNPC(event.getEntity()))  {
			playerName = player.getName();
			// UUID being passed isn't the right one.
			uuid = NameAPI.getUUID(playerName);
			//String realName = combatTagManager.getNPCPlayerName(player);
			log.info("NPC Player: "+playerName+", ID: "+ uuid);
//			if (!realName.equals("")) {
//				playerName = realName;
//			}
		}
		else if (combatTagManager.isCombatTagPlusNPC(player)){
			NpcIdentity iden = combatTagManager.getCombatTagPlusNPCIdentity(player);
			uuid = iden.getId();
			playerName = iden.getName();
			log.info("NPC Player: " + playerName + ", ID: " + uuid);
		} else if (combatTagManager.isEnabled() && !combatTagManager.isCombatTagged(player)) {
			log.info("Player: " + playerName + " is out of combatTag, immune from pearling.");
			return;
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerQuitforBroadcast(PlayerQuitEvent event) {
		
	}
}

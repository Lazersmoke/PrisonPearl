package vg.civcraft.mc.prisonpearl.listener;

import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
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
import vg.civcraft.mc.prisonpearl.PrisonPearlConfig;
import vg.civcraft.mc.prisonpearl.PrisonPearlPlugin;
import vg.civcraft.mc.prisonpearl.PrisonPearlUtil;
import vg.civcraft.mc.prisonpearl.managers.BanManager;
import vg.civcraft.mc.prisonpearl.managers.CombatTagManager;
import vg.civcraft.mc.prisonpearl.managers.DamageLogManager;
import vg.civcraft.mc.prisonpearl.managers.PrisonPearlManager;
import vg.civcraft.mc.prisonpearl.managers.SummonManager;

import static vg.civcraft.mc.prisonpearl.PrisonPearlUtil.*;

public class PlayerListener implements Listener {

	private static final Location RESPAWN_PLAYER = new Location(null, 0, 0, 0);
	private static PrisonPearlManager pearls;
	private static CombatTagManager combatManager;
	private static BanManager ban;
	private static SummonManager summon;
	private static DamageLogManager dlManager;

	public PlayerListener() {
		Bukkit.getPluginManager().registerEvents(this, PrisonPearlPlugin.getInstance());
		pearls = PrisonPearlPlugin.getPrisonPearlManager();
		ban = PrisonPearlPlugin.getBanManager();
		summon = PrisonPearlPlugin.getSummonManager();
		combatManager = PrisonPearlPlugin.getCombatTagManager();
		dlManager = PrisonPearlPlugin.getDamageLogManager();
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

		if (pearls.isImprisoned(player) && !summon.isSummoned(player)) { // if in prison but not imprisoned
			respawnPlayerCorrectly(player);
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
		respawnPlayerCorrectly(event.getPlayer());
	}

	// called when a player joins or spawns
	private void prisonMotd(Player player) {
		if (pearls.isImprisoned(player) && !summon.isSummoned(player)) {
			for (String line : PrisonPearlConfig.getPrisonMotd())
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
		
		if (combatManager.isCombatTagNPC(event.getEntity()))  {
			playerName = player.getName();
			// UUID being passed isn't the right one.
			uuid = NameAPI.getUUID(playerName);
			//String realName = combatTagManager.getNPCPlayerName(player);
			PrisonPearlPlugin.log("NPC Player: "+playerName+", ID: "+ uuid);
//			if (!realName.equals("")) {
//				playerName = realName;
//			}
		}
		else if (combatManager.isCombatTagPlusNPC(player)){
			NpcIdentity iden = combatManager.getCombatTagPlusNPCIdentity(player);
			uuid = iden.getId();
			playerName = iden.getName();
			PrisonPearlPlugin.log("NPC Player: " + playerName + ", ID: " + uuid);
		} else if (combatManager.isEnabled() && !combatManager.isCombatTagged(player)) {
			PrisonPearlPlugin.log("Player: " + playerName + " is out of combatTag, immune from pearling.");
			return;
		}
		
		PrisonPearl pp = pearls.getByImprisoned(uuid); // find out if the player is imprisoned
		if (pp != null) { // if imprisoned
			if (!PrisonPearlConfig.getAllowPrisonStealing() || player.getLocation().getWorld() == pearls.getImprisonWorld()) {// bail if prisoner stealing isn't allowed, or if the player is in prison (can't steal prisoners from prison ever)
				// reveal location of pearl to damaging players if pearl stealing is disabled
				for (Player damager : dlManager.getDamagers(player)) {
					damager.sendMessage(ChatColor.GREEN+"[PrisonPearl] "+playerName+" cannot be pearled here because they are already "+pp.describeLocation());
				}
				return;
			}
		}
		
		for (Player damager : dlManager.getDamagers(player)) { // check to see if anyone can imprison him
			if (pp != null && pp.getHolderPlayer() == damager) // if this damager has already imprisoned this person
				break; // don't be confusing and re-imprison him, just let him die
			
			int firstpearl = Integer.MAX_VALUE; // find the first regular enderpearl in their inventory
			for (Entry<Integer, ? extends ItemStack> entry : damager.getInventory().all(Material.ENDER_PEARL).entrySet()) {
				ItemStack stack = entry.getValue();
				if (!stack.hasItemMeta())
					firstpearl = Math.min(entry.getKey(), firstpearl);
			}
			
			if (firstpearl == Integer.MAX_VALUE) // no pearl
				continue; // no imprisonment
			
			if (PrisonPearlConfig.getMustPrisonPearlHotBar() && firstpearl > 9) // bail if it must be in the hotbar
				continue; 
				
			if (pearls.imprisonPlayer(uuid, damager)) // otherwise, try to imprison
				break;
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerQuitforBroadcast(PlayerQuitEvent event) {
		
	}
}

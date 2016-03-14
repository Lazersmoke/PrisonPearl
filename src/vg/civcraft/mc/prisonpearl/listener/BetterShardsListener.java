package vg.civcraft.mc.prisonpearl.listener;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import vg.civcraft.mc.bettershards.events.PlayerArrivedChangeServerEvent;
import vg.civcraft.mc.bettershards.events.PlayerChangeServerEvent;
import vg.civcraft.mc.bettershards.events.PlayerChangeServerReason;
import vg.civcraft.mc.prisonpearl.PrisonPearlPlugin;
import vg.civcraft.mc.prisonpearl.managers.PrisonPearlManager;
import vg.civcraft.mc.prisonpearl.managers.PrisonPortaledPlayerManager;

/**
 * The point of this class is to catch if BetterShards is trying to random spawn a player when 
 * we are trying to send them to the Prison World.
 * @author rourke750
 *
 */
public class BetterShardsListener implements Listener{

	private PrisonPearlManager pearls;
	private PrisonPortaledPlayerManager manager;
	
	public BetterShardsListener() {
		if (!PrisonPearlPlugin.isBetterShardsEnabled())
			return;
		Bukkit.getPluginManager().registerEvents(this, PrisonPearlPlugin.getInstance());
		pearls = PrisonPearlPlugin.getPrisonPearlManager();
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerTransferServerEvent(PlayerChangeServerEvent event) {
		UUID uuid = event.getPlayerUUID();
		PlayerChangeServerReason reason = event.getReason();
		if ((reason == PlayerChangeServerReason.RANDOMSPAWN || 
				reason == PlayerChangeServerReason.PORTAL || 
				reason == PlayerChangeServerReason.BED)
				&& pearls.isImprisoned(uuid))
			event.setCancelled(true);
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerJoin(PlayerArrivedChangeServerEvent event) {
		// Only need to worry about this for sharding.
		Player player = event.getPlayer();
		if (pearls.isImprisoned(player))
			return;
		Location toLoc = event.getPlayer().getLocation();
		if (toLoc.getWorld() == pearls.getImprisonWorld())
			manager.addPlayerPortaled(player.getUniqueId());
		else{
			manager.removePlayerPortaled(player.getUniqueId());
		}
	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerQuit(PlayerChangeServerEvent event) {
		UUID uuid = event.getPlayerUUID();
		if (pearls.isImprisoned(uuid))
			return;
		Location toLoc = Bukkit.getPlayer(uuid).getLocation();
		if (toLoc.getWorld() == pearls.getImprisonWorld()) {
			manager.removePlayerPortaled(uuid);
		}
	}
}

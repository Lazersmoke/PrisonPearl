package vg.civcraft.mc.prisonpearl.listener;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import vg.civcraft.mc.bettershards.events.PlayerChangeServerEvent;
import vg.civcraft.mc.bettershards.events.PlayerChangeServerReason;
import vg.civcraft.mc.prisonpearl.PrisonPearlPlugin;
import vg.civcraft.mc.prisonpearl.managers.PrisonPearlManager;

/**
 * The point of this class is to catch if BetterShards is trying to random spawn a player when 
 * we are trying to send them to the Prison World.
 * @author rourke750
 *
 */
public class BetterShardsListener implements Listener{

	private PrisonPearlManager pearls;
	
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
				reason == PlayerChangeServerReason.PORTAL)
				&& pearls.isImprisoned(uuid))
			event.setCancelled(true);
	}
}

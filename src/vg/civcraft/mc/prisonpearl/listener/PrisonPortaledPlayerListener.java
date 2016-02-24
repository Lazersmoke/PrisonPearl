package vg.civcraft.mc.prisonpearl.listener;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import vg.civcraft.mc.prisonpearl.PrisonPearlPlugin;
import vg.civcraft.mc.prisonpearl.events.PrisonPearlEvent;
import vg.civcraft.mc.prisonpearl.managers.PrisonPearlManager;
import vg.civcraft.mc.prisonpearl.managers.PrisonPortaledPlayerManager;

public class PrisonPortaledPlayerListener implements Listener{

	private PrisonPearlManager pearls;
	private PrisonPortaledPlayerManager manager;
	
	public PrisonPortaledPlayerListener() {
		Bukkit.getPluginManager().registerEvents(this, PrisonPearlPlugin.getInstance());
		pearls = PrisonPearlPlugin.getPrisonPearlManager();
		manager = PrisonPearlPlugin.getPrisonPortaledPlayerManager();
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		if (pearls.isImprisoned(player.getUniqueId()))
			return;
		
		if (event.getRespawnLocation().getWorld() != pearls.getImprisonWorld()) {
			manager.removePlayerPortaled(player.getUniqueId());
			
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerPortalEvent(PlayerPortalEvent event) {
		Player player = event.getPlayer();
		if (pearls.isImprisoned(player.getUniqueId()))
			return;
		Location toLoc = event.getTo();
		if (toLoc == null) {
			return;
		}
		if (toLoc.getWorld() == pearls.getImprisonWorld())
			manager.addPlayerPortaled(player.getUniqueId());
		else{
			manager.removePlayerPortaled(player.getUniqueId());
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPrisonPearlEvent(PrisonPearlEvent event) {
		if (event.getType() == PrisonPearlEvent.Type.NEW) {
			UUID uuid = event.getPrisonPearl().getImprisonedId();
			manager.removePlayerPortaled(uuid);
		}
	}
}

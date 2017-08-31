package vg.civcraft.mc.prisonpearl.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import vg.civcraft.mc.prisonpearl.PrisonPearlPlugin;
import vg.civcraft.mc.prisonpearl.managers.SummonManager;

public class SummonListener implements Listener{

	private SummonManager summon = PrisonPearlPlugin.getSummonManager();
	
	public SummonListener() {
		Bukkit.getPluginManager().registerEvents(this, PrisonPearlPlugin.getInstance());
	}
	
	public void onPlayerDeathEvent(EntityDeathEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;
		Player p = (Player) event.getEntity();
		if (summon.isSummoned(p))
			summon.getSummon(p).setToBeReturned(true);
	}
	
	@EventHandler(priority=EventPriority.NORMAL)
    public void onPlayerChatEvent(AsyncPlayerChatEvent event) {
        if (summon.isSummoned(event.getPlayer()) && !summon.getSummon(event.getPlayer()).getCanSpeak()) {
           event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.NORMAL)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player)event.getDamager();

        if(summon.isSummoned(player) && !summon.getSummon(player).getCanDamage()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.NORMAL)
    public void onBlockBreakEvent(BlockBreakEvent event) {

        Player player = event.getPlayer();

        if(summon.isSummoned(player) && !summon.getSummon(player).getCanBreak()) {
            event.setCancelled(true);
        }
    }
}

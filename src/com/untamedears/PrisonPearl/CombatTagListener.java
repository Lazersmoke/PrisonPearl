package com.untamedears.PrisonPearl;

import java.util.UUID;

import net.minelink.ctplus.Npc;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.trc202.CombatTag.libs.npclib.NPC;
import com.trc202.CombatTagEvents.NpcDespawnEvent;
import com.trc202.CombatTagEvents.NpcDespawnReason;
import com.untamedears.PrisonPearl.managers.PrisonPearlManager;

class CombatTagListener implements Listener {
    final PrisonPearlManager pearlman_;

    public CombatTagListener(final PrisonPearlPlugin plugin,
            final PrisonPearlManager pearlman) {
        this.pearlman_ = pearlman;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onNpcDespawn(NpcDespawnEvent event) {
        if (event.getReason() != NpcDespawnReason.DESPAWN_TIMEOUT) {
            return;
        }
        UUID plruuid = event.getPlayerUUID();
        NPC npc = event.getNpc();
        Location loc = npc.getEntity().getLocation();

        pearlman_.handleNpcDespawn(plruuid, loc);
    }
    
    @EventHandler
    public void onNpcDespawnPlus(net.minelink.ctplus.event.NpcDespawnEvent event){
    	net.minelink.ctplus.event.NpcDespawnReason reason = event.getDespawnReason();
    	Npc npc = event.getNpc();
    	Player p = npc.getEntity();
    	Location loc = p.getLocation();
    	if (reason == net.minelink.ctplus.event.NpcDespawnReason.DESPAWN){
        	pearlman_.handleNpcDespawn(p.getUniqueId(), loc);
    	}
    }
}
package vg.civcraft.mc.prisonpearl.listener;

import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.mojang.authlib.GameProfile;
import com.trc202.CombatTag.libs.npclib.NPC;
import com.trc202.CombatTagEvents.NpcDespawnEvent;
import com.trc202.CombatTagEvents.NpcDespawnReason;

import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.PlayerInteractManager;
import net.minelink.ctplus.Npc;
import vg.civcraft.mc.bettershards.BetterShardsPlugin;
import vg.civcraft.mc.prisonpearl.PrisonPearl;
import vg.civcraft.mc.prisonpearl.PrisonPearlPlugin;
import vg.civcraft.mc.prisonpearl.managers.CombatTagManager;
import vg.civcraft.mc.prisonpearl.managers.PrisonPearlManager;

public class CombatTagListener implements Listener{

	private PrisonPearlManager pearls;
	
	public CombatTagListener() {
		Bukkit.getPluginManager().registerEvents(this, PrisonPearlPlugin.getInstance());
		pearls = PrisonPearlPlugin.getPrisonPearlManager();
	}
	
	// Called from CombatTagListener.onNpcDespawn
	public void handleNpcDespawn(UUID plruuid, Location loc) {
		World world = loc.getWorld();
		Player player = Bukkit.getServer().getPlayer(plruuid);
		if (player == null) { // If player is offline
			MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
			GameProfile prof = new GameProfile(plruuid, null);
			EntityPlayer entity = new EntityPlayer(
				server, server.getWorldServer(0), prof,
				new PlayerInteractManager(server.getWorldServer(0)));
			player = (entity == null) ? null : (Player) entity.getBukkitEntity();
			if (player == null) {
				return;
			}
			player.loadData();
		}

		Inventory inv = player.getInventory();
		int end = inv.getSize();
		for (int slot = 0; slot < end; ++slot) {
			ItemStack item = inv.getItem(slot);
			if (item == null) {
				continue;
			}
			if (!item.getType().equals(Material.ENDER_PEARL)) {
			   continue;
			}
			PrisonPearl pp = pearls.getPearlByItemStack(item);
			if (pp==null){
				continue;
			}
			inv.clear(slot);
			world.dropItemNaturally(loc, item);  // drops pearl to ground.
		}
		player.saveData();
	}
	
    @EventHandler
    public void onNpcDespawn(NpcDespawnEvent event) {
        if (event.getReason() != NpcDespawnReason.DESPAWN_TIMEOUT) {
            return;
        }
        UUID plruuid = event.getPlayerUUID();
        NPC npc = event.getNpc();
        Location loc = npc.getEntity().getLocation();

        handleNpcDespawn(plruuid, loc);
    }
    
    @EventHandler
    public void onNpcDespawnPlus(net.minelink.ctplus.event.NpcDespawnEvent event){
    	net.minelink.ctplus.event.NpcDespawnReason reason = event.getDespawnReason();
    	Npc npc = event.getNpc();
    	Player p = npc.getEntity();
    	Location loc = p.getLocation();
    	if (reason == net.minelink.ctplus.event.NpcDespawnReason.DESPAWN){
        	handleNpcDespawn(p.getUniqueId(), loc);
    	}
    }
	
}

package vg.civcraft.mc.prisonpearl.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import vg.civcraft.mc.prisonpearl.DamageLog;
import vg.civcraft.mc.prisonpearl.PrisonPearlPlugin;

public class DamageLogManager implements Runnable{
	
	private PrisonPearlPlugin plugin;
	private boolean scheduled;
	private final Map<String, DamageLog> logs;
	
	public DamageLogManager() {
		plugin = PrisonPearlPlugin.getInstance();
		
		scheduled = false;
		logs = new HashMap<String, DamageLog>();
	}
	
	public List<Player> getDamagers(Player player) {
		DamageLog log = logs.get(player.getName());
		if (log != null)
			return log.getDamagers(plugin.getConfig().getInt("damagelog_min"));
		else
			return new ArrayList<Player>();
	}
	
	public void removeDamage(String name) {
		logs.remove(name);
	}
	
	public boolean hasDamageLog(Player player) {
		return logs.containsKey(player.getName());
	}
	
	public void recordDamage(Player player, Player damager, double amt) {
		DamageLog log = logs.get(player.getName());
		if (log == null) {
			log = new DamageLog(player);
			logs.put(player.getName(), log);
		}
		
		long ticks = plugin.getConfig().getInt("damagelog_ticks");
		log.recordDamage(damager, (int)amt, getNowTick() + ticks);
		scheduleExpireTask(ticks);
	}
	
	public void run() {
		scheduled = false;
		
		long nowtick = getNowTick();
		
		Iterator<DamageLog> i = logs.values().iterator();
		long minremaining = Long.MAX_VALUE;
		while (i.hasNext()) {
			DamageLog log = i.next();
			long remaining = nowtick-log.getExpiresTick();
			
			if (remaining <= plugin.getConfig().getInt("damagelog_ticks")/20) {
				i.remove();
				continue;
			}

			minremaining = Math.min(minremaining, remaining);
		}
		
		if (minremaining < Long.MAX_VALUE)
			scheduleExpireTask(minremaining);
	}
	
	private void scheduleExpireTask(long ticks) {
		if (scheduled)
			return;
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this, ticks);
		scheduled = true;
	}
	
	private long getNowTick() {
		return Bukkit.getWorlds().get(0).getFullTime();
	}
}

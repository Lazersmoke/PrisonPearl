package vg.civcraft.mc.prisonpearl.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import vg.civcraft.mc.mercury.MercuryAPI;
import vg.civcraft.mc.mercury.events.AsyncPluginBroadcastMessageEvent;
import vg.civcraft.mc.prisonpearl.PrisonPearlPlugin;

public class MercuryListener implements Listener{
	
	private final String channel = "PrisonPearl";
	
	public MercuryListener() {
		Bukkit.getPluginManager().registerEvents(this, PrisonPearlPlugin.getInstance());
		MercuryAPI.registerPluginMessageChannel(channel);
	}
	
	@EventHandler()
	public void mercuryListener(AsyncPluginBroadcastMessageEvent event) {
		String channel = event.getChannel();
		if (!this.channel.equals(channel))
			return;
	}

}

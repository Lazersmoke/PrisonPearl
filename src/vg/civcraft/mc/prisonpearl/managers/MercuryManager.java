package vg.civcraft.mc.prisonpearl.managers;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import vg.civcraft.mc.mercury.MercuryAPI;
import vg.civcraft.mc.prisonpearl.PrisonPearl;
import vg.civcraft.mc.prisonpearl.PrisonPearlPlugin;
import vg.civcraft.mc.prisonpearl.database.interfaces.IPrisonPearlStorage;
import vg.civcraft.mc.prisonpearl.events.PrisonPearlEvent;
import vg.civcraft.mc.prisonpearl.misc.FakeLocation;

public class MercuryManager {

	private final static String channel = "PrisonPearl";
	private static boolean isMercuryEnabled;
	private static IPrisonPearlStorage storage;
	
	public MercuryManager() {
		isMercuryEnabled = PrisonPearlPlugin.isMercuryEnabled();
		if (!isMercuryEnabled)
			return;
		storage = PrisonPearlPlugin.getDBHandler().getStorageHandler().getPrisonPearlStorage();
		Bukkit.getScheduler().scheduleSyncRepeatingTask(PrisonPearlPlugin.getInstance(), new Runnable() {

			@Override
			public void run() {
				updateAllPearlLocations();
			}
			
		}, 1200, 1200);
	}
	
	private static void updateAllPearlLocations(){
		if (!isMercuryEnabled)
			return;
    	Collection<PrisonPearl> pearls = storage.getAllPearls();
    	for (PrisonPearl pp: pearls){
    		Location loc = pp.getLocation();
    		
    		if (loc instanceof FakeLocation)
    			continue; // If it isn't your pearl don't worry about it.  The server that has it will send the messages.
    		
    		String playerName = null;
    		if (pp.getHolderPlayer() == null)
    			playerName = pp.getHolderPlayer().getDisplayName();
    		String message = pp.getImprisonedId().toString() + "|" + loc.getWorld().getName() + "|" + loc.getBlockX() + "|" + loc.getBlockY() + "|" + loc.getBlockZ() + "|" +
    				pp.getUniqueIdentifier() + "|" + playerName + "|" + pp.getMotd();
    		MercuryAPI.sendGlobalMessage(message, channel);
    	}
    }
	
	public static void updateTransferToMercury(UUID imprisoner, UUID pearled) {
		if (!isMercuryEnabled)
			return;
		String message = "";
		message += imprisoner.toString() + "|" + pearled.toString();
		MercuryAPI.sendGlobalMessage(message, channel);
	}
	
	public static void updateLocationToMercury(PrisonPearl pp, PrisonPearlEvent.Type type){
		if (!isMercuryEnabled)
			return;
		Location loc = pp.getLocation();
		String playerName = null;
		if (pp.getHolderPlayer() != null)
			playerName = pp.getHolderPlayer().getDisplayName();
		UUID uuid = pp.getImprisonedId();
		String message = uuid.toString() + "|" + loc.getWorld().getName() + "|" + loc.getBlockX() + "|" + loc.getBlockY() + "|" + loc.getBlockZ() + "|" +
				pp.getUniqueIdentifier() + "|" + playerName + "|" + pp.getMotd();
		
		if (type.equals(PrisonPearlEvent.Type.FREED) && PrisonPearlPlugin.getInstance().GetConfig()
				.get("free_tppearl").getBool())
			message += "|" + MercuryAPI.serverName();
		MercuryAPI.sendGlobalMessage(message, channel);
	}
	
	public static void requestPPSummon(UUID uuid) {
		if (!isMercuryEnabled)
			return;
		String message = "request|" + uuid.toString() + "|" + MercuryAPI.serverName();
		MercuryAPI.sendGlobalMessage(message, channel);
	}
	
	public static void denyPPSummon(UUID uuid, String reason) {
		if (!isMercuryEnabled)
			return;
		String message = "deny|" + uuid.toString() + "|" + reason;
		MercuryAPI.sendGlobalMessage(message, channel);
	}
	
	public static void returnPPSummon(UUID uuid) {
		if (!isMercuryEnabled)
			return;
		String message = "return|" + uuid.toString();
		MercuryAPI.sendGlobalMessage(message, channel);
	}
	
	public static void addBroadcast() {
		
	}
	
	public static void requestBroadcast() {
		
	}
	
	public static void removeBroadcast() {
		
	}
	
	public static void sendBroadcast() {
		
	}
}

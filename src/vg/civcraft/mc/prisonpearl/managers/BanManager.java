package vg.civcraft.mc.prisonpearl.managers;

import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civmodcore.annotations.CivConfig;
import vg.civcraft.mc.civmodcore.annotations.CivConfigType;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.prisonpearl.PrisonPearlConfig;
import vg.civcraft.mc.prisonpearl.PrisonPearlPlugin;
import vg.civcraft.mc.prisonpearl.database.interfaces.IPrisonPearlStorage;
import vg.civcraft.mc.prisonpearl.managers.ban.CBanManager;
import vg.civcraft.mc.prisonpearl.managers.ban.FBanManager;

/**
 * Extracting common functions as abstract class to allow switching out ban managers
 * 
 * @author ProgrammerDan <programmerdan@gmail.com>
 * @author rourke750
 *
 */
public abstract class BanManager{

	public abstract void enable();
	public abstract void setBanMessage(String msg);
	public abstract String getBanMessage();
	public abstract boolean isBanned(UUID uuidName);
	public abstract Set<UUID> listBannedUUIDS();
	public abstract void ban(UUID UUIDName);
	public abstract void pardon(UUID UUIDName);
	public abstract void setBanState(UUID UUIDName, boolean isBanned);
	public abstract boolean hasBannedPlayers();
	
	public static BanManager initialize() {
		plugin = PrisonPearlPlugin.getInstance();
		storage = PrisonPearlPlugin.getDBHandler().getStorageHandler().getPrisonPearlStorage();
		BanManager ban;
		if (PrisonPearlPlugin.isCBanManagementEnabled()) {
			ban = new CBanManager(plugin);
			ban.enable();
			return ban;
		}
		else {
			ban = new FBanManager(plugin);
			ban.enable();
			return ban;
		}
	}
	
	private static PrisonPearlPlugin plugin;
	private static IPrisonPearlStorage storage;
	
	public int checkBan(UUID id) {
		AltsListManager altsList = PrisonPearlPlugin.getAltsListManager();
		
		int maxImprisonedAlts = PrisonPearlConfig.getMaxAltsAllowed();
		UUID[] alts = altsList.getAltsArray(id);
		Integer pearledCount = storage.getImprisonedCount(alts);
		UUID[] imprisonedNames = storage.getImprisonedIds(alts);
		String names = "";
		String name = NameLayerManager.getName(id);
		for (int i = 0; i < imprisonedNames.length; i++) {
			names = names + imprisonedNames[i];
			if (i < imprisonedNames.length-1) {
				names = names + ", ";
			}
		}
		if (pearledCount >= maxImprisonedAlts) {
			if (!storage.isImprisoned(id)) {
				banAndKick(id, pearledCount, names);
				return maxImprisonedAlts;
			}
			int count = 0;
			for (UUID imprisonedName : imprisonedNames) {
				if (imprisonedName.compareTo(id) < 0) {
					count++;
				}
				if (count >= maxImprisonedAlts) {
					banAndKick(id, pearledCount, names);
					return maxImprisonedAlts;
				}
			}
		} else if (isBanned(id)) {
			if (pearledCount <= 0) {
				PrisonPearlPlugin.log("pardoning "+name+" for having no imprisoned alts");
			} else {
				PrisonPearlPlugin.log("pardoning "+name+" who only has "+pearledCount+" imprisoned alts");
			}
			pardon(id);
			return 1;
		}
		return 0;
	}
	
	public void checkBanForAlts(UUID id){
		AltsListManager alts = PrisonPearlPlugin.getAltsListManager();
		for (UUID alt: alts.getAltsArray(id))
			checkBan(alt);
	}

	private void banAndKick(UUID id, int pearledCount, String names) {
		String kickMessage = PrisonPearlConfig.getKickMessage();
		Player p = Bukkit.getPlayer(id);
		if (p != null) {
			p.kickPlayer(kickMessage);
		}
		String name = NameAPI.getCurrentName(id);
		if (isBanned(id)) {
			PrisonPearlPlugin.log(name+" still banned for having "+pearledCount+" imprisoned alts: "+names);
			return;
		}
		ban(id);
		PrisonPearlPlugin.log("banning "+name+" for having "+pearledCount+" imprisoned alts: "+names);
	}
}

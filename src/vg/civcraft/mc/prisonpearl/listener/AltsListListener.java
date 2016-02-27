package vg.civcraft.mc.prisonpearl.listener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import vg.civcraft.mc.prisonpearl.PrisonPearlPlugin;
import vg.civcraft.mc.prisonpearl.events.AltsListEvent;
import vg.civcraft.mc.prisonpearl.managers.AltsListManager;
import vg.civcraft.mc.prisonpearl.managers.BanManager;

public class AltsListListener implements Listener{

	private BanManager ban;
	private AltsListManager altsManager;
	
	public AltsListListener() {
		Bukkit.getPluginManager().registerEvents(this, PrisonPearlPlugin.getInstance());
		ban = PrisonPearlPlugin.getBanManager();
		altsManager = PrisonPearlPlugin.getAltsListManager();
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onAltsListUpdate(AltsListEvent event) {
			PrisonPearlPlugin
					.log("Grabbing alts for players");
		final List<UUID> altsList = event.getAltsList();
		// Save the old alt lists in their entirety to reduce all the cross
		// checking
		// players for existence within the Set.
		final Set<List<UUID>> banListsToCheck = new HashSet<List<UUID>>(
				altsList.size());
		final List<UUID> normalizedList = new ArrayList<UUID>(altsList.size());
		for (UUID playerUUID : altsList) {
			normalizedList.add(playerUUID);
			altsManager.addAltsHash(playerUUID, normalizedList);
			banListsToCheck.add(altsManager.getAlts(playerUUID));
		}
		// Unroll the ban lists into the playerBansToCheck. Only need a single
		// account from the banlist we just built to check it.
		final Set<UUID> playerBansToCheck = new HashSet<UUID>(
				banListsToCheck.size() * 10);
		playerBansToCheck.add(normalizedList.get(0));
		for (List<UUID> banList : banListsToCheck) {
			playerBansToCheck.addAll(banList);
		}
		// Check each player for bans, removing their alt list from the check
		// list
		// after they have been checked.
		int bannedCount = 0, unbannedCount = 0, total = 0, result;
		while (!playerBansToCheck.isEmpty()) {
			final UUID playerUUID = playerBansToCheck.iterator().next();
			final List<UUID> thisAltList = altsManager.getAlts(playerUUID);
			if (thisAltList == null) {
				playerBansToCheck.remove(playerUUID);
				continue;
			}
			playerBansToCheck.removeAll(thisAltList);
			for (UUID altUUID : thisAltList) {
				result = ban.checkBan(altUUID);
				if (result == 2)
					bannedCount++;
				else if (result == 1)
					unbannedCount++;
				total++;
			}
		}
		PrisonPearlPlugin.log(bannedCount + " players were banned, " + unbannedCount + " were unbanned out of " + total + " accounts.");
	}
}

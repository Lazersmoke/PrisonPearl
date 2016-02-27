package vg.civcraft.mc.prisonpearl.command.commands;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.prisonpearl.PrisonPearl;
import vg.civcraft.mc.prisonpearl.PrisonPearlPlugin;
import vg.civcraft.mc.prisonpearl.managers.PrisonPearlManager;

public class FreeAny extends PlayerCommand {
	public FreeAny(String name) {
		super(name);
		setIdentifier("ppfreeany");
		setDescription("Frees the pearl of the given player");
		setUsage("/ppfreeany <playername>");
		setArguments(1, 1);
	}

	public boolean execute(CommandSender sender, String[] args) {
		UUID imprisonedID;
		PrisonPearlManager manager = PrisonPearlPlugin.getPrisonPearlManager();
		PrisonPearl pearl = null;
		imprisonedID = NameAPI.getUUID(args[0]);
		if (imprisonedID == null) {
			sender.sendMessage(ChatColor.RED + "That player does not exist");
			return true;
		}
		if (!manager.isImprisoned(imprisonedID)) {
			sender.sendMessage(ChatColor.RED + "This player is not imprisoned");
			return true;
		}
		pearl = manager.getByImprisoned(imprisonedID);
		manager.freePearl(pearl, "Freed by admin");
		return true;
	}

	public List<String> tabComplete(CommandSender sender, String[] args) {
		return new LinkedList<String>(); // empty list
	}

}

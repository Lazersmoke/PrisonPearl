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

public class LocateAny extends PlayerCommand {
	public LocateAny(String name) {
		super(name);
		setIdentifier("pplocateany");
		setDescription("Locates the pearl holding the player with the given name");
		setUsage("/pplocateany <playername>");
		setArguments(1, 1);
	}

	public boolean execute(CommandSender sender, String[] args) {
		PrisonPearlManager manager = PrisonPearlPlugin.getPrisonPearlManager();
		UUID imprisonedID = NameAPI.getUUID(args[0]);
		if (imprisonedID == null) {
			sender.sendMessage(ChatColor.RED + "This player doesn't exist");
			return true;
		}
		if (!manager.isImprisoned(imprisonedID)) {
			sender.sendMessage(ChatColor.RED + "This player is not imprisoned");
			return true;
		}
		PrisonPearl pearl = manager.getByImprisoned(imprisonedID);
		if (!pearl.verifyLocation())
			manager.freePearl(pearl, String.format("Prison Pearl %s could not authenticate.", pearl.getImprisonedId().toString()));
		sender.sendMessage(ChatColor.YELLOW + "The pearl of "
				+ pearl.getImprisonedName() + " is " + pearl.describeLocation());
		return true;
	}

	public List<String> tabComplete(CommandSender sender, String[] args) {
		return new LinkedList<String>(); // empty list
	}
}

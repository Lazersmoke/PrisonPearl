package vg.civcraft.mc.prisonpearl.command.commands;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.prisonpearl.PrisonPearlPlugin;
import vg.civcraft.mc.prisonpearl.managers.PrisonPearlManager;

public class ImprisonAny extends PlayerCommand {
	public ImprisonAny(String name) {
		super(name);
		setIdentifier("ppimprisonany");
		setDescription("Imprisons the player with the given name and gives you his pearl");
		setUsage("/ppimprisonany <playername>");
		setArguments(1, 1);
	}
	
	public boolean execute(CommandSender sender, String [] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "You must be a player to do this");
			return true;
		}
		Player p = (Player) sender;
		PrisonPearlManager manager = PrisonPearlPlugin.getPrisonPearlManager();
		UUID imprisonedID = NameAPI.getUUID(args[0]);
		if (imprisonedID == null) {
			sender.sendMessage(ChatColor.RED + "That player does not exist");
			return true;
		}
		manager.imprisonPlayer(imprisonedID, p);		
		return true;
	}
	
	public List <String> tabComplete(CommandSender sender, String [] args) {
		return new LinkedList <String> (); //empty list
	}
}

package vg.civcraft.mc.prisonpearl.command.commands;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;
import vg.civcraft.mc.prisonpearl.PrisonPearlPlugin;
import vg.civcraft.mc.prisonpearl.managers.WorldBorderManager;

public class WorldBorder extends PlayerCommand{

	private WorldBorderManager wb;
	
	public WorldBorder(String name) {
		super(name);
		setIdentifier("ppwb");
		setDescription("Adds or removes an exception to world border pearl feeding.");
		setUsage("/ppwb <add/remove> world x y z");
		setArguments(5,5);
		wb = PrisonPearlPlugin.getWorldBorderManager();
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		boolean shouldAdd = false;
		if (args[0].equalsIgnoreCase("add"))
			shouldAdd = true;
		else if (args[0].equalsIgnoreCase("remove"))
			shouldAdd = false;
		else {
			sender.sendMessage(ChatColor.RED + "Please specify add or remove as the first argument.");
			return true;
		}
		
		String world = args[1];
		int x = 0, y = 0, z = 0;
		try {
			x = Integer.parseInt(args[2]);
			y = Integer.parseInt(args[3]);
			z = Integer.parseInt(args[4]);
		} catch (NumberFormatException e) {
			sender.sendMessage(ChatColor.RED + "Those integers are not valid.");
			return true;
		}
		
		World w = Bukkit.getWorld(world);
		if (w == null) {
			sender.sendMessage(ChatColor.RED + "That is not a valid location.");
			return true;
		}
		Location loc = new Location(w, x, y, z);
		
		if (shouldAdd) {
			if (wb.isOnWhiteList(loc)) {
				sender.sendMessage(ChatColor.RED + "That location is already on the list.");
				return true;
			}
			wb.addWhitelistedLocation(loc);
			sender.sendMessage(ChatColor.GREEN + "The location was added to the database.");
		}
		else {
			if (!wb.isOnWhiteList(loc)) {
				sender.sendMessage(ChatColor.RED + "That location is not on the list.");
				return true;
			}
			wb.removeWhitelistedLocation(loc);
			sender.sendMessage(ChatColor.GREEN + "The location was removed from the database.");
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return new LinkedList<String>();
	}

}

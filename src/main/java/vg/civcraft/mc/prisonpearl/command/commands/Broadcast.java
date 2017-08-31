package vg.civcraft.mc.prisonpearl.command.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;
import vg.civcraft.mc.prisonpearl.PrisonPearlPlugin;
import vg.civcraft.mc.prisonpearl.managers.BroadcastManager;
import vg.civcraft.mc.prisonpearl.managers.NameLayerManager;
import vg.civcraft.mc.prisonpearl.managers.PrisonPearlManager;

public class Broadcast extends PlayerCommand{
	
	private PrisonPearlManager pearls;
	private BroadcastManager broadcast;
	
	public Broadcast(String name) {
		super(name);
		setIdentifier("ppbroadcast");
		setDescription("Sends a broadcast request to a player.");
		setUsage("/ppbroadcast <player>");
		setArguments(1, 1);
		pearls = PrisonPearlPlugin.getPrisonPearlManager();
		broadcast = PrisonPearlPlugin.getBroadcastManager();
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Sorry you must be a player to execute this command.");
			return true;
		}
		Player p = (Player) sender;
		if (!pearls.isImprisoned(p)) {
			p.sendMessage(ChatColor.RED + "You are currently not imprisoned.");
			return true;
		}
		String name = args[0];
		UUID uuid = NameLayerManager.getUUID(name);
		if (uuid == null) {
			p.sendMessage(ChatColor.RED + "That player does not exist.");
			return true;
		}
		
		if (!broadcast.requestBroadcast(p.getUniqueId(), uuid)) {
			p.sendMessage(ChatColor.RED + "Something went wrong please contact the admins.");
			return true;
		}
		p.sendMessage(ChatColor.GREEN + "Your broadcast request has been sent.");
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		List<String> players = new ArrayList<String>();
		String characters = args.length == 0 ? "" : args[0];
		for (Player p: Bukkit.getOnlinePlayers()) 
			if (p.getName().startsWith(characters))
				players.add(p.getName());
		return players;
	}

}

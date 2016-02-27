package vg.civcraft.mc.prisonpearl.command.commands;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;
import vg.civcraft.mc.prisonpearl.PrisonPearl;
import vg.civcraft.mc.prisonpearl.PrisonPearlPlugin;
import vg.civcraft.mc.prisonpearl.managers.BroadcastManager;
import vg.civcraft.mc.prisonpearl.managers.PrisonPearlManager;

public class Locate extends PlayerCommand{

	private PrisonPearlManager manager;
	private BroadcastManager broad;
	
	public Locate(String name) {
		super(name);
		setIdentifier("pplocate");
		setDescription("Locates your pearl");
		setUsage("/ppl");
		setArguments(0, 0);
		manager = PrisonPearlPlugin.getPrisonPearlManager();
		broad = PrisonPearlPlugin.getBroadcastManager();
	}

	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED
					+ "This command can only be used as a player");
			return true;
		}
		Player p = (Player) sender;
		if (!manager.isImprisoned(p)) {
			p.sendMessage(ChatColor.RED + "You are not imprisoned");
			return true;
		}
		PrisonPearl pearl = manager.getByImprisoned(p);
		broad.broadcast(pearl.getImprisonedId());
		p.sendMessage(ChatColor.YELLOW + "Your pearl is " + pearl.describeLocation());
		return true;
	}

	public List<String> tabComplete(CommandSender sender, String[] args) {
		return new LinkedList<String>(); // empty list
	}	
}

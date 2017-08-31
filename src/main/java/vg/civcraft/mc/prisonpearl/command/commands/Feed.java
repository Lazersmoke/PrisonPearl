package vg.civcraft.mc.prisonpearl.command.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;
import vg.civcraft.mc.prisonpearl.PrisonPearlPlugin;
import vg.civcraft.mc.prisonpearl.managers.PrisonPearlManager;

public class Feed extends PlayerCommand{

	private PrisonPearlManager pearls;
	public Feed(String name) {
		super(name);
		setIdentifier("ppfeed");
		setDescription("This command can only be executed from terminal and will feed pearls.");
		setUsage("/ppfeed");
		setArguments(0, 0);
		pearls = PrisonPearlPlugin.getPrisonPearlManager();
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof ConsoleCommandSender)) {
			sender.sendMessage(ChatColor.RED + "You must execute that command from console.");
			return true;
		}
		pearls.feedPearls();
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}

}

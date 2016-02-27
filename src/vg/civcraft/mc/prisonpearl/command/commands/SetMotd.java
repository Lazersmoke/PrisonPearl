package vg.civcraft.mc.prisonpearl.command.commands;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.command.PlayerCommand;
import vg.civcraft.mc.prisonpearl.PrisonPearl;
import vg.civcraft.mc.prisonpearl.PrisonPearlPlugin;
import vg.civcraft.mc.prisonpearl.managers.PrisonPearlManager;

public class SetMotd extends PlayerCommand{

	private PrisonPearlManager pearls;
	
	public SetMotd(String name) {
		super(name);
		setIdentifier("ppsetmotd");
		setDescription("Sets the prisoners motd when they log in.");
		setUsage("/ppsetdist <message>");
		setArguments(0, 25);
		pearls = PrisonPearlPlugin.getPrisonPearlManager();
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("You must be a player to execute this weird command.");
			return true;
		}
		Player p = (Player) sender;
		PrisonPearl pearl = pearls.getPearlByItemStack(p.getItemInHand());
		if (pearl == null) {
			p.sendMessage(ChatColor.RED + "You are not holding a valid Prison Pearl.");
			return true;
		}
		StringBuilder builder = new StringBuilder();
		for (String x: args) {
			builder.append(x + " ");
		}
		String motd = builder.toString();
		motd.replaceAll("\\|", "");
		pearl.setMotd(motd);
		p.sendMessage(ChatColor.GREEN + String.format("You have set the prisoner's motd to %s.", motd));
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return new LinkedList<String>();
	}

}

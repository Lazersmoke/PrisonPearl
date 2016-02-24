package vg.civcraft.mc.prisonpearl.command.commands;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;
import vg.civcraft.mc.prisonpearl.PrisonPearl;
import vg.civcraft.mc.prisonpearl.Summon;
import vg.civcraft.mc.prisonpearl.managers.PrisonPearlManager;
import vg.civcraft.mc.prisonpearl.managers.SummonManager;

public class Silence extends PlayerCommand{

	private PrisonPearlManager pearls;
	private SummonManager summon;
	
	public Silence(String name) {
		super(name);
		setIdentifier("ppsilence");
		setDescription("Sets a prisoner to be silent when summoned, must be holding pearl.");
		setUsage("/ppsilence");
		setArguments(0, 0);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Like how even.");
			return true;
		}
		Player p = (Player) sender;
		PrisonPearl pearl = pearls.getPearlByItemStack(p.getItemInHand());
		if (pearl == null) {
			p.sendMessage(ChatColor.RED + "You are not holding a Prison Pearl.");
			return true;
		}
		if (!summon.isSummoned(pearl)) {
			p.sendMessage(ChatColor.RED + "That prisoner is not summoned.");
			return true;
		}
		Summon s = summon.getSummon(pearl);
		s.setCanSpeak(!s.getCanSpeak());
		p.sendMessage(ChatColor.GREEN + "The player set speak was set to " + s.getCanSpeak() + ".");
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return new LinkedList<String>();
	}

}

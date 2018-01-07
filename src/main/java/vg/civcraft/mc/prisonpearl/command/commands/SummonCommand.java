package vg.civcraft.mc.prisonpearl.command.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;
import vg.civcraft.mc.prisonpearl.PrisonPearl;
import vg.civcraft.mc.prisonpearl.PrisonPearlPlugin;
import vg.civcraft.mc.prisonpearl.managers.CombatTagManager;
import vg.civcraft.mc.prisonpearl.managers.PrisonPearlManager;
import vg.civcraft.mc.prisonpearl.managers.SummonManager;

public class SummonCommand extends PlayerCommand{

	private PrisonPearlManager pearls;
	private static CombatTagManager combatManager;
	private SummonManager summon;
	
	public SummonCommand(String name) {
		super(name);
		setIdentifier("ppsummon");
		setDescription("Summons the player that you are holding.");
		setUsage("/ppsummon");
		setArguments(0, 0);
		pearls = PrisonPearlPlugin.getPrisonPearlManager();
		summon = PrisonPearlPlugin.getSummonManager();
		combatManager = PrisonPearlPlugin.getCombatTagManager();
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("You must be a player to execute this command.");
			return true;
		}
		Player p = (Player) sender;
		PrisonPearl pearl = pearls.getPearlByItemStack(p.getInventory().getItemInMainHand());
		if (pearl == null) {
			p.sendMessage(ChatColor.RED + "That is not a PrisonPearl.");
			return true;
		}
		if (summon.isSummoned(pearl)) {
			p.sendMessage(ChatColor.RED + "That player is already summoned.");
			return true;
		}
		if (combatManager.isEnabled() && combatManager.isCombatTagged(player)) {
			p.sendMessage(ChatColor.RED + "The player is combat tagged, unable to summon.");
			return true;
		}
		if (!summon.summonPlayer(pearl)) {
			p.sendMessage(ChatColor.RED + "The player is not online.");
			return true;
		}
		p.sendMessage("Requesting a ppsummon.");
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}

}

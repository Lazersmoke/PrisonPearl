package vg.civcraft.mc.prisonpearl.command.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;
import vg.civcraft.mc.prisonpearl.PrisonPearl;
import vg.civcraft.mc.prisonpearl.PrisonPearlPlugin;
import vg.civcraft.mc.prisonpearl.managers.PrisonPearlManager;
import vg.civcraft.mc.prisonpearl.managers.SummonManager;

public class Return extends PlayerCommand{

	private PrisonPearlManager pearls;
	private SummonManager summon;
	
	public Return(String name) {
		super(name);
		setIdentifier("ppreturn");
		setDescription("Returns the player that you are holding to the prison world.");
		setUsage("/ppreturn");
		setArguments(0, 0);
		pearls = PrisonPearlPlugin.getPrisonPearlManager();
		summon = PrisonPearlPlugin.getSummonManager();
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("You must be a player to execute this very fine command.");
			return true;
		}
		Player p = (Player) sender;
		ItemStack stack = p.getItemInHand();
		PrisonPearl pearl = pearls.getPearlByItemStack(stack);
		if (pearl == null) {
			p.sendMessage(ChatColor.RED + "You are not holding a PrisonPearl.");
			return true;
		}
		if (!summon.isSummoned(pearl)) {
			p.sendMessage(ChatColor.RED + "That player is not summoned.");
			return true;
		}
		if (pearl.getImprisonedPlayer() == null) {
			p.sendMessage(ChatColor.RED + "The player is offline, cannot be returned safely.");
			return true;
		}
		if (!summon.returnPlayer(pearl)) { // There was an issue returning the player.
			p.sendMessage(ChatColor.RED + "There was an issue returning the player.");
			return true;
		}
		p.sendMessage(ChatColor.GREEN + "You have successfully returned the player.");
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}

}

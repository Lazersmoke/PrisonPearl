package vg.civcraft.mc.prisonpearl.command.commands;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.prisonpearl.PrisonPearl;
import vg.civcraft.mc.prisonpearl.PrisonPearlPlugin;
import vg.civcraft.mc.prisonpearl.managers.PrisonPearlManager;

public class Free extends PlayerCommand {
	public Free(String name) {
		super(name);
		setIdentifier("ppfree");
		setDescription("Frees the pearl you are holding in your hand or the one of the given player, if you have his pearl");
		setUsage("/ppfree [playername]");
		setArguments(0, 1);
	}
	
	public boolean execute(CommandSender sender, String [] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Use /freeany instead");
			return true;
		}
		Player p = (Player) sender;
		UUID imprisonedID;
		PrisonPearlManager manager = PrisonPearlPlugin.getPrisonPearlManager();
		PrisonPearl pearl = null;
		if (args.length != 0) {
			imprisonedID = NameAPI.getUUID(args[0]);
			if (imprisonedID == null) {
				sender.sendMessage(ChatColor.RED + "That player does not exist");
				return true;
			}
			if (manager.isImprisoned(imprisonedID)) {
				pearl = manager.getByImprisoned(imprisonedID);
				if (pearl.getHolderPlayer() != null || !pearl.getHolderPlayer().getUniqueId().equals(p.getUniqueId())) {
					imprisonedID = null;
				}
			}
			else {
				imprisonedID = null;
			}
			if (imprisonedID == null) {
				//dont tell the player whether the person is imprisoned at all
				p.sendMessage(ChatColor.RED + "You do not have the pearl of this player");
				return true;
			}
		}
		else {
			ItemStack is = p.getInventory().getItemInMainHand();
			pearl = manager.getPearlByItemStack(is);
			if (pearl == null) {
				p.sendMessage(ChatColor.RED + "You are not holding a valid ender pearl");
				return true;
			}
			imprisonedID = pearl.getImprisonedId();
		}
		p.sendMessage(ChatColor.GREEN + "You have freed " + pearl.getImprisonedName() + ".");
		manager.freePearl(pearl, "Freed by imprisoner");
		return true;
	}
	
	public List <String> tabComplete(CommandSender sender, String [] args) {
		return new LinkedList <String> (); //empty list
	}
}

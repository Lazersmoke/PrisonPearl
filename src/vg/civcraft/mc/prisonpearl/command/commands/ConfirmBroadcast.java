package vg.civcraft.mc.prisonpearl.command.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;
import vg.civcraft.mc.prisonpearl.PrisonPearlPlugin;
import vg.civcraft.mc.prisonpearl.managers.BroadcastManager;
import vg.civcraft.mc.prisonpearl.managers.MercuryManager;
import vg.civcraft.mc.prisonpearl.managers.NameLayerManager;

public class ConfirmBroadcast extends PlayerCommand{

	private BroadcastManager broadcast;
	
	public ConfirmBroadcast(String name) {
		super(name);
		setIdentifier("ppconfirm");
		setDescription("Confirms a broadcast for a pearled player.");
		setUsage("/ppconfirm <pearled player>");
		setArguments(1, 1);
		broadcast = PrisonPearlPlugin.getBroadcastManager();
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("You must be a player to perform this class.");
			return true;
		}
		Player p = (Player) sender;
		String name = args[0]; // The name of the pearled player.
		UUID uuid = NameLayerManager.getUUID(name);
		if (uuid == null) {
			p.sendMessage(ChatColor.RED + "That player does not exist.");
			return true;
		}
		if (!broadcast.isRequestedPlayer(uuid, p.getUniqueId())) {
			p.sendMessage(ChatColor.RED + "That player has not requested that you broadcast to him.");
			return true;
		}
		broadcast.addBroadcast(uuid, p.getUniqueId());
		MercuryManager.addBroadcast(uuid, p.getUniqueId()); // We need to remember to alert mercury.
		p.sendMessage(ChatColor.GREEN + "You have successfully confirmed the ppbroadcast.");
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		List<String> list = new ArrayList<String>();
		if (!(sender instanceof Player))
			return new ArrayList<String>();
		UUID uuid = broadcast.getRequestedPlayer(((Player) sender).getUniqueId());
		String name = NameLayerManager.getName(uuid);
		list.add(name);
		return list;
	}

}

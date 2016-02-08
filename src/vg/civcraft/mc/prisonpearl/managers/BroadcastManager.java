package vg.civcraft.mc.prisonpearl.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

public class BroadcastManager {
	private final Map<UUID, List<UUID>> broadcasts;
	private final Map<UUID, UUID> confirm;
	
	public BroadcastManager() {
		broadcasts = new HashMap<UUID, List<UUID>>();
		confirm = new HashMap<Player, Player>();
	}
	
	public boolean addBroadcast(UUID player, UUID receiver) {
		if (!broadcasts.containsKey(player))
			broadcasts.put(player, new ArrayList<UUID>());
		
		return broadcasts.get(player).add(receiver);
	}
	
	public boolean confirmBroadcast(UUID player, UUID receiver) {
		
	}
	
	public boolean silenceBroadcast(UUID player, UUID receiver) {
		
    }
	
	public boolean removeBroadcasts(UUID player) {
		
	}
	
	public Player getConfirmPlayer(UUID receiver) {
		
	}
}

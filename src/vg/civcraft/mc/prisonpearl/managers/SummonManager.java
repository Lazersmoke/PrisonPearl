package vg.civcraft.mc.prisonpearl.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;

import vg.civcraft.mc.prisonpearl.PrisonPearl;
import vg.civcraft.mc.prisonpearl.Summon;

public class SummonManager {

	public boolean isSummoned(Player p) {
		return isSummoned(p.getUniqueId());
	}
	
	public boolean isSummoned(UUID uuid) {
		return false; //TODO
	}
	
	public void summonPlayer(PrisonPearl pearl) {
		//TODO
	}
	
	public void returnPlayer(PrisonPearl pearl) {
		//TODO
	}
	
	public Summon getSummon(Player p) {
		return null; //TODO
	}
	
	public Summon getSummon(PrisonPearl pearl) {
		return null; //TODO
	}
}

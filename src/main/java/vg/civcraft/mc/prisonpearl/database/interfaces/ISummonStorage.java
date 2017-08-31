package vg.civcraft.mc.prisonpearl.database.interfaces;

import java.util.Map;
import java.util.UUID;

import vg.civcraft.mc.prisonpearl.Summon;

public interface ISummonStorage {

	public void addSummon(Summon summon);
	public void removeSummon(Summon summon);
	public void removeSummon(UUID uuid);
	public Summon getSummon(UUID uuid);
	public boolean isSummoned(UUID uuid);
	public void updateSummon(Summon summon);
	public Map<UUID, Summon> getAllSummons();
	public void save();
	public void load();
}

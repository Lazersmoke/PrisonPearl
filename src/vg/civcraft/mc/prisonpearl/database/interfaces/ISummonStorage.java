package vg.civcraft.mc.prisonpearl.database.interfaces;

import java.util.UUID;

import vg.civcraft.mc.prisonpearl.Summon;

public interface ISummonStorage {

	public void addSummon(Summon summon);
	public void removeSummon(Summon summon);
	public void removeSummon(UUID uuid);
	public void getSummon(UUID uuid);
	public boolean isSummoned(UUID uuid);
	public void updateSummon(Summon summon);
}

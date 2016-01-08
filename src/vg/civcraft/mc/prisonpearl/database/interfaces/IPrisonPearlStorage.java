package vg.civcraft.mc.prisonpearl.database.interfaces;

import java.util.List;
import java.util.UUID;

import vg.civcraft.mc.prisonpearl.PrisonPearl;

public interface IPrisonPearlStorage {

	public void addPearl(PrisonPearl pp);
	public void removePearl(PrisonPearl pp);
	public PrisonPearl getPearl(UUID uuid);
	public List<PrisonPearl> getAllPearls();
	public void updatePearl(PrisonPearl pp);
}

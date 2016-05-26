package vg.civcraft.mc.prisonpearl.database.flatfile;

import vg.civcraft.mc.prisonpearl.database.interfaces.IPrisonPearlStorage;
import vg.civcraft.mc.prisonpearl.database.interfaces.IPrisonPortaledStorage;
import vg.civcraft.mc.prisonpearl.database.interfaces.ISaveLoad;
import vg.civcraft.mc.prisonpearl.database.interfaces.IStorageHandler;
import vg.civcraft.mc.prisonpearl.database.interfaces.ISummonStorage;
import vg.civcraft.mc.prisonpearl.database.interfaces.IWorldBorderStorage;

public class FlatFileHandler implements ISaveLoad, IStorageHandler{

	private PrisonPearlFileStorage prisonPearlStorage;
	private PrisonPortaledFileStorage prisonPortalStorage;
	private SummonFileStorage summonFileStorage;
	private WorldBorderFileStorage worldBorderFileStorage;
	
	public FlatFileHandler() {
		prisonPearlStorage = new PrisonPearlFileStorage();
		prisonPortalStorage = new PrisonPortaledFileStorage();
		summonFileStorage = new SummonFileStorage();
		worldBorderFileStorage = new WorldBorderFileStorage();
	}

	@Override
	public IPrisonPearlStorage getPrisonPearlStorage() {
		return prisonPearlStorage;
	}

	@Override
	public IPrisonPortaledStorage getPrisonPortaledStorage() {
		return prisonPortalStorage;
	}

	@Override
	public ISummonStorage getSummonStorage() {
		return summonFileStorage;
	}

	@Override
	public IWorldBorderStorage getWorldBorderStorage() {
		return worldBorderFileStorage;
	}

	@Override
	public void save() {
		prisonPearlStorage.save();
		prisonPortalStorage.save();
		summonFileStorage.save();
		worldBorderFileStorage.save();
	}

	@Override
	public void load() {
		prisonPearlStorage.load();
		prisonPortalStorage.load();
		summonFileStorage.load();
		worldBorderFileStorage.load();
	}
}

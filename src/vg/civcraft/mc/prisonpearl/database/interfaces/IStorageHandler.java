package vg.civcraft.mc.prisonpearl.database.interfaces;

/**
 * The IStorageHandler interface deals with getting the correct Storage handler for each manager that requires one.
 * @author Rourke750
 *
 */
public interface IStorageHandler {

	public IPrisonPearlStorage getPrisonPearlStorage();
	public IPrisonPortaledStorage getPrisonPortaledStorage();
	public ISummonStorage getSummonStorage();
	public IWorldBorderStorage getWorldBorderStorage();
}

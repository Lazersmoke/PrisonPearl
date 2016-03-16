package vg.civcraft.mc.prisonpearl.database.mysql;

import java.util.logging.Level;

import org.bukkit.Bukkit;

import vg.civcraft.mc.civmodcore.Config;
import vg.civcraft.mc.civmodcore.annotations.CivConfig;
import vg.civcraft.mc.civmodcore.annotations.CivConfigType;
import vg.civcraft.mc.civmodcore.annotations.CivConfigs;
import vg.civcraft.mc.prisonpearl.PrisonPearlConfig;
import vg.civcraft.mc.prisonpearl.PrisonPearlPlugin;
import vg.civcraft.mc.prisonpearl.database.interfaces.IPrisonPearlStorage;
import vg.civcraft.mc.prisonpearl.database.interfaces.IPrisonPortaledStorage;
import vg.civcraft.mc.prisonpearl.database.interfaces.ISaveLoad;
import vg.civcraft.mc.prisonpearl.database.interfaces.IStorageHandler;
import vg.civcraft.mc.prisonpearl.database.interfaces.ISummonStorage;
import vg.civcraft.mc.prisonpearl.database.interfaces.IWorldBorderStorage;

public class MysqlDatabaseHandler implements ISaveLoad, IStorageHandler{

	private Database db;
	private PrisonPearlPlugin plugin;
	
	private PrisonPearlMysqlStorage ppStorage;
	private PrisonPortaledMysqlStorage portaledStorage;
	private SummonMysqlStorage summonStorage;
	private WorldBorderMysqlStorage worldBorderStorage;
	
	public MysqlDatabaseHandler() {
		plugin = PrisonPearlPlugin.getInstance();
		initializeDB();
		initializeStorageManagers();
		Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

			@Override
			public void run() {
				save();
			}
			
		}, 100, PrisonPearlConfig.getPPSaveTicks());
	}
	
	
	private void initializeDB() {
		String username = PrisonPearlConfig.getUsername();
		String password = PrisonPearlConfig.getPassword();
		String dbname = PrisonPearlConfig.getDBName();
		String host = PrisonPearlConfig.getHost();
		int port = PrisonPearlConfig.getPort();
		db = new Database(host, port, dbname, username, password, plugin.getLogger());
		if (!db.connect()) {
			plugin.getLogger().log(Level.SEVERE, "Failed to connect to mysql, shutting down.");
			Bukkit.shutdown();
		}
	}
	
	private void initializeStorageManagers() {
		ppStorage = new PrisonPearlMysqlStorage(db, this);
		portaledStorage = new PrisonPortaledMysqlStorage(db, this);
		summonStorage = new SummonMysqlStorage(db, this);
		worldBorderStorage = new WorldBorderMysqlStorage(db, this);
	}

	@Override
	public IPrisonPearlStorage getPrisonPearlStorage() {
		return ppStorage;
	}

	@Override
	public IPrisonPortaledStorage getPrisonPortaledStorage() {
		return portaledStorage;
	}

	@Override
	public ISummonStorage getSummonStorage() {
		return summonStorage;
	}

	@Override
	public IWorldBorderStorage getWorldBorderStorage() {
		return worldBorderStorage;
	}

	@Override
	public void save() {
		ppStorage.save();
		portaledStorage.save();
		summonStorage.save();
		worldBorderStorage.save();
	}

	@Override
	public void load() {
		ppStorage.load();
		portaledStorage.load();
		summonStorage.load();
		worldBorderStorage.load();
	}
	
	protected void refreshAndReconnect() {
		if (!db.isConnected())
			db.connect();
	}
}

package vg.civcraft.mc.prisonpearl.database.mysql;

import java.util.logging.Level;

import org.bukkit.Bukkit;

import vg.civcraft.mc.civmodcore.Config;
import vg.civcraft.mc.civmodcore.annotations.CivConfig;
import vg.civcraft.mc.civmodcore.annotations.CivConfigType;
import vg.civcraft.mc.civmodcore.annotations.CivConfigs;
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
	private Config config;
	
	private PrisonPearlMysqlStorage ppStorage;
	private PrisonPortaledMysqlStorage portaledStorage;
	private SummonMysqlStorage summonStorage;
	private WorldBorderMysqlStorage worlderBorderStorage;
	
	public MysqlDatabaseHandler() {
		plugin = PrisonPearlPlugin.getInstance();
		config = plugin.GetConfig();
		initializeDB();
		initializeStorageManagers();
	}
	
	@CivConfigs({
		@CivConfig(name = "database.mysql.username", def = "bukkit", type = CivConfigType.String),
		@CivConfig(name = "database.mysql.password", def = "", type = CivConfigType.String),
		@CivConfig(name = "database.mysql.dbname", def = "bukkit", type = CivConfigType.String),
		@CivConfig(name = "database.mysql.host", def = "localhost", type = CivConfigType.String),
		@CivConfig(name = "database.mysql.port", def = "3306", type = CivConfigType.Int)
	})
	private void initializeDB() {
		String username = config.get("database.mysql.username").getString();
		String password = config.get("database.mysql.password").getString();
		String dbname = config.get("database.mysql.dbname").getString();
		String host = config.get("database.mysql.host").getString();
		int port = config.get("database.mysql.port").getInt();
		db = new Database(host, port, dbname, username, password, plugin.getLogger());
		if (!db.connect()) {
			plugin.getLogger().log(Level.SEVERE, "Failed to connect to mysql, shutting down.");
			Bukkit.shutdown();
		}
	}
	
	private void initializeStorageManagers() {
		
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
		return worlderBorderStorage;
	}

	@Override
	public void save() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void load() {
		// TODO Auto-generated method stub
		
	}
	
	protected void refreshAndReconnect() {
		if (!db.isConnected())
			db.connect();
	}
}

package vg.civcraft.mc.prisonpearl.database;

import vg.civcraft.mc.civmodcore.Config;
import vg.civcraft.mc.civmodcore.annotations.CivConfig;
import vg.civcraft.mc.civmodcore.annotations.CivConfigType;
import vg.civcraft.mc.prisonpearl.PrisonPearlPlugin;
import vg.civcraft.mc.prisonpearl.database.flatfile.FlatFileHandler;
import vg.civcraft.mc.prisonpearl.database.interfaces.ISaveLoad;
import vg.civcraft.mc.prisonpearl.database.interfaces.IStorageHandler;
import vg.civcraft.mc.prisonpearl.database.mysql.MysqlDatabaseHandler;

/**
 * This class is used to get either the type of database manager class for dealing with saving
 * or loading the correct data into its respectful database.
 * @author Rourke750
 *
 */
public class DataBaseHandler {

	private enum DbType {
		FLATFILE,
		MYSQL;
	};
	
	private PrisonPearlPlugin plugin;
	private Config config;
	
	private ISaveLoad saveLoad;
	private IStorageHandler storageHandler;
	
	public DataBaseHandler() {
		plugin = PrisonPearlPlugin.getInstance();
		config = plugin.GetConfig();
		DbType type = getDataBaseType();
		if (type == DbType.FLATFILE)
			handleFlatFile();
		else if (type == DbType.MYSQL)
			handleMysql();
	}
	
	@CivConfig(name = "database.type", def = "0", type = CivConfigType.Int)
	public DbType getDataBaseType() {
		int type = config.get("database.type").getInt();
		switch(type) {
		case 0:
			return DbType.FLATFILE;
		case 1:
			return DbType.MYSQL;
		}
		return null;
	}
	
	private void handleFlatFile() {
		FlatFileHandler handle = new FlatFileHandler();
		saveLoad = handle;
		storageHandler = handle;
	}
	
	private void handleMysql() {
		MysqlDatabaseHandler handle = new MysqlDatabaseHandler();
		saveLoad = handle;
		storageHandler = handle;
	}
	
	/**
	 * The ISaveLoad interface deals with saving and loading all storage classes. 
	 * Each Manager will have no idea in what manner the storage is being saved.
	 * @return Returns the object responsible for saving and loading storage containers.
	 */
	public ISaveLoad getSaveLoadHandler() {
		return saveLoad;
	}
	
	/**
	 * The IStorageHandler interface deals with getting the correct Storage handler for each manager that requires one.
	 * @return Returns the Object dealing with managing each storage manager. You will not know in which manner that data is
	 * being manipulated.
	 */
	public IStorageHandler getStorageHandler() {
		return storageHandler;
	}
}

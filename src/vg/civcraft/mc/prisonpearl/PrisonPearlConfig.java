package vg.civcraft.mc.prisonpearl;

import java.util.List;

import vg.civcraft.mc.civmodcore.annotations.CivConfig;
import vg.civcraft.mc.civmodcore.annotations.CivConfigType;
import vg.civcraft.mc.civmodcore.annotations.CivConfigs;

public class PrisonPearlConfig {

	@CivConfig(name = "prison.reset_bed", def = "false", type = CivConfigType.Bool)
	public static boolean isPrisonResetBed() {
		return PrisonPearlPlugin.getInstance().GetConfig().get("prison.reset_bed").getBool();
	}
	
	@CivConfig(name = "prison_grant_perms", def = "", type = CivConfigType.String_List)
	public static List<String> getPrisonGrantPerms() {
		return PrisonPearlPlugin.getInstance().GetConfig().get("prison_grant_perms").getStringList();
	}

	@CivConfig(name = "prison_deny_perms", def = "", type = CivConfigType.String_List)
	public static List<String> getPrisonDenyPerms() {
		return PrisonPearlPlugin.getInstance().GetConfig().get("prison_deny_perms").getStringList();
	}
	
	@CivConfig(name = "prison.world", def = "world_the_end", type = CivConfigType.String)
	public static String getImprisonWorldName() {
		return PrisonPearlPlugin.getInstance().GetConfig().get("prison.world").getString();
	}
	
	@CivConfig(name = "prison.server", def = "bukkit", type = CivConfigType.String)
	public static String getImprisonServerName() {
		return PrisonPearlPlugin.getInstance().GetConfig().get("prison.server").getString();
	}
	
	@CivConfig(name = "database.mysql.username", def = "bukkit", type = CivConfigType.String)
	public static String getUsername() {
		return PrisonPearlPlugin.getInstance().GetConfig().get("database.mysql.username").getString();
	}
	
	@CivConfig(name = "database.mysql.password", def = "", type = CivConfigType.String)
	public static String getPassword() {
		return PrisonPearlPlugin.getInstance().GetConfig().get("database.mysql.password").getString();
	}
	
	@CivConfig(name = "database.mysql.dbname", def = "bukkit", type = CivConfigType.String)
	public static String getDBName() {
		return PrisonPearlPlugin.getInstance().GetConfig().get("database.mysql.dbname").getString();
	}
	
	@CivConfig(name = "database.mysql.host", def = "localhost", type = CivConfigType.String)
	public static String getHost() {
		return PrisonPearlPlugin.getInstance().GetConfig().get("database.mysql.host").getString();
	}
	
	@CivConfig(name = "database.mysql.port", def = "3306", type = CivConfigType.Int)
	public static int getPort() {
		return PrisonPearlPlugin.getInstance().GetConfig().get("database.mysql.port").getInt();
	}
	
	@CivConfig(name = "free_tppearl", def = "true", type = CivConfigType.Bool)
	public static boolean shouldTpPearlOnFree() {
		return PrisonPearlPlugin.getInstance().GetConfig().get("free_tppearl").getBool();
	}
	
	@CivConfig(name = "prison.clearinventoryonsummon", def = "true", type = CivConfigType.Bool)
	public static boolean shouldPpsummonClearInventory() {
		return PrisonPearlPlugin.getInstance().GetConfig().get("prison.clearinventoryonsummon").getBool();
	}
	
	@CivConfig(name = "prison.ppsummonpearls", def = "true", type = CivConfigType.Bool)
	public static boolean shouldPpsummonLeavePearls() {
		return PrisonPearlPlugin.getInstance().GetConfig().get("prison.ppsummonpearls").getBool();
	}
}

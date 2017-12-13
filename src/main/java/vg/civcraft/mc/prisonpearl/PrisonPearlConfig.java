package vg.civcraft.mc.prisonpearl;

import java.util.List;

import org.bukkit.Material;

public class PrisonPearlConfig {

	
	public static boolean isPrisonResetBed() {
		return PrisonPearlPlugin.getInstance().getConfig().getBoolean("prison.reset_bed");
	}
	
	
	public static List<String> getPrisonGrantPerms() {
		return PrisonPearlPlugin.getInstance().getConfig().getStringList("prison_grant_perms");
	}

	
	public static List<String> getPrisonDenyPerms() {
		return PrisonPearlPlugin.getInstance().getConfig().getStringList("prison_deny_perms");
	}
	
	
	public static String getImprisonWorldName() {
		return PrisonPearlPlugin.getInstance().getConfig().getString("prison.world");
	}
	
	
	public static String getUsername() {
		return PrisonPearlPlugin.getInstance().getConfig().getString("database.mysql.username");
	}
	
	
	public static String getPassword() {
		return PrisonPearlPlugin.getInstance().getConfig().getString("database.mysql.password");
	}
	
	
	public static String getDBName() {
		return PrisonPearlPlugin.getInstance().getConfig().getString("database.mysql.dbname");
	}
	
	
	public static String getHost() {
		return PrisonPearlPlugin.getInstance().getConfig().getString("database.mysql.host");
	}
	
	
	public static int getPort() {
		return PrisonPearlPlugin.getInstance().getConfig().getInt("database.mysql.port");
	}
	
	
	public static int getDatabaseType() {
		return PrisonPearlPlugin.getInstance().getConfig().getInt("database.type");
	}
	
	
	public static boolean shouldTpPearlOnFree() {
		return PrisonPearlPlugin.getInstance().getConfig().getBoolean("free_tppearl");
	}
	
	
	public static boolean requireSummonToKill() {
		return PrisonPearlPlugin.getInstance().getConfig().getBoolean("kill_require_summon");
	}
	
	
	public static boolean shouldPpsummonClearInventory() {
		return PrisonPearlPlugin.getInstance().getConfig().getBoolean("prison.clearinventoryonsummon");
	}
	
	
	public static boolean getShouldPPReturnKill() {
		return PrisonPearlPlugin.getInstance().getConfig().getBoolean("prison.ppreturn_kill");
	}
	
	
	public static boolean shouldPpsummonLeavePearls() {
		return PrisonPearlPlugin.getInstance().getConfig().getBoolean("prison.ppsummonpearls");
	}
	
	
	public static List<String> getPrisonMotd() {
		return PrisonPearlPlugin.getInstance().getConfig().getStringList("prison.motd");
	}
	
	
	public static long getIgnoreFeedSecond() {
		return PrisonPearlPlugin.getInstance().getConfig().getLong("ignore_feed.seconds");
	}
	
	
	public static long getIngoreFeedHours() {
		return PrisonPearlPlugin.getInstance().getConfig().getLong("ignore_feed.hours");
	}
	
	
	public static long getIngoreFeedDays() {
		return PrisonPearlPlugin.getInstance().getConfig().getLong("ignore_feed.days");
	}
	
	
	public static long getIgnoreFeedDelay() {
		return PrisonPearlPlugin.getInstance().getConfig().getLong("ignore_feed.feed_delay");
	}
	
	
	public static Material getResourceUpkeepMaterial() {
		return Material.getMaterial(PrisonPearlPlugin.getInstance().getConfig().getString("upkeep.resource"));
	}
	
	
	public static int getResourceUpkeepAmount() {
		return PrisonPearlPlugin.getInstance().getConfig().getInt("upkeep.quantity");
	}
	
	
	public static boolean getShouldAutoFreeWorldBorder() {
		return PrisonPearlPlugin.getInstance().getConfig().getBoolean("autofree_worldborder");
	}
	
	
	public static boolean getMustPrisonPearlHotBar() {
		return PrisonPearlPlugin.getInstance().getConfig().getBoolean("prison_musthotbar");
	}
	
	
	public static boolean getAllowPrisonStealing() {
		return PrisonPearlPlugin.getInstance().getConfig().getBoolean("prison_stealing");
	}
	
	
	public static int getDamageLogMin() {
		return PrisonPearlPlugin.getInstance().getConfig().getInt("damagelog_min");
	}
	
	
	public static int getDamagelogTicks() {
		return PrisonPearlPlugin.getInstance().getConfig().getInt("damagelog_ticks");
	}
	
	
	public static int getSummonDamageRadius() {
		return PrisonPearlPlugin.getInstance().getConfig().getInt("summon_damage_radius");
	}
	
	
	public static int getSummonDamageTicks() {
		return PrisonPearlPlugin.getInstance().getConfig().getInt("summon_damage_ticks");
	}
	
	
	public static int getSummonDamageAmount() {
		return PrisonPearlPlugin.getInstance().getConfig().getInt("summon_damage_amt");
	}
	
	public static String getKickMessage() {
		return PrisonPearlPlugin.getInstance().getConfig().getString("kickMessage");
	}
	
	public static List<String> getPearlDenyTransferWorlds() {
		return PrisonPearlPlugin.getInstance().getConfig().getStringList("deny_pearls_worlds");
	}
	
	
	public static int getPrisonUnloadTimerTicks() {
		return PrisonPearlPlugin.getInstance().getConfig().getInt("prison.unloadTimerTicks");
	}
	
	
	public static int getPPSaveTicks() {
		return PrisonPearlPlugin.getInstance().getConfig().getInt("ppsaveticks");
	}

	
	public static boolean isDebug() {
		return PrisonPearlPlugin.getInstance().getConfig().getBoolean("debug");
	}
}

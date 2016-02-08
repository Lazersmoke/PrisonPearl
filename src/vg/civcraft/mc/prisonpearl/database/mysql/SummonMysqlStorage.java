package vg.civcraft.mc.prisonpearl.database.mysql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;

import com.google.common.collect.Maps;

import vg.civcraft.mc.prisonpearl.Summon;
import vg.civcraft.mc.prisonpearl.database.interfaces.ISummonStorage;
import vg.civcraft.mc.prisonpearl.misc.FakeLocation;

public class SummonMysqlStorage implements ISummonStorage{

	private Database db;
	private MysqlDatabaseHandler handle;
	
	public SummonMysqlStorage(Database db, MysqlDatabaseHandler handle) {
		this.db = db;
		this.handle = handle;
		createTables();
		initializeStatements();
		load(); // Load summons
	}
	
	private void createTables() {
		db.execute("create table if not exists PrisonPearlSummon("
				+ "uuid varchar(36) not null,"
				+ "world varchar(36) not null,"
				+ "x int not null,"
				+ "y int not null,"
				+ "z int not null,"
				+ "dist int,"
				+ "damage int,"
				+ "canSpeak tinyint(1),"
				+ "canDamage tinyint(1),"
				+ "canBreak tinyint(1),"
				+ "primary key uuid_key(uuid));");
	}
	
	private String addSummonedPlayer, removeSummonedPlayer, updateSummonedPlayer, getAllSummonedPlayer,
		getSummon;
	
	private void initializeStatements() {
		addSummonedPlayer = "insert into PrisonPearlSummon("
				+ "uuid, world, x, y, z, dist, damage, canSpeak, canDamage, canBreak)"
				+ "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
		removeSummonedPlayer = "delete from PrisonPearlSummon where uuid = ?;";
		updateSummonedPlayer = "update PrisonPearlSummon "
				+ "set world = ?, x = ?, y = ?, z = ?, dist = ?, "
				+ "damage = ?, canSpeak = ?, canDamage = ?, canBreak = ? "
				+ "where uuid = ?;";
		getAllSummonedPlayer = "select * from PrisonPearlSummon;";
		getSummon = "select * from PrisonPearlSummon where uuid = ?;";
	}
	
	private void load() {
		
	}
	
	private Map<UUID, Summon> summons = Maps.newHashMap();

	@Override
	public void addSummon(Summon summon) {
		handle.refreshAndReconnect();
		summons.put(summon.getUUID(), summon);
		if (summon.getReturnLocation() instanceof FakeLocation)
			return;
		PreparedStatement addSummonedPlayer = db.prepareStatement(this.addSummonedPlayer);
		Location loc = summon.getReturnLocation();
		try {
			addSummonedPlayer.setString(1, summon.getUUID().toString());
			addSummonedPlayer.setString(2, loc.getWorld().getUID().toString());
			addSummonedPlayer.setInt(3, loc.getBlockX());
			addSummonedPlayer.setInt(4, loc.getBlockY());
			addSummonedPlayer.setInt(5, loc.getBlockZ());
			addSummonedPlayer.setInt(6, summon.getMaxDistance());
			addSummonedPlayer.setInt(7, summon.getAmountDamage());
			addSummonedPlayer.setBoolean(8, summon.getCanSpeak());
			addSummonedPlayer.setBoolean(9, summon.getCanDamage());
			addSummonedPlayer.setBoolean(10, summon.getCanBreak());
			addSummonedPlayer.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				addSummonedPlayer.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void removeSummon(Summon summon) {
		removeSummon(summon.getUUID());
	}
	
	@Override
	public void removeSummon(UUID uuid) {
		handle.refreshAndReconnect();
		PreparedStatement removeSummonedPlayer = db.prepareStatement(this.removeSummonedPlayer);
		try {
			removeSummonedPlayer.setString(1, uuid.toString());
			removeSummonedPlayer.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				removeSummonedPlayer.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void getSummon(UUID uuid) {
		Summon summon = summons.get(uuid);
		if (summon == null) {
			PreparedStatement getSummon = db.prepareStatement(this.getSummon);
		}
	}

	@Override
	public boolean isSummoned(UUID uuid) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void updateSummon(Summon summon) {
		// TODO Auto-generated method stub
		
	}
}

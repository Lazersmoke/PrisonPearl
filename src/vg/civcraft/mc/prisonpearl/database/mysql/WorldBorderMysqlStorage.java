package vg.civcraft.mc.prisonpearl.database.mysql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import vg.civcraft.mc.prisonpearl.database.interfaces.IWorldBorderStorage;

public class WorldBorderMysqlStorage implements IWorldBorderStorage{

	private Database db;
	private MysqlDatabaseHandler handler;
	
	public WorldBorderMysqlStorage(Database db, MysqlDatabaseHandler handler) {
		this.db = db;
		this.handler = handler;
		createTables();
		initializeStatements();
	}
	
	public void createTables() {
		db.execute("create table if not exists ppWorldBorder("
				+ "world varchar(36) not null,"
				+ "x int not null,"
				+ "y int not null,"
				+ "z int not null,"
				+ "primary key world_key(world));");
	}
	
	private String addWorldBorder, removeWorldBorder, getAllWorldBorder;
	
	private List<Location> locs = new ArrayList<Location>();
	
	private void initializeStatements() {
		addWorldBorder = "insert into ppWorldBorder(world, x, y, z) values (?,?,?,?);";
		removeWorldBorder = "delete from ppWorldBorder where world = ? and x = ? and y = ? and z = ?;";
		getAllWorldBorder = "select * from ppWorldBorder;";
	}

	@Override
	public void save() {
		// Doesn't need to do anything as mysql already has the record.
	}

	@Override
	public void load() {
		handler.refreshAndReconnect();
		locs.clear();
		PreparedStatement getAllWorldBorder = db.prepareStatement(this.getAllWorldBorder);
		try {
			ResultSet set = getAllWorldBorder.executeQuery();
			while (set.next()) {
				UUID uuid = UUID.fromString(set.getString("world"));
				World world = Bukkit.getWorld(uuid);
				if (world == null)
					continue;
				int x = set.getInt("x");
				int y = set.getInt("y");
				int z = set.getInt("z");
				Location loc = new Location(world, x, y, z);
				locs.add(loc);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				getAllWorldBorder.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void addWorldBorder(Location loc) {
		handler.refreshAndReconnect();
		PreparedStatement addWorldBorder = db.prepareStatement(this.addWorldBorder);
		try {
			addWorldBorder.setString(1, loc.getWorld().getUID().toString());
			addWorldBorder.setInt(2, loc.getBlockX());
			addWorldBorder.setInt(3, loc.getBlockY());
			addWorldBorder.setInt(4, loc.getBlockZ());
			addWorldBorder.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				addWorldBorder.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void removeWorldBorder(Location loc) {
		handler.refreshAndReconnect();
		PreparedStatement removeWorldBorder = db.prepareStatement(this.removeWorldBorder);
		try {
			removeWorldBorder.setString(1, loc.getWorld().getUID().toString());
			removeWorldBorder.setInt(2, loc.getBlockX());
			removeWorldBorder.setInt(3, loc.getBlockY());
			removeWorldBorder.setInt(4, loc.getBlockZ());
			removeWorldBorder.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				removeWorldBorder.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean isWorldBorderLoc(Location loc) {
		return locs.contains(loc);
	}
}

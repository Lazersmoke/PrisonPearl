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

import vg.civcraft.mc.mercury.MercuryAPI;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.prisonpearl.PrisonPearl;
import vg.civcraft.mc.prisonpearl.database.interfaces.IPrisonPearlStorage;
import vg.civcraft.mc.prisonpearl.managers.MercuryManager;
import vg.civcraft.mc.prisonpearl.managers.NameLayerManager;
import vg.civcraft.mc.prisonpearl.misc.FakeLocation;

public class PrisonPearlMysqlStorage implements IPrisonPearlStorage{

	private Database db;
	private MysqlDatabaseHandler handle;
	private boolean isMercuryEnabled = MercuryManager.isMercuryEnabled();
	private boolean isNameLayerEnabled = NameLayerManager.isNameLayerEnabled();
	
	public PrisonPearlMysqlStorage(Database db, MysqlDatabaseHandler handle) {
		this.db = db;
		this.handle = handle;
		createTables();
		initializeStatements();
	}
	
	private void createTables() {
		db.execute("create table if not exists PrisonPearls( "
				+ "uuid varchar(36) not null,"
				+ "world varchar(36) not null,"
				+ "server varchar(255) not null,"
				+ "x int not null,"
				+ "y int not null," 
				+ "z int not null,"
				+ "uq int not null,"
				+ "motd varchar(255) not null," 
				+ "primary key ids_id(uuid));");
	}
	
	private String addPearl, removePearl, getPearl, getAllPearls, updatePearl;
	
	private void initializeStatements() {
		addPearl = "insert into PrisonPearls(uuid, world, server, x, y, z, uq, motd)"
				+ "values (?, ?, ?, ?, ?, ?, ?, ?);";
		getPearl = "select * from PrisonPearls where uuid = ?;";
		getAllPearls = "select * from PrisonPearls;";
		removePearl = "delete from PrisonPearls where uuid = ?";
		updatePearl = "update PrisonPearls "
				+ "set x = ?, y = ?, z = ?, world = ?, server = ?, "
				+ "motd = ? where uuid = ?;";
	}

	@Override
	public void addPearl(PrisonPearl pp) {
		handle.refreshAndReconnect();
		PreparedStatement addPearl = db.prepareStatement(this.addPearl);
		try {
			String server = "bukkit";
			if (isMercuryEnabled)
				server = MercuryAPI.serverName();
			addPearl.setString(1, pp.getImprisonedId().toString());
			addPearl.setString(2, pp.getLocation().getWorld().getName());
			addPearl.setString(3, server);
			addPearl.setInt(4, pp.getLocation().getBlockX());
			addPearl.setInt(5, pp.getLocation().getBlockY());
			addPearl.setInt(6, pp.getLocation().getBlockZ());
			addPearl.setInt(7, pp.getUniqueIdentifier());
			addPearl.setString(8, pp.getMotd());
			addPearl.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void removePearl(PrisonPearl pp) {
		PreparedStatement removePearl = db.prepareStatement(this.removePearl);
		try {
			removePearl.setString(1, pp.getImprisonedId().toString());
			removePearl.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public PrisonPearl getPearl(UUID uuid) {
		PreparedStatement getPearl = db.prepareStatement(this.getPearl);
		try {
			getPearl.setString(1, uuid.toString());
			ResultSet set = getPearl.executeQuery();
			if (!set.next())
				return null;
			String w = set.getString("world");
			World world = Bukkit.getWorld(w);
			String server = set.getString("server");
			int x = set.getInt("x"), y = set.getInt("y"), z = set.getInt("z");
			String motd = set.getString("motd");
			String name = "";
			int unique = set.getInt("uq");
			if (isNameLayerEnabled)
				name = NameAPI.getCurrentName(uuid);
			else
				name = Bukkit.getOfflinePlayer(uuid).getName();
			PrisonPearl pp = null;
			if (world == null)
				pp = new PrisonPearl(name, uuid, new FakeLocation(w, x, y, z, server), unique);
			else 
				pp = new PrisonPearl(name, uuid, new Location(world, x, y, z), unique);
			pp.setMotd(motd);
			return pp;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<PrisonPearl> getAllPearls() {
		PreparedStatement getAllPearls = db.prepareStatement(this.getAllPearls);
		List<PrisonPearl> pearls = new ArrayList<PrisonPearl>();
		try {
			ResultSet set = getAllPearls.executeQuery();
			while (set.next()) {
				String w = set.getString("world");
				World world = Bukkit.getWorld(w);
				String server = set.getString("server");
				int x = set.getInt("x"), y = set.getInt("y"), z = set
						.getInt("z");
				String motd = set.getString("motd");
				UUID uuid = UUID.fromString(set.getString("uuid"));
				String name = "";
				int unique = set.getInt("uq");
				if (isNameLayerEnabled)
					name = NameAPI.getCurrentName(uuid);
				else
					name = Bukkit.getOfflinePlayer(uuid).getName();
				PrisonPearl pp = null;
				if (world == null)
					pp = new PrisonPearl(name, uuid, new FakeLocation(w, x, y, z, server), unique);
				else 
					pp = new PrisonPearl(name, uuid, new Location(world, x, y, z), unique);
				pp.setMotd(motd);
				pearls.add(pp);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pearls;
	}

	@Override
	public void updatePearl(PrisonPearl pp) {
		Location loc = pp.getLocation();
		if (loc instanceof FakeLocation)
			return;
		PreparedStatement updatePearl = db.prepareStatement(this.updatePearl);
		try {
			updatePearl.setInt(1, loc.getBlockX());
			updatePearl.setInt(2, loc.getBlockY());
			updatePearl.setInt(3, loc.getBlockZ());
			updatePearl.setString(4, loc.getWorld().getName());
			updatePearl.setString(5, pp.getMotd());
			updatePearl.setString(6, pp.getImprisonedId().toString());
			updatePearl.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

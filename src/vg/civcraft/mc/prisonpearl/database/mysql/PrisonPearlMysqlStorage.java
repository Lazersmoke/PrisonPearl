package vg.civcraft.mc.prisonpearl.database.mysql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import vg.civcraft.mc.mercury.MercuryAPI;
import vg.civcraft.mc.prisonpearl.PrisonPearl;
import vg.civcraft.mc.prisonpearl.PrisonPearlPlugin;
import vg.civcraft.mc.prisonpearl.database.interfaces.IPrisonPearlStorage;
import vg.civcraft.mc.prisonpearl.managers.NameLayerManager;
import vg.civcraft.mc.prisonpearl.misc.FakeLocation;

public class PrisonPearlMysqlStorage implements IPrisonPearlStorage{

	private Database db;
	private MysqlDatabaseHandler handle;
	
	private boolean isMercuryEnabled = PrisonPearlPlugin.isMercuryEnabled();
	
	public PrisonPearlMysqlStorage(Database db, MysqlDatabaseHandler handle) {
		this.db = db;
		this.handle = handle;
		createTables();
		updateTables();
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
				+ "motd varchar(255)," 
				+ "primary key ids_id(uuid));");
		db.execute("create table if not exists FeedDelay("
				+ "lastRestart bigint not null default 0,"
				+ "server varchar(255) not null);");
		db.execute("create table if not exists db_version (db_version int not null," 
				+ "update_time varchar(24),"
				+ "plugin_name varchar(40));");
	}
	
	private void updateTables() {
		PreparedStatement getDBVersion = db.prepareStatement("select max(db_version) from db_version where plugin_name=?;");
		PreparedStatement updateVersion = db.prepareStatement("insert into db_version (db_version, update_time, plugin_name) values(?,?,?);"); 
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		int version = -1;
		try {
			getDBVersion.setString(1, "PrisonPearl");
			updateVersion.setString(3, "PrisonPearl");
			ResultSet set = getDBVersion.executeQuery();
			if (set.next()) {
				version = set.getInt(1);
			}
			else {
				updateVersion.setInt(1, 0);
				updateVersion.setString(2, sdf.format(new Date()));
				updateVersion.execute();
				version = 0;
			}
		}
		catch (SQLException e) {
			PrisonPearlPlugin.getInstance().warning("Failed to retrieve db version");
			e.printStackTrace();
			return;
		}
		if (version == 0) {
			PrisonPearlPlugin.getInstance().info("Updating database to version 1");
			db.execute("alter table PrisonPearls add killer varchar(36)");
			db.execute("alter table PrisonPearls add pearlTime bigint default -1");
			version = 1;
			try {
				updateVersion.setInt(1, 1);
				updateVersion.setString(2, sdf.format(new Date()));
				updateVersion.execute();
			} catch (SQLException e) {
				PrisonPearlPlugin.getInstance().severe("Failed to insert db update to version 1");
				e.printStackTrace();
			}
		}
	}
	
	private String addPearl, removePearl, getPearl, getAllPearls, updatePearl;
	private String updateLastRestart, getLastRestart, insertFirstRestart;
	
	private void initializeStatements() {
		addPearl = "insert into PrisonPearls(uuid, world, server, x, y, z, uq, motd, killer, pearlTime)"
				+ "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
		getPearl = "select * from PrisonPearls where uuid = ?;";
		getAllPearls = "select * from PrisonPearls;";
		removePearl = "delete from PrisonPearls where uuid = ?";
		updatePearl = "update PrisonPearls "
				+ "set x = ?, y = ?, z = ?, world = ?, server = ?, "
				+ "motd = ? where uuid = ?;";
		
		insertFirstRestart = "insert into FeedDelay(lastRestart, server) values(?, ?);";
		updateLastRestart = "update FeedDelay "
				+ "set lastRestart = ? where server = ?;";
		getLastRestart = "select * from FeedDelay where server = ?";
	}
	
	private Map<UUID, PrisonPearl> pearls = new HashMap<UUID, PrisonPearl>();
	private long lastFeed;

	@Override
	public void addPearl(PrisonPearl pp) {
		if (isImprisoned(pp.getImprisonedId())) {
			pearls.put(pp.getImprisonedId(), pp);
		}
		
		if (pp.getLocation() instanceof FakeLocation)
			return;
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
			addPearl.setString(9, pp.getKillerUUID() == null ? null : pp.getKillerUUID().toString());
			addPearl.setLong(10, pp.getImprisonTime());
			addPearl.execute();
			pearls.put(pp.getImprisonedId(), pp);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void removePearl(PrisonPearl pp, String reason) {
		handle.refreshAndReconnect();
		PrisonPearlPlugin.log(reason);
		if (pp == null)
			return;
		pearls.remove(pp.getImprisonedId());
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
	public void load() {
		handle.refreshAndReconnect();
		PreparedStatement getAllPearls = db.prepareStatement(this.getAllPearls);
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
				int unique = set.getInt("uq");
				UUID killerUUID;
				String killerUUIDAsString = set.getString("killer");
				if (killerUUIDAsString == null) {
					killerUUID = null;
				}
				else {
					killerUUID = UUID.fromString(killerUUIDAsString);
				}
				Long imprisonTime = set.getLong("pearlTime");
				if (imprisonTime == null) {
					imprisonTime = -1L;
				}
				String name = NameLayerManager.getName(uuid);
				PrisonPearl pp = null;
				if (world == null)
					pp = new PrisonPearl(name, uuid, new FakeLocation(w, x, y, z, server), unique, killerUUID, imprisonTime);
				else 
					pp = new PrisonPearl(name, uuid, new Location(world, x, y, z), unique, killerUUID, imprisonTime);
				pp.setMotd(motd);
				pearls.put(pp.getImprisonedId(), pp);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void save() {
		for (PrisonPearl p: getAllPearls()) {
			if (p.getLocation() instanceof FakeLocation)
				continue;
			updatePearl(p);
		}
	}

	@Override
	public Collection<PrisonPearl> getAllPearls() {
		return pearls.values();
	}

	@Override
	public void updatePearl(PrisonPearl pp) {
		handle.refreshAndReconnect();
		Location loc = pp.getLocation();
		if (loc instanceof FakeLocation)
			return;
		PreparedStatement updatePearl = db.prepareStatement(this.updatePearl);
		try {
			updatePearl.setInt(1, loc.getBlockX());
			updatePearl.setInt(2, loc.getBlockY());
			updatePearl.setInt(3, loc.getBlockZ());
			updatePearl.setString(4, loc.getWorld().getName());
			updatePearl.setString(5, MercuryAPI.serverName());
			updatePearl.setString(6, pp.getMotd());
			updatePearl.setString(7, pp.getImprisonedId().toString());
			updatePearl.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void updateLastFeed(long lastFeed) {
		handle.refreshAndReconnect();
		this.lastFeed = lastFeed;
		String server = "bukkit";
		if (isMercuryEnabled)
			server = MercuryAPI.serverName();
		PreparedStatement updateLastRestart = db.prepareStatement(this.updateLastRestart);
		try {
			updateLastRestart.setLong(1, lastFeed);
			updateLastRestart.setString(2, server);
			updateLastRestart.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets the last time the server was restarted when pearl feeding occurred.
	 */
	@Override
	public long getLastFeed() {
		handle.refreshAndReconnect();
		if (lastFeed != 0)
			return lastFeed;
		try {
			String server = "bukkit";
			if (isMercuryEnabled)
				server = MercuryAPI.serverName();
			PreparedStatement getLastRestart = db.prepareStatement(this.getLastRestart);
			getLastRestart.setString(1, server);
			ResultSet set = getLastRestart.executeQuery();
			if (!set.next()) {
				PreparedStatement insertFirstRestart = db.prepareStatement(this.insertFirstRestart);
				insertFirstRestart.setLong(1, System.currentTimeMillis());
				insertFirstRestart.setString(2, server);
				insertFirstRestart.execute();
				return getLastFeed();
			}
			lastFeed = set.getLong("lastRestart");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return lastFeed;
	}
	
	@Override
	public boolean isImprisoned(UUID uuid) {
		return getByImprisoned(uuid) != null;
	}
	
	@Override
	public boolean isImprisoned(Player p) {
		return getByImprisoned(p.getUniqueId()) != null;
	}
	
	@Override
	public Integer getImprisonedCount(UUID[] ids) {
		Integer count = 0;
		for (UUID id : ids) {
			if (isImprisoned(id)) {
				count++;
			}
		}
		return count;
	}
	
	@Override
	public Collection <UUID> getImprisonedIds(UUID[] ids) {
		List<UUID> imdIds = new ArrayList<UUID>();
		for (UUID id : ids) {
			if (isImprisoned(id)) {
				imdIds.add(id);
			}
		}
		return imdIds;
	}
	
	@Override
	public PrisonPearl getByImprisoned(Player player) {
		return getByImprisoned(player.getUniqueId());
	}
	
	@Override
	public PrisonPearl newPearl(OfflinePlayer imprisoned, Player imprisoner) {
		return newPearl(imprisoned.getName(), imprisoned.getUniqueId(), imprisoner);
	}
	
	@Override
	public PrisonPearl newPearl(String imprisonedName, UUID imprisonedId, Player imprisoner) {
		Random rand = new Random();
		PrisonPearl pp = new PrisonPearl(imprisonedName, imprisonedId, imprisoner, rand.nextInt(1000000000), imprisoner.getUniqueId(), System.currentTimeMillis());
		addPearl(pp);
		pp.setHolder(imprisoner); // This will set the holder to something valid so it can correctly send it out.
		pp.markMove();
		return pp;
	}

	@Override
	public PrisonPearl getByImprisoned(UUID uuid) {
		handle.refreshAndReconnect();
		PrisonPearl pp = null;
		if ((pp = pearls.get(uuid)) != null)
			return pp;
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
			String name = NameLayerManager.getName(uuid);
			int unique = set.getInt("uq");
			UUID killerUUID;
			String killerUUIDAsString = set.getString("killer");
			if (killerUUIDAsString == null) {
				killerUUID = null;
			}
			else {
				killerUUID = UUID.fromString(killerUUIDAsString);
			}
			Long imprisonTime = set.getLong("pearlTime");
			if (imprisonTime == null) {
				imprisonTime = -1L;
			}
			if (world == null)
				pp = new PrisonPearl(name, uuid, new FakeLocation(w, x, y, z, server), unique, killerUUID, imprisonTime);
			else 
				pp = new PrisonPearl(name, uuid, new Location(world, x, y, z), unique, killerUUID, imprisonTime);
			pp.setMotd(motd);
			return pp;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}

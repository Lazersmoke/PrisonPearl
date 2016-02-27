package vg.civcraft.mc.prisonpearl.database.mysql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import vg.civcraft.mc.prisonpearl.database.interfaces.IPrisonPortaledStorage;

public class PrisonPortaledMysqlStorage implements IPrisonPortaledStorage{

	private Database db;
	private MysqlDatabaseHandler handle;
	
	public PrisonPortaledMysqlStorage(Database db, MysqlDatabaseHandler handle) {
		this.db = db;
		this.handle = handle;
		createTables();
		initializeStatements();
	}
	
	private void createTables() {
		db.execute("create table if not exists PrisonPearlPortaled("
				+ "uuid varchar(36) not null,"
				+ "primary key uuid_key(uuid));");
	}

	private String addPortaledPlayer, removePortaledPlayer, getAllPortaledPlayers;
	
	private void initializeStatements() {
		addPortaledPlayer = "insert ignore into PrisonPearlPortaled(uuid) values(?);";
		removePortaledPlayer = "delete from PrisonPearlPortaled where uuid = ?;";
		getAllPortaledPlayers = "select * from PrisonPearlPortaled;";
	}
	
	private List<UUID> portaledPlayers = new ArrayList<UUID>();
	
	@Override
	public void addPortaledPlayer(UUID uuid) {
		portaledPlayers.add(uuid);
		handle.refreshAndReconnect();
		PreparedStatement addPortaledPlayer = db.prepareStatement(this.addPortaledPlayer);
		try {
			addPortaledPlayer.setString(1, uuid.toString());
			addPortaledPlayer.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				addPortaledPlayer.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void removePortaledPlayer(UUID uuid) {
		portaledPlayers.remove(uuid);
		handle.refreshAndReconnect();
		PreparedStatement removePortaledPlayer = db.prepareStatement(this.removePortaledPlayer);
		try {
			removePortaledPlayer.setString(1, uuid.toString());
			removePortaledPlayer.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				removePortaledPlayer.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public List<UUID> getAllPortaledPlayers() {
		handle.refreshAndReconnect();
		PreparedStatement getAllPortaledPlayers = db.prepareStatement(this.getAllPortaledPlayers);
		try {
			ResultSet set = getAllPortaledPlayers.executeQuery();
			portaledPlayers.clear();
			while(set.next()){
				portaledPlayers.add(UUID.fromString(set.getString("uuid")));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				getAllPortaledPlayers.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return portaledPlayers;
	}
	
	@Override
	public boolean isPortaledPlayer(UUID uuid) {
		return portaledPlayers.contains(uuid);
	}

	@Override
	public void save() {
		// Don't need to save mysql already saved.
	}

	@Override
	public void load() {
		handle.refreshAndReconnect();
		getAllPortaledPlayers();
	}
}

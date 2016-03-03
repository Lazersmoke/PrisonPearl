package vg.civcraft.mc.prisonpearl.database.mysql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Furnace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import vg.civcraft.mc.civmodcore.annotations.CivConfig;
import vg.civcraft.mc.civmodcore.annotations.CivConfigType;
import vg.civcraft.mc.civmodcore.annotations.CivConfigs;
import vg.civcraft.mc.mercury.MercuryAPI;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.prisonpearl.PrisonPearl;
import vg.civcraft.mc.prisonpearl.PrisonPearlConfig;
import vg.civcraft.mc.prisonpearl.PrisonPearlPlugin;
import vg.civcraft.mc.prisonpearl.database.interfaces.IPrisonPearlStorage;
import vg.civcraft.mc.prisonpearl.managers.MercuryManager;
import vg.civcraft.mc.prisonpearl.managers.NameLayerManager;
import vg.civcraft.mc.prisonpearl.managers.PrisonPearlManager;
import vg.civcraft.mc.prisonpearl.misc.FakeLocation;

public class PrisonPearlMysqlStorage implements IPrisonPearlStorage{

	private Database db;
	private PrisonPearlPlugin plugin;
	private MysqlDatabaseHandler handle;
	
	private boolean isMercuryEnabled = PrisonPearlPlugin.isMercuryEnabled();
	private boolean isWorldBorderEnabled = PrisonPearlPlugin.isWorldBorderEnabled();
	
	public PrisonPearlMysqlStorage(Database db, MysqlDatabaseHandler handle) {
		this.db = db;
		this.handle = handle;
		plugin = PrisonPearlPlugin.getInstance();
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
				+ "motd varchar(255)," 
				+ "primary key ids_id(uuid));");
		db.execute("create table if not exists FeedDelay("
				+ "lastRestart bigint not null default 0,"
				+ "server varchar(255) not null);");
	}
	
	private String addPearl, removePearl, getPearl, getAllPearls, updatePearl;
	private String updateLastRestart, getLastRestart, insertFirstRestart;
	
	private void initializeStatements() {
		addPearl = "insert into PrisonPearls(uuid, world, server, x, y, z, uq, motd)"
				+ "values (?, ?, ?, ?, ?, ?, ?, ?);";
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
	
	private final int HolderStateToInventory_SUCCESS = 0;
	private final int HolderStateToInventory_BADPARAM = 1;
	private final int HolderStateToInventory_NULLSTATE = 2;
	private final int HolderStateToInventory_BADCONTAINER = 3;
	private final int HolderStateToInventory_NULLINV = 4;
	
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
			addPearl.execute();
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
				String name = NameLayerManager.getName(uuid);
				PrisonPearl pp = null;
				if (world == null)
					pp = new PrisonPearl(name, uuid, new FakeLocation(w, x, y, z, server), unique);
				else 
					pp = new PrisonPearl(name, uuid, new Location(world, x, y, z), unique);
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
			updatePearl.setString(5, pp.getMotd());
			updatePearl.setString(6, pp.getImprisonedId().toString());
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
	public UUID[] getImprisonedIds(UUID[] ids) {
		List<UUID> imdIds = new ArrayList<UUID>();
		for (UUID id : ids) {
			if (isImprisoned(id)) {
				imdIds.add(id);
			}
		}
		int count = imdIds.size();
		UUID[] results = new UUID[count];
		for (int i = 0; i < count; i++) {
			results[i] = imdIds.get(i);
		}
		return results;
	}
	
	@Override
	public String feedPearls(PrisonPearlManager pearlman) {
		String message = "";
		String log = "";
		ConcurrentHashMap<UUID,PrisonPearl> map = new ConcurrentHashMap<UUID,PrisonPearl>(pearls);

		long inactive_seconds = PrisonPearlConfig.getIgnoreFeedSecond();
		long inactive_hours = PrisonPearlConfig.getIngoreFeedHours();
		long inactive_days = PrisonPearlConfig.getIngoreFeedDays();

		long feedDelay = PrisonPearlConfig.getIgnoreFeedDelay();	//if pearls have been fed in the last x millis it wont feed, defaults to 20 hours
		if(getLastFeed() >= System.currentTimeMillis() - feedDelay) {
			return "Pearls have already been fed, not gonna do it again just yet";
		} else {
			log+="\nSetting last feed time";
			updateLastFeed(System.currentTimeMillis());
		}
		
		int pearlsfed = 0;
		int coalfed = 0;
		int freedpearls = 0;
		for (PrisonPearl pp : map.values()) {
			final UUID prisonerId = pp.getImprisonedId();
			fixAllPearlMissing(prisonerId);
			//final String prisoner = Bukkit.getPlayer(prisonerId).getName();
			Inventory inv[] = new Inventory[2];
			int retval = HolderStateToInventory(pp, inv);
			Location loc = pp.getLocation();
			if (loc instanceof FakeLocation) { // Not on server
				log+="\n" + pp.getImprisonedName() + " was skipped feeding because he is not on the current server.";
				continue; // Other server will handle feeding
			}
			if (!upgradePearl(inv[0], pp) && inv[1] != null) {
				upgradePearl(inv[1], pp);
			}
			if (retval == HolderStateToInventory_BADCONTAINER) {
				String reason = prisonerId + " is being freed. Reason: Freed during coal feed, container was corrupt.";
				pearlman.freePearl(pp, reason);
				log+="\n freed:"+prisonerId+",reason:"+"badcontainer";
				freedpearls++;
				continue;
			} else if (retval != HolderStateToInventory_SUCCESS) {
				continue;
			}
			else if (isWorldBorderEnabled && PrisonPearlPlugin.getWorldBorderManager().isMaxFeed(loc)){
				String reason = prisonerId + " is being freed. Reason: Freed during coal feed, was outside max distance.";
				pearlman.freePearl(pp, reason);
				log+="\n freed:"+prisonerId+",reason:"+"maxDistance";
				freedpearls++;
				continue;
			}
			if (inactive_seconds != 0 || inactive_hours != 0 || inactive_days != 0) {
				long inactive_time = pp.getImprisonedOfflinePlayer().getLastPlayed();
				long inactive_millis = inactive_seconds * 1000 + inactive_hours * 3600000 + inactive_days * 86400000;
				inactive_time += inactive_millis;
				if (inactive_time <= System.currentTimeMillis()) {
					// if player has not logged on in the set amount of time than ignore feeding
					log += "\nnot fed inactive: " + prisonerId;
					continue;
				}
			}
			message = message + "Pearl Id: " + prisonerId + " in a " + pp.getHolderBlockState().getType();
			ItemStack requirement = new ItemStack(PrisonPearlConfig.getResourceUpkeepMaterial(), 
					PrisonPearlConfig.getResourceUpkeepAmount());
			int requirementSize = requirement.getAmount();

			if(inv[0].containsAtLeast(requirement,requirementSize)) {
				int pearlnum;
				pearlnum = inv.length;
				message = message + "\n Chest contains enough purestrain coal.";
				inv[0].removeItem(requirement);
				pearlsfed++;
				coalfed += requirementSize;
				log+="\n fed:" + prisonerId + ",location:"+ pp.describeLocation();
			} else if(inv[1] != null && inv[1].containsAtLeast(requirement,requirementSize)){
				message = message + "\n Chest contains enough purestrain coal.";
				inv[1].removeItem(requirement);
				pearlsfed++;
				coalfed += requirementSize;
				log+="\n fed:" + prisonerId + ",location:"+ pp.describeLocation();
			} else {
				message = message + "\n Chest does not contain enough purestrain coal.";
				String reason = prisonerId + " is being freed. Reason: Freed during coal feed, container did not have enough coal.";
				pearlman.freePearl(pp, reason);
				log+="\n freed:"+prisonerId+",reason:"+"nocoal"+",location:"+pp.describeLocation();
				freedpearls++;
			}
		}
		message = message + "\n Feeding Complete. " + pearlsfed + " were fed " + coalfed + " coal. " + freedpearls + " players were freed.";
		return message;
	}
	
	@Override
	public int HolderStateToInventory(PrisonPearl pp, Inventory inv[]) {
		if (pp == null || inv == null) {
			return HolderStateToInventory_BADPARAM;
		}
		BlockState inherentViolence = pp.getHolderBlockState();
//		if (Bukkit.getPluginManager().isPluginEnabled("EnderExpansion")){
//			if (pp.getLocation().getBlock().getType() == Material.ENDER_CHEST){
//				inv[0] = Enderplugin.getchestInventory(pp.getLocation());
//				return HolderStateToInventory_SUCCESS;
//			}
//		}
		if (inherentViolence == null) {
			return HolderStateToInventory_NULLSTATE;
		}
		Material mat = inherentViolence.getType();
		
		switch(mat) {
		case FURNACE:
			inv[0] = ((Furnace)inherentViolence).getInventory();
			break;
		case DISPENSER:
			inv[0] = ((Dispenser)inherentViolence).getInventory();
			break;
		case BREWING_STAND:
			inv[0] = ((BrewingStand)inherentViolence).getInventory();
			break;
		case CHEST:
		case TRAPPED_CHEST:
			Chest c = ((Chest)inherentViolence);
			DoubleChestInventory dblInv = null;
			try {
				dblInv = (DoubleChestInventory)c.getInventory();
				inv[0] = dblInv.getLeftSide();
				inv[1] = dblInv.getRightSide();
			} catch(Exception e){
				inv[0] = c.getInventory();
			}
			break;
		default:
			return HolderStateToInventory_BADCONTAINER;
		}
		if (inv[0] == null && inv[1] == null) {
			return HolderStateToInventory_NULLINV;
		}
		return HolderStateToInventory_SUCCESS;
	}
	
	@Override
	public boolean upgradePearl(Inventory inv, PrisonPearl pp) {
		final UUID prisonerId = pp.getImprisonedId();
		final String prisoner = NameLayerManager.getName(prisonerId);
		ItemStack is = new ItemStack(Material.ENDER_PEARL, 1);
		if (inv == null) {
			return false;
		}
		for (ItemStack existing_is: inv.getContents()) {
			if (existing_is == null || existing_is.getType() != Material.ENDER_PEARL)
				continue;
			int pearlslot = inv.first(existing_is);
			if (existing_is != null) {
				existing_is.setDurability((short) 0);
				ItemMeta existing_meta = existing_is.getItemMeta();
				if (existing_meta != null) {
					String existing_name = existing_meta.getDisplayName();
					List<String> lore = existing_meta.getLore();
					if (existing_name != null && prisoner != null &&
							existing_name.compareTo(prisoner) == 0 && lore != null && lore.size() == 3) {
						// This check says all existing stuff is there so return true.
						return true;
					}
					else if (existing_name != null && 
							prisoner != null && existing_name.compareTo(prisoner) != 0) 
						// If we don't have the right pearl keep looking.
						continue;
					else if (existing_name == null)
						// This pearl can't even be right so just return.
						return true;
				}
			}
			ItemMeta im = is.getItemMeta(); 
			// Rename pearl to that of imprisoned player 
			im.setDisplayName(prisoner);
			List<String> lore = new ArrayList<String>(); 
			lore.add(prisoner + " is held within this pearl");
			lore.add("UUID: " + pp.getImprisonedId().toString());
			lore.add("Unique: " + pp.getUniqueIdentifier());
			// Given enchantment effect
			// Durability used because it doesn't affect pearl behaviour
			im.addEnchant(Enchantment.DURABILITY, 1, true);
			im.setLore(lore);
			is.setItemMeta(im);
			inv.clear(pearlslot);
			inv.setItem(pearlslot, is);
			return true;
		}
		return false;
	}
	
	/**
	 * @param itemStack - the item stack to check for being a PrisonPearl.
	 * @return true, if itemStack is a PrisonPearl, false otherwise.
	 */
	@Override
	public boolean isPrisonPearl(ItemStack itemStack) {
		return getPearlbyItemStack(itemStack) != null;
	}
	
	@Override
	public PrisonPearl getByImprisoned(Player player) {
		return getByImprisoned(player.getUniqueId());
	}
	
	@CivConfig(name = "fixMissingPearls", def = "false", type = CivConfigType.Bool)
	private void fixAllPearlMissing(UUID uuid) {
    	if (!plugin.GetConfig().get("fixMissingPearls").getBool())
    		return;
    	PrisonPearl pp = getByImprisoned(uuid);
    	Location loc = pp.getLocation();
    	Block b = pp.getLocation().getBlock();
    	Inventory[] inv = new Inventory[2];
    	BlockState inherentViolence = pp.getHolderBlockState();
    	// Grabs the inventories.
    	switch(b.getType()) {
		case FURNACE:
			inv[0] = ((Furnace)inherentViolence).getInventory();
			break;
		case DISPENSER:
			inv[0] = ((Dispenser)inherentViolence).getInventory();
			break;
		case BREWING_STAND:
			inv[0] = ((BrewingStand)inherentViolence).getInventory();
			break;
		case CHEST:
		case TRAPPED_CHEST:
			Chest c = ((Chest)inherentViolence);
			DoubleChestInventory dblInv = null;
			try {
				dblInv = (DoubleChestInventory)c.getInventory();
				inv[0] = dblInv.getLeftSide();
				inv[1] = dblInv.getRightSide();
			} catch(Exception e){
				inv[0] = c.getInventory();
			}
			break;
		default:
			inv[0] = null;
			inv[1] = null;
		}
		ItemStack stack = null;
		// Scans the inventories looking for the prisonpearl.
    	for (Inventory i: inv) {
    		if (i == null)
    			continue;
    		for (int x = 0; x < i.getSize(); x++) {
    			ItemStack s = i.getItem(x);
    			if (s == null || s.getType() != Material.ENDER_PEARL)
    				continue;
    			PrisonPearl tmp = getPearlbyItemStack(s);
    			if (tmp == null)
    				continue;
    			if (tmp.getImprisonedId().equals(uuid)) {
    				stack = s;
    				break;
    			}
    		}
    		if (stack != null)
    			break;
    	}
    	if (stack == null)
    		for (Inventory i: inv) {
            	if (stack != null)
        			break;
        		for (int x = 0; x < i.getSize(); x++) {
        			ItemStack current = i.getItem(x);
        			if (getPearlbyItemStack(current) == null) {
        				removePearl(pp, "Regenerating pearl cause it was lost. UUID is: " + pp.getImprisonedId().toString());
        				String name = NameLayerManager.getName(uuid);
        				pp = new PrisonPearl(name, uuid, loc, new Random().nextInt(1000));
        				addPearl(pp);
        				ItemStack is = new ItemStack(Material.ENDER_PEARL, 1);
        				ItemMeta im = is.getItemMeta();
        				// Rename pearl to that of imprisoned player
        				im.setDisplayName(name);
        				List<String> lore = new ArrayList<String>();
        				// Gives pearl lore that says more info when hovered over
        				lore.add(name + " is held within this pearl");
        				lore.add("UUID: "+pp.getImprisonedId());
        				lore.add("Unique: " + pp.getUniqueIdentifier());
        				// Given enchantment effect (durability used because it doesn't affect pearl behaviour)
        				im.addEnchant(Enchantment.DURABILITY, 1, true);
        				im.setLore(lore);
        				is.setItemMeta(im);
        				i.clear(x);
        				i.setItem(x, is);
        				stack = is;
        				break;
        			}
        		}
    		}
    }
	
	@Override
	public PrisonPearl getPearlbyItemStack(ItemStack stack) {
    	if (stack == null || !stack.hasItemMeta() || stack.getType() != Material.ENDER_PEARL)
    		return null;
    	if (!stack.getItemMeta().hasLore())
    		return null;
    	List<String> lore = stack.getItemMeta().getLore();
    	if (lore.size() != 3)
    		return null;
    	UUID uuid = UUID.fromString(lore.get(1).split(" ")[1]);
    	PrisonPearl pp = getByImprisoned(uuid);
    	if (pp == null){
    		stack.setItemMeta(null);
    		return null;
    	}
    	int id = Integer.parseInt(lore.get(2).split(" ")[1]);
    	boolean isValid = uuid.equals(pp.getImprisonedId()) && id == pp.getUniqueIdentifier();
		if (!isValid) {
			stack.setItemMeta(null);
			pp = null;
		}
    	return pp;
    }
	
	@Override
	public PrisonPearl newPearl(OfflinePlayer imprisoned, Player imprisoner) {
		return newPearl(imprisoned.getName(), imprisoned.getUniqueId(), imprisoner);
	}
	
	@Override
	public PrisonPearl newPearl(String imprisonedName, UUID imprisonedId, Player imprisoner) {
		Random rand = new Random();
		PrisonPearl pp = new PrisonPearl(imprisonedName, imprisonedId, imprisoner, rand.nextInt(1000000000));
		addPearl(pp);
		pp.setHolder(imprisoner.getLocation()); // This will set the holder to something valid so it can correctly send it out.
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
}

package vg.civcraft.mc.prisonpearl.database.flatfile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
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
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.prisonpearl.PrisonPearl;
import vg.civcraft.mc.prisonpearl.PrisonPearlConfig;
import vg.civcraft.mc.prisonpearl.PrisonPearlPlugin;
import vg.civcraft.mc.prisonpearl.database.interfaces.IPrisonPearlStorage;
import vg.civcraft.mc.prisonpearl.managers.NameLayerManager;
import vg.civcraft.mc.prisonpearl.managers.PrisonPearlManager;
import vg.civcraft.mc.prisonpearl.misc.FakeLocation;

public class PrisonPearlFileStorage implements IPrisonPearlStorage{

	private boolean isNameLayer;
	private boolean isWorldBorderEnabled;
	
	private final Map<UUID, PrisonPearl> pearls_byimprisoned;
	private long lastFeed = 0;
	private File file;
	
	private final int HolderStateToInventory_SUCCESS = 0;
	private final int HolderStateToInventory_BADPARAM = 1;
	private final int HolderStateToInventory_NULLSTATE = 2;
	private final int HolderStateToInventory_BADCONTAINER = 3;
	private final int HolderStateToInventory_NULLINV = 4;
	
	public PrisonPearlFileStorage() {
		isNameLayer = PrisonPearlPlugin.isNameLayerEnabled();
		isWorldBorderEnabled = PrisonPearlPlugin.isWorldBorderEnabled();
		pearls_byimprisoned = new HashMap<UUID, PrisonPearl>();
		
		file = new File(PrisonPearlPlugin.getInstance().getDataFolder(), "prisonpearlsUUID.txt");
	}
	
	@Override
	public void addPearl(PrisonPearl pp) {
		// Add them to the hashmap, they get saved when saved method is called.
		pearls_byimprisoned.put(pp.getImprisonedId(), pp);
	}

	@Override
	public void removePearl(PrisonPearl pp, String reason) {
		pearls_byimprisoned.remove(pp.getImprisonedId());
		PrisonPearlPlugin.log(reason);
	}

	@Override
	public Collection<PrisonPearl> getAllPearls() {
		return pearls_byimprisoned.values();
	}

	@Override
	public void load() {
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		String line;
		try {
			FileInputStream fis = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			while ((line = br.readLine()) != null) {
				if(line.matches("lastFeed:([0-9]+)")) {
					lastFeed = Long.parseLong(line.split(":")[1]);
					continue;
				}
				String parts[] = line.split(" ");
				if (parts.length <= 1)
					continue;
				UUID imprisoned = UUID.fromString(parts[0]);
				Location loc = new Location(Bukkit.getWorld(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), Integer.parseInt(parts[4]));
				String name = "";
				if (isNameLayer)
					name = NameAPI.getCurrentName(imprisoned);
				else
					name = Bukkit.getOfflinePlayer(imprisoned).getName();
				int unique = Integer.parseInt(parts[5]);
				PrisonPearl pp = PrisonPearl.makeFromLocation(name, imprisoned, loc, unique);
				if (parts.length > 6) {
					String motd = "";
					for (int i = 6; i < parts.length; i++) {
						motd = motd.concat(parts[i] + " ");
					}
					if (pp != null)
						pp.setMotd(motd);
				}
				if (pp == null) {
					System.err.println("PrisonPearl for " + imprisoned + " didn't validate, so is now set free. Chunks and/or prisonpearls.txt are corrupt");
					continue;
				}
				
				addPearl(pp);
			}
			fis.close();
		} catch (NumberFormatException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	@Override
	public void save() {
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			FileOutputStream fos = new FileOutputStream(file);
			BufferedWriter br = new BufferedWriter(new OutputStreamWriter(fos));
			
			for (PrisonPearl pp : pearls_byimprisoned.values()) {
				if (pp.getHolderBlockState() == null)
					continue;
				
				Location loc = pp.getLocation();
				br.append(pp.getImprisonedId().toString());
				br.append(" ");
				br.append(loc.getWorld().getName());
				br.append(" ");
				br.append(String.valueOf(loc.getBlockX()));
				br.append(" ");
				br.append(String.valueOf(loc.getBlockY()));
				br.append(" ");
				br.append(String.valueOf(loc.getBlockZ()));
				br.append(" ");
				br.append(String.valueOf(pp.getUniqueIdentifier()));
				br.append(" ");
				br.append(pp.getMotd());
				br.append("\n");
			}
			
			br.write("lastFeed:" + lastFeed);
			br.flush();
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void updatePearl(PrisonPearl pp) {
		// We do not use this for flat files.
	}

	@Override
	public boolean isImprisoned(UUID uuid) {
		return pearls_byimprisoned.containsKey(uuid);
	}

	@Override
	public boolean isImprisoned(Player p) {
		return isImprisoned(p.getUniqueId());
	}

	@Override
	public Integer getImprisonedCount(UUID[] ids) {
		return pearls_byimprisoned.size();
	}

	@Override
	public UUID[] getImprisonedIds(UUID[] ids) {
		return (UUID[]) pearls_byimprisoned.keySet().toArray();
	}

	@Override
	public void updateLastFeed(long lastFeed) {
		this.lastFeed = lastFeed;
	}

	@Override
	public long getLastFeed() {
		return lastFeed;
	}

	@Override
	public String feedPearls(PrisonPearlManager pearlman) {
		String message = "";
		String log = "";
		ConcurrentHashMap<UUID,PrisonPearl> map = new ConcurrentHashMap<UUID,PrisonPearl>(pearls_byimprisoned);

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
	public int HolderStateToInventory(PrisonPearl pp, Inventory[] inv) {
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

	@Override
	public boolean isPrisonPearl(ItemStack itemStack) {
		return getPearlbyItemStack(itemStack) != null;
	}

	@Override
	public PrisonPearl getByImprisoned(Player player) {
		return getByImprisoned(player.getUniqueId());
	}

	@Override
	public PrisonPearl getByImprisoned(UUID uuid) {
		return pearls_byimprisoned.get(uuid);
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
	public PrisonPearl newPearl(String imprisonedName, UUID imprisonedId, Player imprisoner) {
		Random rand = new Random();
		PrisonPearl pp = new PrisonPearl(imprisonedName, imprisonedId, imprisoner, rand.nextInt(1000000000));
		addPearl(pp);
		pp.setHolder(imprisoner); // This will set the holder to something valid so it can correctly send it out.
		pp.markMove();
		return pp;
	}

	@Override
	public PrisonPearl newPearl(OfflinePlayer imprisoned, Player imprisoner) {
		return newPearl(imprisoned.getName(), imprisoned.getUniqueId(), imprisoner);
	}
	
	@CivConfig(name = "fixMissingPearls", def = "false", type = CivConfigType.Bool)
	private void fixAllPearlMissing(UUID uuid) {
    	if (!PrisonPearlPlugin.getInstance().GetConfig().get("fixMissingPearls").getBool())
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

}

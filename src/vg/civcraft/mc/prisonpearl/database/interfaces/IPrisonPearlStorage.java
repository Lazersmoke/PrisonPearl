package vg.civcraft.mc.prisonpearl.database.interfaces;

import java.util.Collection;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.prisonpearl.PrisonPearl;
import vg.civcraft.mc.prisonpearl.managers.PrisonPearlManager;

public interface IPrisonPearlStorage {

	public void addPearl(PrisonPearl pp);
	public void removePearl(PrisonPearl pp, String reason);
	public Collection<PrisonPearl> getAllPearls();
	public void load();
	public void save();
	public void updatePearl(PrisonPearl pp);
	public boolean isImprisoned(UUID uuid);
	public boolean isImprisoned(Player p);
	public Integer getImprisonedCount(UUID[] ids);
	public UUID[] getImprisonedIds(UUID[] ids);
	public void updateLastFeed(long lastFeed);
	public long getLastFeed();
	public String feedPearls(PrisonPearlManager pearlman);
	public int HolderStateToInventory(PrisonPearl pp, Inventory inv[]);
	public boolean upgradePearl(Inventory inv, PrisonPearl pp);
	public boolean isPrisonPearl(ItemStack itemStack);
	public PrisonPearl getByImprisoned(Player player);
	public PrisonPearl getByImprisoned(UUID uuid);
	public PrisonPearl getPearlbyItemStack(ItemStack stack);
	public PrisonPearl newPearl(String imprisonedName, UUID imprisonedId, Player imprisoner);
	public PrisonPearl newPearl(OfflinePlayer imprisoned, Player imprisoner);
}

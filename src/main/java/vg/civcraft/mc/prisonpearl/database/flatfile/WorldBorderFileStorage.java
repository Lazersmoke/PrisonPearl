package vg.civcraft.mc.prisonpearl.database.flatfile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import vg.civcraft.mc.prisonpearl.PrisonPearlPlugin;
import vg.civcraft.mc.prisonpearl.database.interfaces.IWorldBorderStorage;

public class WorldBorderFileStorage implements IWorldBorderStorage{

	private File file;
	
	private List<Location> locs = new ArrayList<Location>();
	
	public WorldBorderFileStorage() {
		file = new File(PrisonPearlPlugin.getInstance().getDataFolder(), "whitelistedlocations.txt");
	}
	
	@Override
	public void save() {
		try {
			FileOutputStream  fos = new FileOutputStream (file);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			bw.append("whitelistedlocations File");
			bw.append("\n");
			for(Location loc : locs) {
				bw.append(loc.getWorld().getName() +","+loc.getX()+","+loc.getY()+","+loc.getZ());
				bw.append("\n");
			}
			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void load() {
		if (file.exists()) {
			try {
				FileInputStream fis = new FileInputStream(file);
				BufferedReader br = new BufferedReader(new InputStreamReader(fis));
				PrisonPearlPlugin.getInstance().getLogger().log(Level.INFO, file.getName());
				if(br.readLine() == null) {
					PrisonPearlPlugin.getInstance().getLogger().log(Level.INFO, file.getName()+ " file is empty");;
					br.close();
					return;
				}
				String line;
				while ((line = br.readLine()) != null) {
					PrisonPearlPlugin.getInstance().getLogger().log(Level.INFO, ("Reading curLine: " + line));
					String parts[] = line.split(",");
					if (parts.length == 3) {
						locs.add(new Location(Bukkit.getWorlds().get(0), Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), 
								Integer.parseInt(parts[2])));
					} else if (parts.length == 4) {
						locs.add(new Location(Bukkit.getWorld(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), 
								Integer.parseInt(parts[3])));
					}
				}
				br.close();
				fis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void addWorldBorder(Location loc) {
		locs.add(loc);
	}

	@Override
	public void removeWorldBorder(Location loc) {
		locs.remove(loc);
	}

	@Override
	public boolean isWorldBorderLoc(Location loc) {
		return locs.contains(loc);
	}

}

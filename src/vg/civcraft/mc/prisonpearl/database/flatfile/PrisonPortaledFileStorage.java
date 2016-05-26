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
import java.util.List;
import java.util.UUID;

import vg.civcraft.mc.prisonpearl.PrisonPearlPlugin;
import vg.civcraft.mc.prisonpearl.database.interfaces.IPrisonPortaledStorage;

public class PrisonPortaledFileStorage implements IPrisonPortaledStorage{

	private File file;
	
	private List<UUID> portaledPlayers = new ArrayList<UUID>();
	
	public PrisonPortaledFileStorage() {
		file = new File(PrisonPearlPlugin.getInstance().getDataFolder(), "portaledplayersUUID.txt");
	}
	
	@Override
	public void addPortaledPlayer(UUID uuid) {
		portaledPlayers.add(uuid);
	}

	@Override
	public void removePortaledPlayer(UUID uuid) {
		portaledPlayers.remove(uuid);
	}

	@Override
	public List<UUID> getAllPortaledPlayers() {
		return portaledPlayers;
	}

	@Override
	public boolean isPortaledPlayer(UUID uuid) {
		return portaledPlayers.contains(uuid);
	}

	@Override
	public void save() {
		try {
			FileOutputStream fos = new FileOutputStream(file);
			BufferedWriter br = new BufferedWriter(new OutputStreamWriter(fos));
			
			for (UUID id: portaledPlayers) {
				br.append(id.toString()).append("\n");
			}
			
			br.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void load() {
		try {
			FileInputStream fis = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			
			String id;
			while ((id = br.readLine()) != null) {
				portaledPlayers.add(UUID.fromString(id));
			}
			
			fis.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

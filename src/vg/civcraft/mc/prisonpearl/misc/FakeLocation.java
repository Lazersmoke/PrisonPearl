package vg.civcraft.mc.prisonpearl.misc;

import org.bukkit.Location;

public class FakeLocation extends Location{

	private String world;
	private double x, y, z;
	private String server;
	
	public FakeLocation(String world, double x, double y, double z, String server) {
		super(null, x, y, z);
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.server = server;
	}
	
	public String getWorldName() {
		return world;
	}
	
	public String getServerName() {
		return server;
	}
	
	@Override
	public Location clone() {
		return null;
	}
	
	@Override
	public double getX() {
		return x;
	}
	
	@Override
	public double getY() {
		return y;
	}
	
	@Override
	public double getZ() {
		return z;
	}
	
	@Override
	public int getBlockX() {
		return (int) x;
	}
	
	@Override
	public int getBlockY() {
		return (int) y;
	}
	
	@Override
	public int getBlockZ() {
		return (int) z;
	}
}

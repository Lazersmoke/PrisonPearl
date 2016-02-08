package vg.civcraft.mc.prisonpearl.misc;

import org.bukkit.Location;

public class FakeLocation extends Location{

	private String world;
	private double x, y, z;
	private String server;
	private String player;
	
	public FakeLocation(String world, double x, double y, double z, String server, String player) {
		super(null, x, y, z);
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.server = server;
		this.player = player;
	}
	
	public FakeLocation(String world, double x, double y, double z, String server) {
		this(world, x, y, z, server, null);
	}
	
	public String getWorldName() {
		return world;
	}
	
	public String getServerName() {
		return server;
	}
	
	public String getPlayer() {
		return player;
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

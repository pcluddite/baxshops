package qs.swornshop.serialization;

import java.io.Serializable;

import org.bukkit.Location;

import qs.swornshop.Main;

/**
 * A BlockLocation is a Location suitable for serialization.
 */
public class BlockLocation implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Creates a BlockLocation from a {@link Location}.
	 * @param loc the location
	 */
	public BlockLocation(Location loc) {
		x = loc.getBlockX();
		y = loc.getBlockY();
		z = loc.getBlockZ();
		worldName = loc.getWorld().getName();
	}
	
	int x, y, z;
	String worldName;

	/**
	 * Convert this BlockLocation back to a Location
	 * @return
	 */
	public Location toLocation() {
		return new Location(Main.instance.getServer().getWorld(worldName), x, y, z);
	}
}

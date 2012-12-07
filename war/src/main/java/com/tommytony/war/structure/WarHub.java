package com.tommytony.war.structure;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;


import com.tommytony.war.Team;
import com.tommytony.war.War;
import com.tommytony.war.Warzone;
import com.tommytony.war.config.TeamConfig;
import com.tommytony.war.config.WarzoneConfig;
import com.tommytony.war.utility.SignHelper;
import com.tommytony.war.volume.BlockInfo;
import com.tommytony.war.volume.Volume;

/**
 *
 * @author tommytony, Tim D��sterhus
 * @package com.tommytony.war
 */
public class WarHub {
	private Location location;
	private Volume volume;
	private Map<String, Block> zoneGateBlocks = new HashMap<String, Block>();
	private BlockFace orientation;

	public WarHub(Location location, String hubOrientation) {
		int yaw = 0;
		if (hubOrientation.equals("south")) {
			yaw = 270;
			this.setOrientation(War.legacyBlockFace ? BlockFace.SOUTH : BlockFace.EAST);
		} else if (hubOrientation.equals("north")) {
			yaw = 90;
			this.setOrientation(War.legacyBlockFace ? BlockFace.NORTH : BlockFace.WEST);
		} else if (hubOrientation.equals("east")) {
			yaw = 180;
			this.setOrientation(War.legacyBlockFace ? BlockFace.EAST : BlockFace.NORTH);
		} else {
			//yaw = 0; unneccessary call...
			this.setOrientation(War.legacyBlockFace ? BlockFace.WEST : BlockFace.SOUTH);
		}

		this.location = new Location(location.getWorld(),
								location.getX(),
								location.getY(),
								location.getZ(),
								yaw, 0);
		this.volume = new Volume("warhub", location.getWorld());
	}

	// Use when creating from player location (with yaw)
	public WarHub(Location location) {
		this.location = location;
		this.volume = new Volume("warhub", location.getWorld());

		this.setLocation(location);
	}

	public Volume getVolume() {
		return this.volume;
	}

	public void setLocation(Location loc) {
		this.location = loc;
		// Lobby orientation
		int yaw = 0;
		if (this.location.getYaw() >= 0) {
			yaw = (int) (this.location.getYaw() % 360);
		} else {
			yaw = (int) (360 + (this.location.getYaw() % 360));
		}
		//as far as I know this update did not affect yaw
		BlockFace facing = null;
		if ((yaw >= 0 && yaw < 45) || (yaw >= 315 && yaw <= 360)) {
			facing = War.legacyBlockFace ? BlockFace.WEST : BlockFace.SOUTH;
		} else if (yaw >= 45 && yaw < 135) {
			facing = War.legacyBlockFace ? BlockFace.NORTH : BlockFace.WEST;
		} else if (yaw >= 135 && yaw < 225) {
			facing = War.legacyBlockFace ? BlockFace.EAST : BlockFace.NORTH;
		} else if (yaw >= 225 && yaw < 315) {
			facing = War.legacyBlockFace ? BlockFace.SOUTH : BlockFace.EAST;
		}
		this.setOrientation(facing);
	}

	public Location getLocation() {
		return this.location;
	}

	public Warzone getDestinationWarzoneForLocation(Location playerLocation) {
		Warzone zone = null;
		for (String zoneName : this.zoneGateBlocks.keySet()) {
			Block gate = this.zoneGateBlocks.get(zoneName);
			if (gate.getX() == playerLocation.getBlockX() && gate.getY() == playerLocation.getBlockY() && gate.getZ() == playerLocation.getBlockZ()) {
				zone = War.war.findWarzone(zoneName);
			}
		}
		return zone;
	}

	public void initialize() {
		// for now, draw the wall of gates to the west
		this.zoneGateBlocks.clear();
		int disabled = 0;
		for (Warzone zone : War.war.getWarzones()) {
			if (zone.getWarzoneConfig().getBoolean(WarzoneConfig.DISABLED)) {
				disabled++;
			}
		}
		int noOfWarzones = War.war.getWarzones().size() - disabled;
		if (noOfWarzones > 0) {
			int hubWidth = noOfWarzones * 4 + 2;
			int halfHubWidth = hubWidth / 2;
			int hubDepth = 6;
			int hubHeigth = 4;

			BlockFace left;
			BlockFace right;
			BlockFace front = this.getOrientation();
			BlockFace back;
			byte data;
			if (this.getOrientation() == BlockFace.SOUTH) {
				data = (byte) 4;
				left = BlockFace.EAST;
				right = BlockFace.WEST;
				back = BlockFace.NORTH;
			} else if (this.getOrientation() == BlockFace.NORTH) {
				data = (byte) 12;
				left = BlockFace.WEST;
				right = BlockFace.EAST;
				back = BlockFace.SOUTH;
			} else if (this.getOrientation() == BlockFace.EAST) {
				data = (byte) 0;
				left = BlockFace.NORTH;
				right = BlockFace.SOUTH;
				back = BlockFace.WEST;
			} else {
				data = (byte) 8;
				left = BlockFace.SOUTH;
				right = BlockFace.NORTH;
				back = BlockFace.EAST;
			}

			Block locationBlock = this.location.getWorld().getBlockAt(this.location.getBlockX(), this.location.getBlockY(), this.location.getBlockZ());

			this.volume.setWorld(this.location.getWorld());
			this.volume.setCornerOne(locationBlock.getRelative(back).getRelative(left, halfHubWidth).getRelative(BlockFace.DOWN));
			this.volume.setCornerTwo(locationBlock.getRelative(right, halfHubWidth).getRelative(front, hubDepth).getRelative(BlockFace.UP, hubHeigth));
			this.volume.saveBlocks();
			
			// materials
			Material floor = Material.getMaterial(War.war.getWarhubMaterials().getFloorId());
			byte floorData = War.war.getWarhubMaterials().getFloorData();
			Material outline = Material.getMaterial(War.war.getWarhubMaterials().getOutlineId());
			byte outlineData = War.war.getWarhubMaterials().getOutlineData();
			Material gate = Material.getMaterial(War.war.getWarhubMaterials().getGateId());
			byte gateData = War.war.getWarhubMaterials().getGateData();
			Material light = Material.getMaterial(War.war.getWarhubMaterials().getLightId());
			byte lightData = War.war.getWarhubMaterials().getLightData();

			// glass floor
			if (!floor.equals(Material.AIR)) {
				// If air, don't set floor to air, just leave original ground. Otherwise apply material.
				this.volume.setFaceMaterial(BlockFace.DOWN, floor, floorData);	
			}
			
			if (!outline.equals(Material.AIR)) {
				// If air, leave original blocks.
				this.volume.setFloorOutlineMaterial(outline, outlineData);
			}
			
			// clear minimal path around warhub tp
			Volume warhubTpVolume = new Volume("warhubtp", this.location.getWorld());
			warhubTpVolume.setCornerOne(locationBlock.getRelative(back).getRelative(left));
			warhubTpVolume.setCornerTwo(locationBlock.getRelative(front, 2).getRelative(right).getRelative(BlockFace.UP));
			warhubTpVolume.setToMaterial(Material.AIR);
		
			// draw gates
			Block currentGateBlock = BlockInfo.getBlock(this.location.getWorld(), this.volume.getCornerOne()).getRelative(BlockFace.UP).getRelative(front, hubDepth).getRelative(right, 2);

			for (Warzone zone : War.war.getWarzones()) { // gonna use the index to find it again
				if (!zone.getWarzoneConfig().getBoolean(WarzoneConfig.DISABLED)) {
					this.zoneGateBlocks.put(zone.getName(), currentGateBlock);
					
					// minimal air path
					Volume gateAirVolume = new Volume("gateAir", currentGateBlock.getWorld());
					gateAirVolume.setCornerOne(currentGateBlock.getRelative(right));
					gateAirVolume.setCornerTwo(currentGateBlock.getRelative(left).getRelative(back, 2).getRelative(BlockFace.UP, 2));
					gateAirVolume.setToMaterial(Material.AIR);
					
					currentGateBlock.getRelative(back, 2).getRelative(right, 2).setType(Material.AIR);
					currentGateBlock.getRelative(back, 2).getRelative(right, 2).getRelative(BlockFace.UP).setType(Material.AIR);
					currentGateBlock.getRelative(back, 2).getRelative(right, 2).getRelative(BlockFace.UP, 2).setType(Material.AIR);
					currentGateBlock.getRelative(back, 2).getRelative(left, 2).setType(Material.AIR);
					currentGateBlock.getRelative(back, 2).getRelative(left, 2).getRelative(BlockFace.UP).setType(Material.AIR);
					currentGateBlock.getRelative(back, 2).getRelative(left, 2).getRelative(BlockFace.UP, 2).setType(Material.AIR);
					
					// gate blocks
					currentGateBlock.getRelative(BlockFace.DOWN).setType(light);
					currentGateBlock.getRelative(BlockFace.DOWN).setData(lightData);
					
					currentGateBlock.getRelative(left).setType(gate);
					currentGateBlock.getRelative(left).setData(gateData);
					
					currentGateBlock.getRelative(right).getRelative(BlockFace.UP).setType(gate);
					currentGateBlock.getRelative(right).getRelative(BlockFace.UP).setData(gateData);
					//merged calls into one call
					currentGateBlock.getRelative(left).getRelative(BlockFace.UP, 2).setType(gate);
					currentGateBlock.getRelative(left).getRelative(BlockFace.UP, 2).setData(gateData);
					
					currentGateBlock.getRelative(right).setType(gate);
					currentGateBlock.getRelative(right).setData(gateData);
					
					currentGateBlock.getRelative(left).getRelative(BlockFace.UP).setType(gate);
					currentGateBlock.getRelative(left).getRelative(BlockFace.UP).setData(gateData);
					
					currentGateBlock.getRelative(right).getRelative(BlockFace.UP, 2).setType(gate);
					currentGateBlock.getRelative(right).getRelative(BlockFace.UP, 2).setData(gateData);
					
					currentGateBlock.getRelative(BlockFace.UP, 2).setType(gate);
					currentGateBlock.getRelative(BlockFace.UP, 2).setData(gateData);
					
					currentGateBlock = currentGateBlock.getRelative(right, 4);

				}
			}

			// War hub sign
			Block signBlock = locationBlock.getRelative(front, 2);

			String[] lines = new String[4];
			lines[0] = "War hub";
			lines[1] = "(/warhub)";
			lines[2] = "Pick your";
			lines[3] = "battle!";
			SignHelper.setToSign(War.war, signBlock, data, lines);

			// Warzone signs
			for (Warzone zone : War.war.getWarzones()) {
				if (!zone.getWarzoneConfig().getBoolean(WarzoneConfig.DISABLED) && zone.ready()) {
					this.resetZoneSign(zone);
				}
			}
		}
	}

	/**
	 * Resets the sign of the given warzone
	 *
	 * @param Warzone
	 *                zone
	 */
	public void resetZoneSign(Warzone zone) {
		BlockFace left;
		BlockFace back;
		byte data;
		if (this.getOrientation() == BlockFace.SOUTH) {
			data = (byte) 4;
			left = BlockFace.EAST;
			back = BlockFace.NORTH;
		} else if (this.getOrientation() == BlockFace.NORTH) {
			data = (byte) 12;
			left = BlockFace.WEST;
			back = BlockFace.SOUTH;
		} else if (this.getOrientation() == BlockFace.EAST) {
			data = (byte) 0;
			left = BlockFace.NORTH;
			back = BlockFace.WEST;
		} else {
			data = (byte) 8;
			left = BlockFace.SOUTH;
			back = BlockFace.EAST;
		}

		Block zoneGate = this.zoneGateBlocks.get(zone.getName());
		if (zoneGate != null) {
			Block block = zoneGate.getRelative(left).getRelative(back, 1);
			if (block.getType() != Material.SIGN_POST) {
				block.setType(Material.SIGN_POST);
			}
			block.setData(data);
	
			int zoneCap = 0;
			int zonePlayers = 0;
			for (Team t : zone.getTeams()) {
				zonePlayers += t.getPlayers().size();
				zoneCap += t.getTeamConfig().resolveInt(TeamConfig.TEAMSIZE);
			}
			String[] lines = new String[4];
			lines[0] = "Warzone";
			lines[1] = zone.getName();
			lines[2] = zonePlayers + "/" + zoneCap + " players";
			lines[3] = zone.getTeams().size() + " teams";
			SignHelper.setToSign(War.war, block, data, lines);
		} else {
			War.war.log("Failed to find warhub gate for " + zone.getName() + " warzone.", Level.WARNING);
		}
	}

	public void setVolume(Volume vol) {
		this.volume = vol;
	}

	public void setOrientation(BlockFace orientation) {
		this.orientation = orientation;
	}

	public BlockFace getOrientation() {
		return this.orientation;
	}

}

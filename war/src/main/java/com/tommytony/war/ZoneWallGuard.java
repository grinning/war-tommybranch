package com.tommytony.war;

import org.bukkit.Block;
import org.bukkit.BlockFace;
import org.bukkit.HumanEntity;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Player;

import bukkit.tommytony.war.War;

import com.tommytony.war.volumes.CenteredVolume;

public class ZoneWallGuard {
	private Player player;
	private Warzone warzone;
	private Location playerLocation;
	private final War war; 
	private BlockFace wall; 
	
	private final int radius = 3;
	
	
	public ZoneWallGuard(Player player, War war, Warzone warzone) {
		this.player = player;
		this.war = war;
		this.playerLocation = player.getLocation();
		this.warzone = warzone;
		this.activate();
	}
	
	private void activate() {
		// save current blocks
		Block nearestWallBlock = warzone.getNearestWallBlock(playerLocation);
//		if(volume == null) {
//			volume = new CenteredVolume("zoneGuard-" + warzone.getName() + "-" + player.getName(), nearestWallBlock, radius, war, warzone);
//			int saved = volume.saveBlocks();
//			war.getLogger().info("Warzone wall guard created: " + saved + " blocks saved for " + player.getName());
//		} else {
//			volume.changeCenter(nearestWallBlock, radius);
//			int saved = volume.saveBlocks();
//			war.getLogger().info("Warzone wall guard updated: " + saved + " blocks saved for " + player.getName());
//		}
		// add wall guard blocks
		nearestWallBlock.setType(Material.Glass);
		nearestWallBlock.getFace(BlockFace.Up).setType(Material.Glass);
		nearestWallBlock.getFace(BlockFace.Down).setType(Material.Glass);
		if(warzone.getVolume().isNorthWallBlock(nearestWallBlock.getFace(BlockFace.East)) && 
			warzone.getVolume().isNorthWallBlock(nearestWallBlock.getFace(BlockFace.West))) {
			// north wall guard
			this.wall = BlockFace.North;
			toGlass(nearestWallBlock.getFace(BlockFace.East), BlockFace.North);
			toGlass(nearestWallBlock.getFace(BlockFace.East).getFace(BlockFace.Up), BlockFace.North);
			toGlass(nearestWallBlock.getFace(BlockFace.East).getFace(BlockFace.Down), BlockFace.North);
			toGlass(nearestWallBlock.getFace(BlockFace.West), BlockFace.North);
			toGlass(nearestWallBlock.getFace(BlockFace.West).getFace(BlockFace.Up), BlockFace.North);
			toGlass(nearestWallBlock.getFace(BlockFace.West).getFace(BlockFace.Down), BlockFace.North);
		} else if (warzone.getVolume().isSouthWallBlock(nearestWallBlock.getFace(BlockFace.East)) && 
			warzone.getVolume().isSouthWallBlock(nearestWallBlock.getFace(BlockFace.West))) {
			// south wall guard
			this.wall = BlockFace.South;
			toGlass(nearestWallBlock.getFace(BlockFace.East), BlockFace.South);
			toGlass(nearestWallBlock.getFace(BlockFace.East).getFace(BlockFace.Up), BlockFace.South);
			toGlass(nearestWallBlock.getFace(BlockFace.East).getFace(BlockFace.Down), BlockFace.South);
			toGlass(nearestWallBlock.getFace(BlockFace.West), BlockFace.South);
			toGlass(nearestWallBlock.getFace(BlockFace.West).getFace(BlockFace.Up), BlockFace.South);
			toGlass(nearestWallBlock.getFace(BlockFace.West).getFace(BlockFace.Down), BlockFace.South);
			// ..
		} else if (warzone.getVolume().isEastWallBlock(nearestWallBlock.getFace(BlockFace.North)) && 
				warzone.getVolume().isEastWallBlock(nearestWallBlock.getFace(BlockFace.South))) {
			//east wall guard
			this.wall = BlockFace.East;
			toGlass(nearestWallBlock.getFace(BlockFace.North), BlockFace.East);
			toGlass(nearestWallBlock.getFace(BlockFace.North).getFace(BlockFace.Up), BlockFace.East);
			toGlass(nearestWallBlock.getFace(BlockFace.North).getFace(BlockFace.Down), BlockFace.East);
			toGlass(nearestWallBlock.getFace(BlockFace.South), BlockFace.West);
			toGlass(nearestWallBlock.getFace(BlockFace.South).getFace(BlockFace.Up), BlockFace.West);
			toGlass(nearestWallBlock.getFace(BlockFace.South).getFace(BlockFace.Down), BlockFace.West);
		} else if (warzone.getVolume().isWestWallBlock(nearestWallBlock.getFace(BlockFace.North)) && 
				warzone.getVolume().isWestWallBlock(nearestWallBlock.getFace(BlockFace.South))) {
			//west wall guard
			this.wall = BlockFace.West;
			toGlass(nearestWallBlock.getFace(BlockFace.North), BlockFace.West);
			toGlass(nearestWallBlock.getFace(BlockFace.North).getFace(BlockFace.Up), BlockFace.West);
			toGlass(nearestWallBlock.getFace(BlockFace.North).getFace(BlockFace.Down), BlockFace.West);
			toGlass(nearestWallBlock.getFace(BlockFace.South), BlockFace.West);
			toGlass(nearestWallBlock.getFace(BlockFace.South).getFace(BlockFace.Up), BlockFace.West);
			toGlass(nearestWallBlock.getFace(BlockFace.South).getFace(BlockFace.Down), BlockFace.West);
		}
	}
	
	private void toGlass(Block block, BlockFace wall) {
		// face here means which wall we are working on
		if(wall == BlockFace.North) {
			if(warzone.getVolume().isNorthWallBlock(block)) {
				block.setType(Material.Glass);
			}
		} else if (wall == BlockFace.South) {
			if(warzone.getVolume().isSouthWallBlock(block)) {
				block.setType(Material.Glass);
			}
		} else if (wall == BlockFace.East) {
			if(warzone.getVolume().isEastWallBlock(block)) {
				block.setType(Material.Glass);
			}
		} else if (wall == BlockFace.West) {
			if(warzone.getVolume().isWestWallBlock(block)) {
				block.setType(Material.Glass);
			}
		}
	}
	
	public void updatePlayerPosition(Location location) {
		if(warzone.isNearWall(location)) {
			this.playerLocation = location;
			activate();
		}
	}

	public Player getPlayer() {
		return player;
	}

	public void setWall(BlockFace wall) {
		this.wall = wall;
	}

	public BlockFace getWall() {
		return wall;
	}
}
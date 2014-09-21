package net.gmx.nosefish.fishysigns.watcher;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.canarymod.api.world.World;
import net.canarymod.api.world.blocks.Block;
import net.canarymod.hook.HookHandler;
import net.canarymod.hook.world.BlockUpdateHook;
import net.canarymod.plugin.Priority;
import net.gmx.nosefish.fishylib.blocks.BlockInfo;
import net.gmx.nosefish.fishylib.datastructures.ConcurrentMapWithSet;
import net.gmx.nosefish.fishylib.worldmath.FishyChunk;
import net.gmx.nosefish.fishylib.worldmath.FishyCuboidInt;
import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.gmx.nosefish.fishylib.worldmath.FishyPointInt;
import net.gmx.nosefish.fishylib.worldmath.FishyVectorInt;
import net.gmx.nosefish.fishylib.worldmath.FishyWorld;
import net.gmx.nosefish.fishysigns.plugin.FishySigns;
import net.gmx.nosefish.fishysigns.plugin.engine.ActivationManager;
import net.gmx.nosefish.fishysigns.task.FishyTask;
import net.gmx.nosefish.fishysigns.watcher.activator.IActivator;


public final class InventoryBlockWatcher implements IFishyWatcher {
 
    /**
     * Singleton instance
     */
    protected static final InventoryBlockWatcher instance = new InventoryBlockWatcher(); 
	static {
		FishySigns.addWatcher(instance);
	}
	
	
	//  watch block updates: placement/destruction of inventory blocks and activate
	//      --> don't want to store a location for every block, need a way to efficiently
	//          find all registered cuboids that contain a given point
	//          Approach 1: octrees or some other tree structure - too much work, maybe later
	//          Approach 2: Allow a maximum size of 16x256x16, use chunk grid - exhaustive search through
	//                      all cuboids registered for the 2x2 chunks <= the point, 
	//                      store by chunk of lowPoint corner
	
	// in addition to the chunk of the location, cuboids in these chunks must be
	// checked, too.
	protected static final FishyVectorInt[] chunkVectors = new FishyVectorInt[] {
		new FishyVectorInt(-16, 0, 0),
		new FishyVectorInt(0, 0, -16),
		new FishyVectorInt(-16, 0, -16)
	};

	
	protected volatile boolean enabled = false;
	protected BlockLocationWatcher singleBlocks = new BlockLocationWatcher(){};
	protected Object addRemoveLock = new Object(); // the two indexes must be consistent
	protected ConcurrentMapWithSet<FishyChunk, SmallCuboid> cuboids =
			new ConcurrentMapWithSet<>();
	protected ConcurrentMapWithSet<SmallCuboid, Long> cuboidToId =
			new ConcurrentMapWithSet<>();



	
	public static InventoryBlockWatcher getInstance() {
		return instance;
	}

	/**
	 * Registers a cuboid. The activatable with the given ID will be
	 * activated whenever a storage block is created or destroyed
	 * inside the cuboid.
	 * 
	 * @param activatableID
	 * @param cuboid
	 */
	public void register(Long activatableID, SmallCuboid cuboid){
		FishyLocationInt lowPointLocation =
				new FishyLocationInt(cuboid.getWorld(), cuboid.getLowPoint());
		if (cuboid.getLowPoint().equals(cuboid.getHighPoint())) {
			// Just a single block. Treat it as such, it's faster.
			this.register(activatableID, lowPointLocation);
		} else {
			synchronized(addRemoveLock) {
				cuboidToId.put(cuboid, activatableID);
				cuboids.put(FishyChunk.getChunkContaining(lowPointLocation), cuboid);
			}
		}
		StorageBlockFinder stbFinder = new StorageBlockFinder(cuboid, activatableID);
		stbFinder.submit();
	}
	
	/**
	 * Registers a single location. The activatable with the given ID will be
	 * activated block is created or destroyed at this location.
	 * If it's only a few blocks, prefer this over the cuboid variant.
	 * 
	 * @param activatableID
	 * @param location
	 */
	public void register(Long activatableID, FishyLocationInt location){
		singleBlocks.register(activatableID, location);
		StorageBlockFinder stbFinder = new StorageBlockFinder(location, activatableID);
		stbFinder.submit();
	}
	
	/**
	 * Removes an Activatable. Be sure to call
	 * this for all activatables when they are
	 * no longer needed.
	 * 
	 * @param activatableID
	 */
	public void remove(Long activatableID) {
		singleBlocks.remove(activatableID);
		synchronized(addRemoveLock) {
			List<SmallCuboid> removedCuboids = cuboidToId.removeValue(activatableID);
			for (SmallCuboid cuboid : removedCuboids) {
				cuboids.removeValue(cuboid);
			}
		}
	}

	/**
	 * Called by Canary when a block is updated.
	 * Activates registered Activatables if the affected block
	 * is a storage block that has been created or destroyed.
	 * 
	 * @param hook
	 */
	@HookHandler(priority=Priority.PASSIVE)
	public void onBlockUpdate(BlockUpdateHook hook) {
		if (! enabled) {
			return;
		}
		int oldId = hook.getBlock().getTypeId();
		int newId = hook.getNewBlockId();
		// TODO: any way this could go wrong?
		if (oldId != newId && BlockInfo.isStorageBlock(newId)) {
			// storage block created, pass new block type
			ActivationTask activate = 
					new ActivationTask(Type.CREATED,
							new FishyLocationInt(hook.getBlock().getLocation()),
							hook.getNewBlockId());
			activate.submit();
		} else if (oldId != newId && BlockInfo.isStorageBlock(oldId)) {
			// storage block destroyed, pass old block type
			ActivationTask activate = 
					new ActivationTask(Type.DESTROYED,
							new FishyLocationInt(hook.getBlock().getLocation()),
							hook.getBlock().getTypeId());
			activate.submit();
		}
	}
	
	@Override
	public void enable() {
		singleBlocks.enable();
		enabled = true;
	}

	@Override
	public void disable() {
		enabled = false;
		singleBlocks.disable();
	}

	/**
	 * A FishyCuboidInt no larger than 16x256x16.
	 * 
	 * @author Stefan Steinheimer
	 *
	 */
	public static class SmallCuboid extends FishyCuboidInt {

		public SmallCuboid(FishyWorld world, FishyPointInt point1,	FishyPointInt point2)
				throws CuboidTooLargeException {
			super(world, point1, point2);
			if ((highPoint.getIntX() - lowPoint.getIntX() > 16)
					|| (highPoint.getIntZ() - lowPoint.getIntZ() > 16)) {
				throw new CuboidTooLargeException("Maximum size of SmallCuboid exceeded." +
						"Maximum permitted size is 16x256x16");
			}
		}
	}
	
	/**
	 * Thrown when a cuboid exceeds maximum permitted size.
	 * 
	 * @author Stefan Steinheimer
	 *
	 */
	public static class CuboidTooLargeException extends Exception {
		private static final long serialVersionUID = -3229464016655825744L;

		public CuboidTooLargeException() {
			super();
		}

		public CuboidTooLargeException(String message) {
			super(message);
		}
	}
	
	public class StorageBlockFinder extends FishyTask {
		private final FishyLocationInt location;
		private final FishyCuboidInt cuboid; 
		private final Long idToActivate;
		
		public StorageBlockFinder(FishyLocationInt location, Long idToActivate) {
			this.location = location;
			this.cuboid = null;
			this.idToActivate = idToActivate;
		}
		
		public StorageBlockFinder(FishyCuboidInt cuboid, Long idToActivate) {
			this.location = null;
			this.cuboid = cuboid;
			this.idToActivate = idToActivate;
		}
		
		@Override
		public void doStuff() {
			if (location != null) {
				activateSingleLocation();
			}
			if (cuboid != null) {
				activateCuboid();
			}
		}
		
		private int getStorageBlock(FishyWorld fWorld, int x, int y, int z) {
			World world = fWorld.getWorldIfLoaded();
			if (world == null) {
				return -1;
			}
			if (! world.isChunkLoaded(FishyChunk.worldToChunk(x), FishyChunk.worldToChunk(z))) {
				return -1;
			}
			Block block = world.getBlockAt(x, y, z); 
			if (block == null) {
				return -1;
			}
			int blockId = block.getTypeId();
			return BlockInfo.isStorageBlock(blockId) ? blockId : -1;
		}
		
		private void activateSingleLocation() {
			int blockId = getStorageBlock(location.getWorld(),
					location.getIntX(),
					location.getIntY(),
					location.getIntZ());
			if (blockId != -1) {
				Map<FishyLocationInt, Integer> activationMap =
						new HashMap<>(1, 1.0F);
				activationMap.put(location, blockId);
				InitialActivationTask activate = 
						new InitialActivationTask(idToActivate, activationMap);
				this.setNextTask(activate);
			}
		}
		
		private void activateCuboid() {
			Map<FishyLocationInt, Integer> activationMap =
					new HashMap<>(8, 0.9F);
			// iterate over cuboid
			// TODO: use TileEntityList for larger cuboids
			for (int x = cuboid.getLowPoint().getIntX();
					x <= cuboid.getHighPoint().getIntX(); x++){
				for (int y = cuboid.getLowPoint().getIntY();
						y <= cuboid.getHighPoint().getIntY(); y++){
					for (int z = cuboid.getLowPoint().getIntZ();
							z <= cuboid.getHighPoint().getIntZ(); z++){
						// check for storage block
						int blockId = getStorageBlock(cuboid.getWorld(), x, y, z);
						if (blockId != -1) {
							activationMap.put(new FishyLocationInt(cuboid.getWorld(), x, y, z), blockId);
						}
					}
				}
			}
			if (! activationMap.isEmpty()) {
				InitialActivationTask activate = 
						new InitialActivationTask(idToActivate, activationMap);
				this.setNextTask(activate);
			}
		}
	}
	
	protected class InitialActivationTask extends FishyTask {
		private final Long toActivate;
		private final Map<FishyLocationInt, Integer> storageBlocks;
		
		public InitialActivationTask(Long toActivate, Map<FishyLocationInt, Integer> storageBlocks) {
			this.toActivate = toActivate;
			this.storageBlocks = storageBlocks;
		}

		@Override
		public void doStuff() {
			for (Map.Entry<FishyLocationInt, Integer> block : storageBlocks.entrySet()) {
			IActivator activator =
					new ActivatorInventoryBlock(Type.CREATED, block.getKey(), block.getValue());
			ActivationManager.getInstance().activate(toActivate, activator);
			}
		}
		
	}
	
	/**
	 * Finds all ids that are registered for this block or
	 * cuboids containing the block and activates them. 
	 *
	 */
	protected class ActivationTask extends FishyTask {
		private final Type type;
		private final FishyLocationInt location;
		private final int blockId;
		
		public ActivationTask(Type type, FishyLocationInt location, int blockId) {
			this.type = type;
			this.location = location;
			this.blockId = blockId;
		}
		
		@Override
		public void doStuff() {
			Set<Long> toActivate = new TreeSet<>(); // HashSet may be faster, but they're so wasteful
			// find activatables from singleBlocks
			Set<Long> idSet = singleBlocks.blockLocationIndex.get(location);
			if (idSet != null) {
				synchronized(idSet) {
					toActivate.addAll(idSet);
				}
			}
			// find activatables from cuboids
			Set<SmallCuboid> foundCuboids = findCuboidsContainingLocation();
			for (SmallCuboid cuboid : foundCuboids) {
				idSet = cuboidToId.get(cuboid);
				if (idSet != null) {
					synchronized(idSet) {
						toActivate.addAll(idSet);
					}
				}
			}
			// if there are any, activate
			if (! toActivate.isEmpty()) {
				IActivator activator = new ActivatorInventoryBlock(type, location, blockId);
				ActivationManager.getInstance().activateAll(activator, toActivate);
			}
		}
		
		private Set<SmallCuboid> findCuboidsContainingLocation() {
			Set<SmallCuboid> resultSet = new LinkedHashSet<>();
			FishyChunk[] chunks = new FishyChunk[] {
					FishyChunk.getChunkContaining(location),
					FishyChunk.getChunkContaining(location.addIntVector(chunkVectors[0])),
					FishyChunk.getChunkContaining(location.addIntVector(chunkVectors[1])),
					FishyChunk.getChunkContaining(location.addIntVector(chunkVectors[2]))
			};
			for (FishyChunk chunk : chunks) {
				Set<SmallCuboid> cuboidSet = cuboids.get(chunk);
				if (cuboidSet != null) {
					synchronized(cuboidSet) {
						for (SmallCuboid cuboid : cuboidSet) {
							if (cuboid.containsLocation(location)) {
								resultSet.add(cuboid);
							}
						}
					}
				}
			}
			return resultSet;
		}
	}
	
	/**
	 * Event types
	 * 
	 */
	public static enum Type {
		CREATED, DESTROYED;
	}
	
	/**
	 * The Activator sent out by this watcher.
	 *
	 */
	public class ActivatorInventoryBlock implements IActivator {
		private final Type type;
		private final FishyLocationInt location;
		private final int blockId;
		
		public ActivatorInventoryBlock(Type type, FishyLocationInt location, int blockId) {
			this.type = type;
			this.location = location;
			this.blockId = blockId;
		}

		/**
		 * @return the type
		 */
		public Type getType() {
			return type;
		}

		/**
		 * @return the location
		 */
		public FishyLocationInt getLocation() {
			return location;
		}

		/**
		 * @return the blockId
		 */
		public int getBlockId() {
			return blockId;
		}
	}
}
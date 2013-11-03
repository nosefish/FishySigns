package net.gmx.nosefish.fishysigns.iobox;

import net.canarymod.api.inventory.Inventory;
import net.canarymod.api.inventory.Item;
import net.canarymod.api.world.World;
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.blocks.TileEntity;
import net.gmx.nosefish.fishylib.worldmath.FishyChunk;
import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.gmx.nosefish.fishysigns.anchor.IAnchor;
import net.gmx.nosefish.fishysigns.watcher.InventoryBlockWatcher;
import net.gmx.nosefish.fishysigns.watcher.InventoryBlockWatcher.ActivatorInventoryBlock;
import net.gmx.nosefish.fishysigns.watcher.InventoryBlockWatcher.Type;
import net.gmx.nosefish.fishysigns.watcher.activator.IActivator;

public class ChestAccessBox extends AnchoredActivatableBox {
	private final FishyLocationInt chestLocation;
	private volatile ChestType chestType = ChestType.NONE;

	public static ChestAccessBox createAndRegister(IAnchor anchor, FishyLocationInt chestLocation) {
		ChestAccessBox box = new ChestAccessBox(chestLocation);
		registerWithActivationManagerAndAnchor(box, anchor);
		return box;
	}
	
	private ChestAccessBox(FishyLocationInt chestLocation) {
		super();
		this.chestLocation = chestLocation;
	}

	@Override
	public void activate(IActivator activator) {
		if (activator instanceof ActivatorInventoryBlock) {
			ActivatorInventoryBlock aib = (ActivatorInventoryBlock) activator;
			switch (aib.getType()) {
			case DESTROYED: 
				chestType = ChestType.NONE;
				break;
			case CREATED:
				if (aib.getBlockId() == BlockType.Chest.getId()) {
					chestType = ChestType.CHEST;
				} else if (aib.getBlockId() == BlockType.TrappedChest.getId()) {
					chestType = ChestType.TRAPPED_CHEST;
				} else  {
					chestType = ChestType.NONE;
				}
			}
		}
	}
	
	/**
	 * Must be called from server thread
	 * 
	 * @param item
	 */
	public void storeItem(Item item) {
		if (chestType == ChestType.NONE) {
			return;
		}
		if (item.getAmount() <= 0) {
			return;
		}
		World world = chestLocation.getWorld().getWorldIfLoaded();
		if (world == null) {
			return;
		}
		if (! world.isChunkLoaded(
				FishyChunk.worldToChunk(chestLocation.getIntX()), 
				FishyChunk.worldToChunk(chestLocation.getIntZ()))) {
			return;
		}
		TileEntity tEnt = world.getTileEntityAt(
				chestLocation.getIntX(),
				chestLocation.getIntY(),
				chestLocation.getIntZ());
		if (tEnt == null || ! (tEnt instanceof Inventory)) {
			return;
		}
		Inventory chest = (Inventory)tEnt;
		chest.addItem(item);
	}

	/**
	 * Must be called from server thread
	 * 
	 * @param item
	 * @return
	 */
	public Item fetchItem(Item item) {
		return null;
	}

	@Override
	public void remove() {
		InventoryBlockWatcher.getInstance().remove(this.getID());
	}

	@Override
	protected void initActivatable() {
		InventoryBlockWatcher.getInstance().register(this.getID(), chestLocation);
	}

	public enum ChestType {
		CHEST, TRAPPED_CHEST, NONE;
	}
}

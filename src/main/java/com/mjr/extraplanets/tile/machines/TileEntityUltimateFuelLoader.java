package com.mjr.extraplanets.tile.machines;

import micdoodle8.mods.galacticraft.api.entity.IFuelable;
import micdoodle8.mods.galacticraft.api.tile.ILandingPadAttachable;
import micdoodle8.mods.galacticraft.api.transmission.NetworkType;
import micdoodle8.mods.galacticraft.api.vector.BlockVec3;
import micdoodle8.mods.galacticraft.core.GCFluids;
import micdoodle8.mods.galacticraft.core.GCItems;
import micdoodle8.mods.galacticraft.core.energy.item.ItemElectricBase;
import micdoodle8.mods.galacticraft.core.energy.tile.TileBaseElectricBlockWithInventory;
import micdoodle8.mods.galacticraft.core.tile.IMachineSides;
import micdoodle8.mods.galacticraft.core.tile.IMachineSidesProperties;
import micdoodle8.mods.galacticraft.core.tile.TileEntityMulti;
import micdoodle8.mods.galacticraft.core.util.FluidUtil;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import micdoodle8.mods.galacticraft.core.wrappers.IFluidHandlerWrapper;
import micdoodle8.mods.miccore.Annotations.NetworkedField;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fml.relauncher.Side;

import com.mjr.extraplanets.blocks.machines.UltimateFuelLoader;

public class TileEntityUltimateFuelLoader extends TileBaseElectricBlockWithInventory implements ISidedInventory, IFluidHandlerWrapper, ILandingPadAttachable, IMachineSides {
	private final int tankCapacity = 12000 * 3;
	@NetworkedField(targetSide = Side.CLIENT)
	public FluidTank fuelTank = new FluidTank(this.tankCapacity);
	private NonNullList<ItemStack> stacks = NonNullList.withSize(2, ItemStack.EMPTY);
	public IFuelable attachedFuelable;
	private boolean loadedFuelLastTick = false;

	public TileEntityUltimateFuelLoader() {
		this.storage.setMaxExtract(30 * 5);
	}

	public int getScaledFuelLevel(int i) {
		final double fuelLevel = this.fuelTank.getFluid() == null ? 0 : this.fuelTank.getFluid().amount;

		return (int) (fuelLevel * i / this.tankCapacity);
	}

	@Override
	public void update() {
		super.update();

		if (!this.world.isRemote) {
			this.loadedFuelLastTick = false;

			final FluidStack liquidContained = FluidUtil.getFluidContained(this.stacks.get(1));
			if (FluidUtil.isFuel(liquidContained)) {
				FluidUtil.loadFromContainer(this.fuelTank, GCFluids.fluidFuel, this.stacks, 1, liquidContained.amount);
			}

			if (this.ticks % 100 == 0) {
				this.attachedFuelable = null;

				for (final EnumFacing dir : EnumFacing.values()) {
					final TileEntity pad = new BlockVec3(this).getTileEntityOnSide(this.world, dir);

					if (pad instanceof TileEntityMulti) {
						final TileEntity mainTile = ((TileEntityMulti) pad).getMainBlockTile();

						if (mainTile instanceof IFuelable) {
							this.attachedFuelable = (IFuelable) mainTile;
							break;
						}
					} else if (pad instanceof IFuelable) {
						this.attachedFuelable = (IFuelable) pad;
						break;
					}
				}

			}

			if (this.fuelTank != null && this.fuelTank.getFluid() != null && this.fuelTank.getFluid().amount > 0) {
				final FluidStack liquid = new FluidStack(GCFluids.fluidFuel, 10);

				if (this.attachedFuelable != null && this.hasEnoughEnergyToRun && !this.disabled) {
					int filled = this.attachedFuelable.addFuel(liquid, true);
					this.loadedFuelLastTick = filled > 0;
					this.fuelTank.drain(filled, true);
				}
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound par1NBTTagCompound) {
		super.readFromNBT(par1NBTTagCompound);
		this.stacks = this.readStandardItemsFromNBT(par1NBTTagCompound);

		if (par1NBTTagCompound.hasKey("fuelTank")) {
			this.fuelTank.readFromNBT(par1NBTTagCompound.getCompoundTag("fuelTank"));
		}

		this.readMachineSidesFromNBT(par1NBTTagCompound); // Needed by IMachineSides
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		this.writeStandardItemsToNBT(nbt, this.stacks);

		if (this.fuelTank.getFluid() != null) {
			nbt.setTag("fuelTank", this.fuelTank.writeToNBT(new NBTTagCompound()));
		}

		this.addMachineSidesToNBT(nbt); // Needed by IMachineSides

		return nbt;
	}

	@Override
	protected NonNullList<ItemStack> getContainingItems() {
		return this.stacks;
	}

	@Override
	public String getName() {
		return GCCoreUtil.translate("container.ultimate.fuelloader.name");
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	// ISidedInventory Implementation:

	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		return new int[] { 0, 1 };
	}

	@Override
	public boolean canInsertItem(int slotID, ItemStack itemstack, EnumFacing side) {
		return this.isItemValidForSlot(slotID, itemstack);
	}

	@Override
	public boolean canExtractItem(int slotID, ItemStack itemstack, EnumFacing side) {
		if (slotID == 1 && itemstack != null) {
			return FluidUtil.isEmptyContainer(itemstack);
		}
		return false;
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public ITextComponent getDisplayName() {
		return null;
	}

	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack) {
		return (slotID == 1 && itemstack != null && itemstack.getItem() == GCItems.fuelCanister) || (slotID == 0 ? ItemElectricBase.isElectricItem(itemstack.getItem()) : false);
	}

	@Override
	public boolean canDrain(EnumFacing from, Fluid fluid) {
		return false;
	}

	@Override
	public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain) {
		return null;
	}

	@Override
	public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain) {
		return null;
	}

	@Override
	public boolean canFill(EnumFacing from, Fluid fluid) {
		if (this.getPipeInputDirection().equals(from)) {
			return this.fuelTank.getFluid() == null || this.fuelTank.getFluidAmount() < this.fuelTank.getCapacity();
		}
		return false;
	}

	@Override
	public int fill(EnumFacing from, FluidStack resource, boolean doFill) {
		int used = 0;

		if (this.getPipeInputDirection().equals(from) && resource != null) {
			if (FluidUtil.testFuel(FluidRegistry.getFluidName(resource))) {
				used = this.fuelTank.fill(resource, doFill);
			}
		}

		return used;
	}

	@Override
	public FluidTankInfo[] getTankInfo(EnumFacing from) {
		if (this.getPipeInputDirection().equals(from)) {
			return new FluidTankInfo[] { new FluidTankInfo(this.fuelTank) };
		}
		return null;
	}

	@Override
	public boolean shouldUseEnergy() {
		return this.fuelTank.getFluid() != null && this.fuelTank.getFluid().amount > 0 && !this.getDisabled(0) && loadedFuelLastTick;
	}

	@Override
	public boolean canAttachToLandingPad(IBlockAccess world, BlockPos pos) {
		return true;
	}

	@Override
	public EnumFacing getFront() {
        IBlockState state = this.world.getBlockState(getPos()); 
        if (state.getBlock() instanceof UltimateFuelLoader)
        {
            return state.getValue(UltimateFuelLoader.FACING).rotateY();
        }
        return EnumFacing.NORTH;
	}
	
	@Override
	public boolean canConnect(EnumFacing direction, NetworkType type) {
		if (direction == null) {
			return false;
		}
		if (type == NetworkType.POWER) {
			return direction == this.getElectricInputDirection();
		}
		if (type == NetworkType.FLUID) {
			return direction == this.getPipeInputDirection();
		}
		return false;
	}

	@Override
	public EnumFacing getElectricInputDirection() {
		switch (this.getSide(MachineSide.ELECTRIC_IN)) {
		case RIGHT:
			return getFront().rotateYCCW();
		case REAR:
			return getFront().getOpposite();
		case TOP:
			return EnumFacing.UP;
		case BOTTOM:
			return EnumFacing.DOWN;
		case LEFT:
		default:
			return getFront().rotateY();
		}
	}

	@Override
	public EnumFacing getPipeInputDirection() {
		switch (this.getSide(MachineSide.PIPE_IN)) {
		case RIGHT:
		default:
			return getFront().rotateYCCW();
		case REAR:
			return getFront().getOpposite();
		case TOP:
			return EnumFacing.UP;
		case BOTTOM:
			return EnumFacing.DOWN;
		case LEFT:
			return getFront().rotateY();
		}
	}

	// ------------------
	// Added these methods and field to implement IMachineSides properly
	// ------------------
	@Override
	public MachineSide[] listConfigurableSides() {
		return new MachineSide[] { MachineSide.ELECTRIC_IN, MachineSide.PIPE_IN };
	}

	@Override
	public Face[] listDefaultFaces() {
		return new Face[] { Face.LEFT, Face.RIGHT };
	}

	private MachineSidePack[] machineSides;

	@Override
	public MachineSidePack[] getAllMachineSides() {
		if (this.machineSides == null) {
			this.initialiseSides();
		}

		return this.machineSides;
	}

	@Override
	public void setupMachineSides(int length) {
		this.machineSides = new MachineSidePack[length];
	}

	@Override
	public void onLoad() {
		this.clientOnLoad();
	}

	@Override
	public IMachineSidesProperties getConfigurationType() {
		return UltimateFuelLoader.MACHINESIDES_RENDERTYPE;
	}
	// ------------------END OF IMachineSides implementation
}
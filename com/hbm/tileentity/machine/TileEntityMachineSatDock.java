package com.hbm.tileentity.machine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.hbm.entity.missile.EntityMinerRocket;
import com.hbm.handler.FluidTypeHandler.FluidType;
import com.hbm.interfaces.IConsumer;
import com.hbm.interfaces.IFluidAcceptor;
import com.hbm.interfaces.IFluidContainer;
import com.hbm.inventory.FluidTank;
import com.hbm.inventory.MachineRecipes;
import com.hbm.inventory.MachineRecipes.GasCentOutput;
import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemSatChip;
import com.hbm.lib.Library;
import com.hbm.packet.AuxElectricityPacket;
import com.hbm.packet.AuxGaugePacket;
import com.hbm.packet.LoopedSoundPacket;
import com.hbm.packet.PacketDispatcher;
import com.hbm.saveddata.SatelliteSaveStructure;
import com.hbm.saveddata.SatelliteSaveStructure.SatelliteType;
import com.hbm.saveddata.SatelliteSavedData;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

public class TileEntityMachineSatDock extends TileEntity implements ISidedInventory {

	private ItemStack slots[];
	
	private static final int[] access = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14 };
	
	private String customName;
	
	public TileEntityMachineSatDock() {
		slots = new ItemStack[16];
	}

	@Override
	public int getSizeInventory() {
		return slots.length;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return slots[i];
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i) {
		if(slots[i] != null)
		{
			ItemStack itemStack = slots[i];
			slots[i] = null;
			return itemStack;
		} else {
		return null;
		}
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemStack) {
		slots[i] = itemStack;
		if(itemStack != null && itemStack.stackSize > getInventoryStackLimit())
		{
			itemStack.stackSize = getInventoryStackLimit();
		}
	}

	@Override
	public String getInventoryName() {
		return this.hasCustomInventoryName() ? this.customName : "container.satDock";
	}

	@Override
	public boolean hasCustomInventoryName() {
		return this.customName != null && this.customName.length() > 0;
	}
	
	public void setCustomName(String name) {
		this.customName = name;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		if(worldObj.getTileEntity(xCoord, yCoord, zCoord) != this)
		{
			return false;
		}else{
			return player.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <=64;
		}
	}
	
	//You scrubs aren't needed for anything (right now)
	@Override
	public void openInventory() {}
	@Override
	public void closeInventory() {}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		if(i == 2 || i == 3 || i == 4 || i == 5)
		{
			return false;
		}
		
		return true;
	}
	
	@Override
	public ItemStack decrStackSize(int i, int j) {
		if(slots[i] != null)
		{
			if(slots[i].stackSize <= j)
			{
				ItemStack itemStack = slots[i];
				slots[i] = null;
				return itemStack;
			}
			ItemStack itemStack1 = slots[i].splitStack(j);
			if (slots[i].stackSize == 0)
			{
				slots[i] = null;
			}
			
			return itemStack1;
		} else {
			return null;
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		NBTTagList list = nbt.getTagList("items", 10);
		
		slots = new ItemStack[getSizeInventory()];
		
		for(int i = 0; i < list.tagCount(); i++)
		{
			NBTTagCompound nbt1 = list.getCompoundTagAt(i);
			byte b0 = nbt1.getByte("slot");
			if(b0 >= 0 && b0 < slots.length)
			{
				slots[b0] = ItemStack.loadItemStackFromNBT(nbt1);
			}
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		NBTTagList list = new NBTTagList();
		
		for(int i = 0; i < slots.length; i++)
		{
			if(slots[i] != null)
			{
				NBTTagCompound nbt1 = new NBTTagCompound();
				nbt1.setByte("slot", (byte)i);
				slots[i].writeToNBT(nbt1);
				list.appendTag(nbt1);
			}
		}
		nbt.setTag("items", list);
	}
	
	@Override
	public int[] getAccessibleSlotsFromSide(int p_94128_1_)
    {
        return access;
    }

	@Override
	public boolean canInsertItem(int i, ItemStack itemStack, int j) {
		return this.isItemValidForSlot(i, itemStack);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, int j) {
		return true;
	}
	
	SatelliteSavedData data = null;
	
	@Override
	public void updateEntity() {

		if(!worldObj.isRemote) {
			
			if(data == null)
				data = (SatelliteSavedData)worldObj.perWorldStorage.loadData(SatelliteSavedData.class, "satellites");
			
		    if(data == null) {
		        worldObj.perWorldStorage.setData("satellites", new SatelliteSavedData(worldObj));
		        data = (SatelliteSavedData)worldObj.perWorldStorage.loadData(SatelliteSavedData.class, "satellites");
		    }
		    data.markDirty();

		    if(data != null && slots[15] != null) {
			    int freq = ItemSatChip.getFreq(slots[15]);
			    
			    SatelliteSaveStructure sat = data.getSatFromFreq(freq);
			    
			   // int delay = 10 * 60 * 1000;
			    int delay = 30 * 1000;
			    
			    if(sat != null && sat.satelliteID == freq && sat.satelliteType.name().equals(SatelliteType.MINER.name())) {
			    	if(sat.lastOp + delay < System.currentTimeMillis()) {
			    		
			        	EntityMinerRocket rocket = new EntityMinerRocket(worldObj);
			        	rocket.posX = xCoord + 0.5;
			        	rocket.posY = 300;
			        	rocket.posZ = zCoord + 0.5;
			        	worldObj.spawnEntityInWorld(rocket);
			        	sat.lastOp = System.currentTimeMillis();
			        	data.markDirty();
			    	}
			    }
		    }
		    
		    List<Entity> list = worldObj.getEntitiesWithinAABBExcludingEntity(null, AxisAlignedBB.getBoundingBox(xCoord - 0.25 + 0.5, yCoord + 0.75, zCoord - 0.25 + 0.5, xCoord + 0.25 + 0.5, yCoord + 2, zCoord + 0.25 + 0.5));
		    
		    for(Entity e : list) {
		    	
		    	if(e instanceof EntityMinerRocket) {
		    		
		    		EntityMinerRocket rocket = (EntityMinerRocket)e;
		    		
		    		if(rocket.getDataWatcher().getWatchableObjectInt(16) == 1 && rocket.timer == 50) {
		    			unloadCargo();
		    		}
		    	}
		    }

		    ejectInto(xCoord + 2, yCoord, zCoord);
		    ejectInto(xCoord - 2, yCoord, zCoord);
		    ejectInto(xCoord, yCoord, zCoord + 2);
		    ejectInto(xCoord, yCoord, zCoord - 2);
		}
	}
	
	static Random rand = new Random();
	
	private void unloadCargo() {
		
		int items = rand.nextInt(6) + 10;
		
		int randy = 0;
		rand = new Random();
		
		for(int i = 0; i < items; i++) {
			
			randy = (int) (rand.nextFloat() * 100);
			
			if(randy < 10)
				addToInv(new ItemStack(ModItems.powder_aluminium, 3));
			else if(randy < 20)
				addToInv(new ItemStack(ModItems.powder_iron, 3));
			else if(randy < 30)
				addToInv(new ItemStack(ModItems.powder_titanium, 2));
			else if(randy < 45)
				addToInv(new ItemStack(ModItems.powder_coal, 4));
			else if(randy < 50)
				addToInv(new ItemStack(ModItems.powder_uranium, 2));
			else if(randy < 53)
				addToInv(new ItemStack(ModItems.powder_plutonium, 1));
			else if(randy < 58)
				addToInv(new ItemStack(ModItems.powder_thorium, 2));
			else if(randy < 60)
				addToInv(new ItemStack(ModItems.powder_desh_mix, 3));
			else if(randy < 65)
				addToInv(new ItemStack(ModItems.powder_diamond, 2));
			else if(randy < 75)
				addToInv(new ItemStack(Items.redstone, 5));
			else if(randy < 77)
				addToInv(new ItemStack(ModItems.powder_nitan_mix, 2));
			else if(randy < 80)
				addToInv(new ItemStack(ModItems.powder_power, 2));
			else if(randy < 85)
				addToInv(new ItemStack(ModItems.powder_copper, 5));
			else if(randy < 90)
				addToInv(new ItemStack(ModItems.powder_lead, 3));
			else if(randy < 95)
				addToInv(new ItemStack(ModItems.fluorite, 4));
			else if(randy < 99)
				addToInv(new ItemStack(ModItems.powder_lapis, 4));
			else
				addToInv(new ItemStack(ModItems.powder_combine_steel, 1));
		}
	}
	
	private void addToInv(ItemStack stack) {
		
		for(int i = 0; i < 15; i++) {
			
			if(slots[i] != null && slots[i].getItem() == stack.getItem() && slots[i].getItemDamage() == stack.getItemDamage() && 
					slots[i].stackSize < slots[i].getMaxStackSize()) {
				
				slots[i].stackSize++;
				
				return;
			}
		}
		
		for(int i = 0; i < 15; i++) {
			
			if(slots[i] == null) {
				slots[i] = new ItemStack(stack.getItem(), 1, stack.getItemDamage());
				return;
			}
		}
	}
	
	private void ejectInto(int x, int y, int z) {

		TileEntity te = worldObj.getTileEntity(x, y, z);
		
		if(te instanceof IInventory) {
			
			IInventory chest = (IInventory)te;
			
			for(int i = 0; i < 15; i++) {
				
				if(slots[i] != null) {
					
					for(int j = 0; j < chest.getSizeInventory(); j++) {
						
						ItemStack sta = slots[i].copy();
						sta.stackSize = 1;
						
						if(chest.getStackInSlot(j) != null && chest.getStackInSlot(j).getItem() == slots[i].getItem() && 
								chest.getStackInSlot(j).getItemDamage() == slots[i].getItemDamage() && 
								chest.getStackInSlot(j).stackSize < chest.getStackInSlot(j).getMaxStackSize()) {
							
							slots[i].stackSize--;
							
							if(slots[i].stackSize <= 0)
								slots[i] = null;
							
							chest.setInventorySlotContents(j, new ItemStack(chest.getStackInSlot(j).getItem(), chest.getStackInSlot(j).stackSize + 1, chest.getStackInSlot(j).getItemDamage()));
							return;
						}
					}
				}
			}
			
			for(int i = 0; i < 15; i++) {
				
				if(slots[i] != null) {
					
					for(int j = 0; j < chest.getSizeInventory(); j++) {
						
						ItemStack sta = slots[i].copy();
						sta.stackSize = 1;
						
						if(chest.getStackInSlot(j) == null && chest.isItemValidForSlot(j, sta)) {
							
							slots[i].stackSize--;
							
							if(slots[i].stackSize <= 0)
								slots[i] = null;
							
							chest.setInventorySlotContents(j, new ItemStack(sta.getItem(), 1, sta.getItemDamage()));
							return;
						}
					}
				}
			}
		}
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return 65536.0D;
	}

}

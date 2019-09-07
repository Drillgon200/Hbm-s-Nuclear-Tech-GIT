package com.hbm.entity.mob;

import java.util.HashSet;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import com.hbm.lib.Library;
import com.hbm.lib.ModDamageSource;
import com.hbm.main.MainRegistry;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class EntityTaintedCreeper extends EntityMob {
    /**
     * Time when this creeper was last in an active state (Messed up code here, probably causes creeper animation to go
     * weird)
     */
    private int lastActiveTime;
    /** The amount of time since the creeper was close enough to the player to ignite */
    private int timeSinceIgnited;
    private int fuseTime = 30;
    /** Explosion radius for this creeper. */
    private int explosionRadius = 20;
    private static final String __OBFID = "CL_00001684";

    public EntityTaintedCreeper(World p_i1733_1_)
    {
        super(p_i1733_1_);
        this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAITaintedCreeperSwell(this));
        this.tasks.addTask(3, new EntityAIAttackOnCollide(this, 1.0D, false));
        this.tasks.addTask(4, new EntityAIWander(this, 0.8D));
        this.tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(6, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
        this.targetTasks.addTask(2, new EntityAIHurtByTarget(this, false));
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntityOcelot.class, 0, true));
    }

    @Override
	protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(15.0D);
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.35D);
    }

    /**
     * Returns true if the newer Entity AI code should be run
     */
    @Override
	public boolean isAIEnabled()
    {
        return true;
    }

    /**
     * The number of iterations PathFinder.getSafePoint will execute before giving up.
     */
    @Override
	public int getMaxSafePointTries()
    {
        return this.getAttackTarget() == null ? 3 : 3 + (int)(this.getHealth() - 1.0F);
    }

    /**
     * Called when the mob is falling. Calculates and applies fall damage.
     */
    @Override
	protected void fall(float p_70069_1_)
    {
        super.fall(p_70069_1_);
        this.timeSinceIgnited = (int)(this.timeSinceIgnited + p_70069_1_ * 1.5F);

        if (this.timeSinceIgnited > this.fuseTime - 5)
        {
            this.timeSinceIgnited = this.fuseTime - 5;
        }
    }

    @Override
	protected void entityInit()
    {
        super.entityInit();
        this.dataWatcher.addObject(16, Byte.valueOf((byte) - 1));
        this.dataWatcher.addObject(17, Byte.valueOf((byte)0));
        this.dataWatcher.addObject(18, Byte.valueOf((byte)0));
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    @Override
	public void writeEntityToNBT(NBTTagCompound p_70014_1_)
    {
        super.writeEntityToNBT(p_70014_1_);

        if (this.dataWatcher.getWatchableObjectByte(17) == 1)
        {
            p_70014_1_.setBoolean("powered", true);
        }

        p_70014_1_.setShort("Fuse", (short)this.fuseTime);
        p_70014_1_.setByte("ExplosionRadius", (byte)this.explosionRadius);
        p_70014_1_.setBoolean("ignited", this.func_146078_ca());
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
	public void readEntityFromNBT(NBTTagCompound p_70037_1_)
    {
        super.readEntityFromNBT(p_70037_1_);
        this.dataWatcher.updateObject(17, Byte.valueOf((byte)(p_70037_1_.getBoolean("powered") ? 1 : 0)));

        if (p_70037_1_.hasKey("Fuse", 99))
        {
            this.fuseTime = p_70037_1_.getShort("Fuse");
        }

        if (p_70037_1_.hasKey("ExplosionRadius", 99))
        {
            this.explosionRadius = p_70037_1_.getByte("ExplosionRadius");
        }

        if (p_70037_1_.getBoolean("ignited"))
        {
            this.func_146079_cb();
        }
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
	public void onUpdate()
    {
        if (this.isEntityAlive())
        {
            this.lastActiveTime = this.timeSinceIgnited;

            if (this.func_146078_ca())
            {
                this.setCreeperState(1);
            }

            int i = this.getCreeperState();

            if (i > 0 && this.timeSinceIgnited == 0)
            {
                this.playSound("creeper.primed", 1.0F * 30 / 75, 0.5F);
            }

            this.timeSinceIgnited += i;

            if (this.timeSinceIgnited < 0)
            {
                this.timeSinceIgnited = 0;
            }

            if (this.timeSinceIgnited >= this.fuseTime)
            {
                this.timeSinceIgnited = this.fuseTime;
                this.func_146077_cc();
            }
        }

        super.onUpdate();
        
        if(this.getHealth() < this.getMaxHealth() && this.ticksExisted % 10 == 0)
        {
        	this.heal(1.0F);
        }
    }

    /**
     * Returns the sound this mob makes when it is hurt.
     */
    @Override
	protected String getHurtSound()
    {
        return "mob.creeper.say";
    }

    /**
     * Returns the sound this mob makes on death.
     */
    @Override
	protected String getDeathSound()
    {
        return "mob.creeper.death";
    }

    /**
     * Called when the mob's health reaches 0.
     */
    @Override
	public void onDeath(DamageSource p_70645_1_)
    {
        super.onDeath(p_70645_1_);
    }

    @Override
	public boolean attackEntityAsMob(Entity p_70652_1_)
    {
        return true;
    }

    /**
     * Returns true if the creeper is powered by a lightning bolt.
     */
    public boolean getPowered()
    {
        return this.dataWatcher.getWatchableObjectByte(17) == 1;
    }

    /**
     * Params: (Float)Render tick. Returns the intensity of the creeper's flash when it is ignited.
     */
    @SideOnly(Side.CLIENT)
    public float getCreeperFlashIntensity(float p_70831_1_)
    {
        return (this.lastActiveTime + (this.timeSinceIgnited - this.lastActiveTime) * p_70831_1_) / (this.fuseTime - 2);
    }

    @Override
	protected Item getDropItem()
    {
        return Item.getItemFromBlock(Blocks.tnt);
    }

    /**
     * Returns the current state of creeper, -1 is idle, 1 is 'in fuse'
     */
    public int getCreeperState()
    {
        return this.dataWatcher.getWatchableObjectByte(16);
    }

    /**
     * Sets the state of creeper, -1 to idle and 1 to be 'in fuse'
     */
    public void setCreeperState(int p_70829_1_)
    {
        this.dataWatcher.updateObject(16, Byte.valueOf((byte)p_70829_1_));
    }

    /**
     * Called when a lightning bolt hits the entity.
     */
    @Override
	public void onStruckByLightning(EntityLightningBolt p_70077_1_)
    {
        super.onStruckByLightning(p_70077_1_);
        this.dataWatcher.updateObject(17, Byte.valueOf((byte)1));
    }

    /**
     * Called when a player interacts with a mob. e.g. gets milk from a cow, gets into the saddle on a pig.
     */
    @Override
	protected boolean interact(EntityPlayer p_70085_1_)
    {
        ItemStack itemstack = p_70085_1_.inventory.getCurrentItem();

        if (itemstack != null && itemstack.getItem() == Items.flint_and_steel)
        {
            this.worldObj.playSoundEffect(this.posX + 0.5D, this.posY + 0.5D, this.posZ + 0.5D, "fire.ignite", 1.0F, this.rand.nextFloat() * 0.4F + 0.8F);
            p_70085_1_.swingItem();

            if (!this.worldObj.isRemote)
            {
                this.func_146079_cb();
                itemstack.damageItem(1, p_70085_1_);
                return true;
            }
        }

        return super.interact(p_70085_1_);
    }

    private void func_146077_cc()
    {
        if (!this.worldObj.isRemote)
        {
            boolean flag = this.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing");

            if (this.getPowered())
            {
            	this.explosionRadius *= 3;
            }
            
            worldObj.newExplosion(this, posX, posY, posZ, 5.0F, false, false);
            
            if(this.getPowered())
            {
	            
			    for(int i = 0; i < 255; i++) {
			    	int a = rand.nextInt(15) + (int)posX - 7;
			    	int b = rand.nextInt(15) + (int)posY - 7;
			    	int c = rand.nextInt(15) + (int)posZ - 7;
			           if(worldObj.getBlock(a, b, c).isReplaceable(worldObj, a, b, c) && hasPosNeightbour(worldObj, a, b, c)) {
			        	   
			        	   if(!MainRegistry.enableHardcoreTaint)
			        		   worldObj.setBlock(a, b, c, ModBlocks.taint, rand.nextInt(3) + 5, 2);
			        	   else
			        		   worldObj.setBlock(a, b, c, ModBlocks.taint, rand.nextInt(3), 2);
			           }
			    }
			    
            } else {
	            
			    for(int i = 0; i < 85; i++) {
			    	int a = rand.nextInt(7) + (int)posX - 3;
			    	int b = rand.nextInt(7) + (int)posY - 3;
			    	int c = rand.nextInt(7) + (int)posZ - 3;
			           if(worldObj.getBlock(a, b, c).isReplaceable(worldObj, a, b, c) && hasPosNeightbour(worldObj, a, b, c)) {
			        	   
			        	   if(!MainRegistry.enableHardcoreTaint)
			        	   
			        		   worldObj.setBlock(a, b, c, ModBlocks.taint, rand.nextInt(6) + 10, 2);
			        	   else
			        		   worldObj.setBlock(a, b, c, ModBlocks.taint, rand.nextInt(3) + 4, 2);
			           }
			    }
            }

            this.setDead();
        }
    }
    
    public static boolean hasPosNeightbour(World world, int x, int y, int z) {
    	Block b0 = world.getBlock(x + 1, y, z);
    	Block b1 = world.getBlock(x, y + 1, z);
    	Block b2 = world.getBlock(x, y, z + 1);
    	Block b3 = world.getBlock(x - 1, y, z);
    	Block b4 = world.getBlock(x, y - 1, z);
    	Block b5 = world.getBlock(x, y, z - 1);
    	boolean b = (b0.renderAsNormalBlock() && b0.getMaterial().isOpaque()) ||
    			(b1.renderAsNormalBlock() && b1.getMaterial().isOpaque()) ||
    			(b2.renderAsNormalBlock() && b2.getMaterial().isOpaque()) ||
    			(b3.renderAsNormalBlock() && b3.getMaterial().isOpaque()) ||
    			(b4.renderAsNormalBlock() && b4.getMaterial().isOpaque()) ||
    			(b5.renderAsNormalBlock() && b5.getMaterial().isOpaque());
    	return b;
    }

    public boolean func_146078_ca()
    {
        return this.dataWatcher.getWatchableObjectByte(18) != 0;
    }

    public void func_146079_cb()
    {
        this.dataWatcher.updateObject(18, Byte.valueOf((byte)1));
    }
    
    public void setPowered(int power) {
        this.dataWatcher.updateObject(17, power);
    }
}

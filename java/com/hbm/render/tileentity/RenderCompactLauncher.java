package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.items.weapon.ItemCustomMissile;
import com.hbm.items.weapon.ItemMissile;
import com.hbm.main.ResourceManager;
import com.hbm.render.misc.ErrorPronter;
import com.hbm.render.misc.MissileMultipart;
import com.hbm.render.misc.MissilePart;
import com.hbm.render.misc.MissilePronter;
import com.hbm.tileentity.bomb.TileEntityCompactLauncher;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class RenderCompactLauncher extends TileEntitySpecialRenderer {

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float p_147500_8_) {
		
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x + 0.5F, (float) y, (float) z + 0.5F);
		
		GL11.glEnable(GL11.GL_CULL_FACE);
		
		bindTexture(ResourceManager.compact_launcher_tex);
		ResourceManager.compact_launcher.renderAll();
		
		TileEntityCompactLauncher launcher = (TileEntityCompactLauncher)tileentity;

		GL11.glTranslatef(0F, 1.0625F, 0F);
		
		/// DRAW MISSILE START
		GL11.glPushMatrix();

		MissileMultipart missile;
		
		if(launcher.load != null) {
			//ItemStack custom = launcher.getStackInSlot(0);
			
			//missile = ItemCustomMissile.getMultipart(custom);
			
			MissilePronter.prontMissile(MissileMultipart.loadFromStruct(launcher.load), Minecraft.getMinecraft().getTextureManager());
			//
		}
		
		GL11.glPopMatrix();
		/// DRAW MISSILE END

		GL11.glPopMatrix();
	}
}

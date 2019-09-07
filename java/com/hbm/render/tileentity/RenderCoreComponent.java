package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.TileEntityCoreEmitter;
import com.hbm.tileentity.machine.TileEntityCoreInjector;
import com.hbm.tileentity.machine.TileEntityCoreReceiver;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

public class RenderCoreComponent extends TileEntitySpecialRenderer {
	
	public RenderCoreComponent() { }

    @Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float f)
    {
        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y, z + 0.5);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_CULL_FACE);
        
        GL11.glRotatef(90, 0F, 1F, 0F);
        
		switch(tileEntity.getBlockMetadata()) {
		case 0:
	        GL11.glTranslated(0.0D, 0.5D, -0.5D);
			GL11.glRotatef(90, 1F, 0F, 0F); break;
		case 1:
	        GL11.glTranslated(0.0D, 0.5D, 0.5D);
			GL11.glRotatef(90, -1F, 0F, 0F); break;
		case 2:
			GL11.glRotatef(90, 0F, 1F, 0F); break;
		case 4:
			GL11.glRotatef(180, 0F, 1F, 0F); break;
		case 3:
			GL11.glRotatef(270, 0F, 1F, 0F); break;
		case 5:
			GL11.glRotatef(0, 0F, 1F, 0F); break;
		}
		
        GL11.glTranslated(0.0D, 0D, 0.0D);

        if(tileEntity instanceof TileEntityCoreEmitter) {
	        bindTexture(ResourceManager.dfc_emitter_tex);
	        ResourceManager.dfc_emitter.renderAll();
        }

        if(tileEntity instanceof TileEntityCoreReceiver) {
	        bindTexture(ResourceManager.dfc_receiver_tex);
	        ResourceManager.dfc_receiver.renderAll();
        }

        if(tileEntity instanceof TileEntityCoreInjector) {
	        bindTexture(ResourceManager.dfc_injector_tex);
	        ResourceManager.dfc_injector.renderAll();
        }
        
        GL11.glEnable(GL11.GL_LIGHTING);

        GL11.glPopMatrix();
    }

}

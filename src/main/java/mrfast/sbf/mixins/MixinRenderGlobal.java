package mrfast.sbf.mixins;

import java.awt.*;
import java.nio.FloatBuffer;
import java.util.List;

import mrfast.sbf.utils.OutlineUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.SkyblockInfo;
import mrfast.sbf.utils.ItemRarity;
import mrfast.sbf.utils.ItemUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team.EnumVisible;


/**
 * Modified from LobbyGlow
 * https://github.com/biscuut/LobbyGlow
 * @author Biscuut
 */
@Mixin(RenderGlobal.class)
public abstract class MixinRenderGlobal {
    @Final @Shadow private RenderManager renderManager;

    @Shadow protected abstract boolean isRenderEntityOutlines();

    @Redirect(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderGlobal;isRenderEntityOutlines()Z", ordinal = 0))
    private boolean onRenderEntities(RenderGlobal renderGlobal) {
        return false;
    }

    // Remove condition by always returning true
    @Redirect(method = "isRenderEntityOutlines", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;isSpectator()Z", ordinal = 0))
    private boolean isSpectatorDisableCheck(EntityPlayerSP entityPlayerSP) {
        return true;
    }

    // Instead of key down, check if they are in the lobby
    @Redirect(method = "isRenderEntityOutlines", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/settings/KeyBinding;isKeyDown()Z", ordinal = 0))
    private boolean isKeyDownDisableCheck(KeyBinding keyBinding) {
        boolean items = SkyblockFeatures.config.glowingItems && Utils.inSkyblock;
        boolean players = SkyblockFeatures.config.glowingDungeonPlayers && Utils.inDungeons;
        boolean zealots = SkyblockFeatures.config.glowingZealots && SkyblockInfo.map.equals("The End");
        return items || players | zealots;
    }

    @Inject(method = "renderEntities", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V", shift = At.Shift.BEFORE, ordinal = 2, args = {"ldc=entities"}), locals = LocalCapture.CAPTURE_FAILSOFT) // Optifine version
    private void renderEntities(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci, int pass, double d0, double d1, double d2, Entity entity, double d3, double d4, double d5, List<Entity> list, boolean bool0, boolean bool1) {
        displayOutlines(list, d0, d1, d2, camera, partialTicks);
    }

    @Inject(method = "renderEntities", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V", shift = At.Shift.BEFORE, ordinal = 2, args = {"ldc=entities"}), locals = LocalCapture.CAPTURE_FAILSOFT) // Non-optifine version
    private void renderEntities(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci, int pass, double d0, double d1, double d2, Entity entity, double d3, double d4, double d5, List<Entity> list) {
        displayOutlines(list, d0, d1, d2, camera, partialTicks);
    }
    
    private final FloatBuffer BUF_FLOAT_4 = BufferUtils.createFloatBuffer(4);
    private static Framebuffer swapBuffer = null;

    private static Framebuffer getOrCreateSwapBuffer() {
        if (swapBuffer == null) {
            Framebuffer main = Minecraft.getMinecraft().getFramebuffer();
            swapBuffer = new Framebuffer(main.framebufferTextureWidth, main.framebufferTextureHeight, true);
            swapBuffer.setFramebufferFilter(GL11.GL_NEAREST);
            swapBuffer.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
        }
        return swapBuffer;
    }
    
    private static void updateFramebufferSize() {
        Framebuffer swapBuffer = getOrCreateSwapBuffer();
        int width = Minecraft.getMinecraft().displayWidth;
        int height = Minecraft.getMinecraft().displayHeight;
        
        if (swapBuffer.framebufferWidth != width || swapBuffer.framebufferHeight != height) {
            swapBuffer.createBindFramebuffer(width, height);
        }
        
        RenderGlobal rg = Minecraft.getMinecraft().renderGlobal;
        Framebuffer outlineBuffer = rg.entityOutlineFramebuffer;
        
        if (outlineBuffer.framebufferWidth != width || outlineBuffer.framebufferHeight != height) {
            outlineBuffer.createBindFramebuffer(width, height);
            rg.entityOutlineShader.createBindFramebuffers(width, height);
        }
    }
    private void displayOutlines(List<Entity> entities, double x, double y, double z, ICamera camera, float partialTicks) {
        if (isRenderEntityOutlines()) {
            GlStateManager.pushMatrix();

            Minecraft mc = Minecraft.getMinecraft();
            RenderGlobal renderGlobal = mc.renderGlobal;

            mc.theWorld.theProfiler.endStartSection("entityOutlines");
            updateFramebufferSize();
            // Clear and bind the outline framebuffer
            renderGlobal.entityOutlineFramebuffer.framebufferClear();
            renderGlobal.entityOutlineFramebuffer.bindFramebuffer(false);

            // Vanilla options
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableFog();
            mc.getRenderManager().setRenderOutlines(true);
            
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL13.GL_COMBINE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_RGB, GL11.GL_REPLACE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_RGB, GL13.GL_CONSTANT);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_ALPHA, GL11.GL_REPLACE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_ALPHA, GL11.GL_TEXTURE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA);
            GL11.glColor4d(1f,1f,1f,1f);

            GlStateManager.depthFunc(GL11.GL_ALWAYS);
            try {
                for (Entity entity : entities) {
                    boolean inRange = entity.isInRangeToRender3d(x, y, z) && (entity.ignoreFrustumCheck || camera.isBoundingBoxInFrustum(entity.getEntityBoundingBox()));

                    OutlineUtils.EntityOutline outline = OutlineUtils.getOutline(entity);

                    if(outline!=null && inRange) {
                        if(!outline.renderNow || outline.entity==null) continue;
                        outline.renderCount+=1;
                        if(outline.renderCount>25) {
                            outline.renderCount=0;
                            outline.renderNow=false;
                        }
                        if(!outline.throughWalls) {
                            if(!Utils.GetMC().thePlayer.canEntityBeSeen(outline.entity)) continue;
                        }
                        if(outline.outlineColor!=null) setColor(outline.outlineColor);
                        renderManager.renderEntityStatic(entity,partialTicks,true);
                    }

                    // Item Glowing
                    boolean inItemRange = (mc.thePlayer.getDistanceToEntity(entity) < 15.0F && entity instanceof EntityItem);
                    if (inItemRange && SkyblockFeatures.config.glowingItems) {
                        ItemRarity itemRarity = ItemUtils.getRarity(((EntityItem)entity).getEntityItem());
                        outlineColor(itemRarity.getColor().getRGB());
                        renderManager.renderEntityStatic(entity, partialTicks,true);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            GlStateManager.depthFunc(GL11.GL_LEQUAL);

            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_RGB, GL11.GL_MODULATE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_RGB, GL11.GL_TEXTURE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_ALPHA, GL11.GL_MODULATE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_ALPHA, GL11.GL_TEXTURE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA);

            // Vanilla options
            RenderHelper.enableStandardItemLighting();
            mc.getRenderManager().setRenderOutlines(false);

            // Load the outline shader
            GlStateManager.depthMask(false);
            renderGlobal.entityOutlineShader.loadShaderGroup(partialTicks);
            GlStateManager.depthMask(true);

            // Reset GL/framebuffers for next render layers
            GlStateManager.enableLighting();
            mc.getFramebuffer().bindFramebuffer(false);
            GlStateManager.enableFog();
            GlStateManager.enableBlend();
            GlStateManager.enableColorMaterial();
            GlStateManager.enableDepth();
            GlStateManager.enableAlpha();

            GlStateManager.popMatrix();

        }

    }


    private void outlineColor(int color) {
        BUF_FLOAT_4.put(0, (float)(color >> 16 & 255) / 255.0F);
        BUF_FLOAT_4.put(1, (float)(color >> 8 & 255) / 255.0F);
        BUF_FLOAT_4.put(2, (float)(color & 255) / 255.0F);
        BUF_FLOAT_4.put(3, 1);

        GL11.glTexEnv(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_COLOR, BUF_FLOAT_4);
    }

    private void setColor(Color color) {
        GL11.glColor4d(
                (color.getRed()/255f),
                (color.getGreen()/255f),
                (color.getBlue()/255f),
                (color.getAlpha()/255f)
        );
    }

    private void outlineColor(Entity entity, String string) {
        if(!(entity instanceof EntityPlayer)) return;
        ScorePlayerTeam scoreplayerteam = (ScorePlayerTeam) ((EntityPlayer) entity).getTeam();
        if (scoreplayerteam != null && scoreplayerteam.getNameTagVisibility() != EnumVisible.NEVER) {
            scoreplayerteam.setNamePrefix(string);
        }
    }

}
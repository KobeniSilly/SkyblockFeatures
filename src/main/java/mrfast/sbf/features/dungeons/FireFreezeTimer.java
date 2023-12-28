package mrfast.sbf.features.dungeons;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.GuiUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FireFreezeTimer {

    String bossDialogue = "[BOSS] The Professor: Oh? You found my Guardians' one weakness?";

    static String display = "";
    private static final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onChatMessage(ClientChatReceivedEvent event) {
        if (!Utils.inDungeons || event.type == 2) return;
        String text = event.message.getUnformattedText();
        if(text.startsWith(bossDialogue) && SkyblockFeatures.config.fireFreezeTimer) {
            for(int i=1; i<=8;i++) {
                String count = (i == 7) ? "§aFire Freeze Now!" : (i == 6) ? "§aFire Freeze Now!" : (i < 6) ? "§cFire Freeze in " + (6 - i) + " seconds" : "";

                Utils.setTimeout(()->{
                    display = count;
                },i*860);
            }
        }
    }

    @SubscribeEvent
    public void onLoad(WorldEvent.Load event) {
        display = "";
    }

    static {
        new fireFreezeGUI();
    }

    public static class fireFreezeGUI extends UIElement {

        public fireFreezeGUI() {
            super("Fire Freeze Timer", new Point(0.2f, 0.0f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            float scale = 2f;

            GlStateManager.scale(scale, scale, 0);
            GuiUtils.drawText(display, 0, 0, GuiUtils.TextStyle.DROP_SHADOW);
            GlStateManager.scale(1 / scale, 1 / scale, 0);
        }

        @Override
        public void drawElementExample() {
            float scale = 2f;

            GlStateManager.scale(scale, scale, 0);
            GuiUtils.drawText("§cFire Freeze in 5 seconds!", 0, 0, GuiUtils.TextStyle.DROP_SHADOW);
            GlStateManager.scale(1/scale, 1/scale, 0);
        }

        @Override
        public boolean getToggled() {
            return SkyblockFeatures.config.fireFreezeTimer;
        }

        @Override
        public boolean getRequirement() {
            return Utils.inDungeons && Utils.inSkyblock;
        }

        @Override
        public int getHeight() {
            return Utils.GetMC().fontRendererObj.FONT_HEIGHT*2;
        }

        @Override
        public int getWidth() {
            return Utils.GetMC().fontRendererObj.getStringWidth("§6Fire Freeze in 5 seconds")*2;
        }
    }
}

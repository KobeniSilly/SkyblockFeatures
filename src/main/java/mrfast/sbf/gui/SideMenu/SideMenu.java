package mrfast.sbf.gui.SideMenu;

import mrfast.sbf.mixins.transformers.GuiContainerAccessor;
import mrfast.sbf.utils.GuiUtils;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

public class SideMenu {

    private Map<String,CustomElement> elements;
    int x,y = 0;

    public Map<String,CustomElement> getElements() {
        return elements;
    }

    public SideMenu() {
        this.elements = new HashMap<>();
    }

    public void addOrUpdateElement(String name,CustomElement element) {
        element.parent = this;
        elements.put(name,element);
    }

    public void render(int x, int y, int width, int height, GuiContainerAccessor gui) {
        this.x=x;
        this.y=y;

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int guiLeft = gui.getGuiLeft();
        int guiTop = gui.getGuiTop();
        int guiWidth = gui.getWidth();
        int guiHeight = gui.getHeight();
        int maxWidth = 0;
        for (CustomElement element : elements.values()) {
            if(element.width>maxWidth) maxWidth=element.width;
        }
        GlStateManager.translate(0,0,340f);
        GuiUtils.drawGraySquareWithBorder(x+guiLeft+guiWidth,y+guiTop,maxWidth+8,height);
        for (CustomElement element : elements.values()) {
            element.render(x,y);
        }
        GlStateManager.translate(0,0,-340f);
    }
}


package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.gui.EntryWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Tessellator;

public abstract class EntryListWidget extends ListWidget {
    public EntryListWidget(MinecraftClient minecraftClient, int i, int j, int k, int l, int m) {
        super(minecraftClient, i, j, k, l, m);
    }

    @Override
    protected void selectEntry(int index, boolean doubleClick, int lastMouseX, int lastMouseY) {
    }

    @Override
    protected boolean isEntrySelected(int index) {
        return false;
    }

    @Override
    protected void renderBackground() {
    }

    @Override
    protected void method_1055(int i, int j, int k, int l, Tessellator tessellator, int m, int n) {
        this.getEntry(i).draw(i, j, k, this.getRowWidth(), l, tessellator, m, n, this.getEntryAt(m, n) == i);
    }

    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (this.isMouseInList(mouseY)) {
            int var4 = this.getEntryAt(mouseX, mouseY);
            if (var4 >= 0) {
                int var5 = this.xStart + this.width / 2 - this.getRowWidth() / 2 + 2;
                int var6 = this.yStart + 4 - this.getScrollAmount() + var4 * this.entryHeight + this.headerHeight;
                int var7 = mouseX - var5;
                int var8 = mouseY - var6;
                if (this.getEntry(var4).mouseClicked(var4, mouseX, mouseY, button, var7, var8)) {
                    this.setDragging(false);
                    return true;
                }
            }
        }

        return false;
    }

    public boolean mouseReleased(int mouseX, int mouseY, int button) {
        for (int var4 = 0; var4 < this.getEntryCount(); var4++) {
            int var5 = this.xStart + this.width / 2 - this.getRowWidth() / 2 + 2;
            int var6 = this.yStart + 4 - this.getScrollAmount() + var4 * this.entryHeight + this.headerHeight;
            int var7 = mouseX - var5;
            int var8 = mouseY - var6;
            this.getEntry(var4).mouseReleased(var4, mouseX, mouseY, button, var7, var8);
        }

        this.setDragging(true);
        return false;
    }

    public abstract EntryWidget getEntry(int i);
}

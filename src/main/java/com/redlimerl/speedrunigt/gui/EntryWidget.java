package com.redlimerl.speedrunigt.gui;

import net.minecraft.client.render.Tessellator;

public interface EntryWidget {
    void draw(int i, int j, int k, int l, int m, Tessellator tessellator, int n, int o, boolean bl);

    boolean mouseClicked(int index, int mouseX, int mouseY, int button, int x, int y);

    void mouseReleased(int index, int mouseX, int mouseY, int button, int x, int y);
}

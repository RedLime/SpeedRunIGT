package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.render.GLXExt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.Tessellator;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public abstract class ListWidget {
    private final Minecraft client;
    protected int width;
    private int height;
    protected int yStart;
    protected int yEnd;
    protected int xEnd;
    protected int xStart;
    protected final int entryHeight;
    private int homeButtonId;
    private int endButtonId;
    protected int lastMouseX;
    protected int lastMouseY;
    protected boolean centerListVertically = true;
    private float field_1253 = -2.0F;
    private float scrollSpeed;
    private float scrollAmount;
    private int selectedEntry = -1;
    private long time;
    private boolean renderSelection = true;
    private boolean renderHeader;
    protected int headerHeight;
    private boolean dragging = true;

    public ListWidget(Minecraft minecraftClient, int i, int j, int k, int l, int m) {
        this.client = minecraftClient;
        this.width = i;
        this.height = j;
        this.yStart = k;
        this.yEnd = l;
        this.entryHeight = m;
        this.xStart = 0;
        this.xEnd = i;
    }

    public void updateBounds(int right, int height, int top, int bottom) {
        this.width = right;
        this.height = height;
        this.yStart = top;
        this.yEnd = bottom;
        this.xStart = 0;
        this.xEnd = right;
    }

    public void setRenderSelection(boolean renderSelection) {
        this.renderSelection = renderSelection;
    }

    protected void setHeader(boolean renderHeader, int headerHeight) {
        this.renderHeader = renderHeader;
        this.headerHeight = headerHeight;
        if (!renderHeader) {
            this.headerHeight = 0;
        }
    }

    protected abstract int getEntryCount();

    protected abstract void selectEntry(int index, boolean doubleClick, int lastMouseX, int lastMouseY);

    protected abstract boolean isEntrySelected(int index);

    protected int getMaxPosition() {
        return this.getEntryCount() * this.entryHeight + this.headerHeight;
    }

    protected abstract void renderBackground();

    protected abstract void method_1055(int i, int j, int k, int l, Tessellator tessellator, int m, int n);

    protected void renderHeader(int x, int y, Tessellator tessellator) {
    }

    protected void clickedHeader(int mouseX, int mouseY) {
    }

    protected void renderDecorations(int mouseX, int mouseY) {
    }

    public int getEntryAt(int x, int y) {
        int var3 = this.xStart + this.width / 2 - this.getRowWidth() / 2;
        int var4 = this.xStart + this.width / 2 + this.getRowWidth() / 2;
        int var5 = y - this.yStart - this.headerHeight + (int)this.scrollAmount - 4;
        int var6 = var5 / this.entryHeight;
        return x < this.getScrollbarPosition() && x >= var3 && x <= var4 && var6 >= 0 && var5 >= 0 && var6 < this.getEntryCount() ? var6 : -1;
    }

    public void setButtonIds(int homeButtonId, int endButtonId) {
        this.homeButtonId = homeButtonId;
        this.endButtonId = endButtonId;
    }

    private void capYPosition() {
        int var1 = this.getMaxScroll();
        if (var1 < 0) {
            var1 /= 2;
        }

        if (!this.centerListVertically && var1 < 0) {
            var1 = 0;
        }

        if (this.scrollAmount < 0.0F) {
            this.scrollAmount = 0.0F;
        }

        if (this.scrollAmount > (float)var1) {
            this.scrollAmount = (float)var1;
        }
    }

    public int getMaxScroll() {
        return this.getMaxPosition() - (this.yEnd - this.yStart - 4);
    }

    public int getScrollAmount() {
        return (int)this.scrollAmount;
    }

    public boolean isMouseInList(int mouseY) {
        return mouseY >= this.yStart && mouseY <= this.yEnd;
    }

    public void scroll(int amount) {
        this.scrollAmount += (float)amount;
        this.capYPosition();
        this.field_1253 = -2.0F;
    }

    public void buttonClicked(ButtonWidget button) {
        if (button.active) {
            if (button.id == this.homeButtonId) {
                this.scrollAmount = this.scrollAmount - (float)(this.entryHeight * 2 / 3);
                this.field_1253 = -2.0F;
                this.capYPosition();
            } else if (button.id == this.endButtonId) {
                this.scrollAmount = this.scrollAmount + (float)(this.entryHeight * 2 / 3);
                this.field_1253 = -2.0F;
                this.capYPosition();
            }
        }
    }

    public void render(int mouseX, int mouseY, float tickDelta) {
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        this.renderBackground();
        int var4 = this.getEntryCount();
        int var5 = this.getScrollbarPosition();
        int var6 = var5 + 6;
        if (mouseX > this.xStart && mouseX < this.xEnd && mouseY > this.yStart && mouseY < this.yEnd) {
            if (!Mouse.isButtonDown(0) || !this.isDragging()) {
                for (; Mouse.next(); this.client.currentScreen.handleMouse()) {
                    int var15 = Mouse.getEventDWheel();
                    if (var15 != 0) {
                        if (var15 > 0) {
                            var15 = -1;
                        } else if (var15 < 0) {
                            var15 = 1;
                        }

                        this.scrollAmount = this.scrollAmount + (float)(var15 * this.entryHeight / 2);
                    }
                }

                this.field_1253 = -1.0F;
            } else if (this.field_1253 == -1.0F) {
                boolean var7 = true;
                if (mouseY >= this.yStart && mouseY <= this.yEnd) {
                    int var8 = this.width / 2 - this.getRowWidth() / 2;
                    int var9 = this.width / 2 + this.getRowWidth() / 2;
                    int var10 = mouseY - this.yStart - this.headerHeight + (int)this.scrollAmount - 4;
                    int var11 = var10 / this.entryHeight;
                    if (mouseX >= var8 && mouseX <= var9 && var11 >= 0 && var10 >= 0 && var11 < var4) {
                        boolean var12 = var11 == this.selectedEntry && Minecraft.getTime() - this.time < 250L;
                        this.selectEntry(var11, var12, mouseX, mouseY);
                        this.selectedEntry = var11;
                        this.time = Minecraft.getTime();
                    } else if (mouseX >= var8 && mouseX <= var9 && var10 < 0) {
                        this.clickedHeader(mouseX - var8, mouseY - this.yStart + (int)this.scrollAmount - 4);
                        var7 = false;
                    }

                    if (mouseX >= var5 && mouseX <= var6) {
                        this.scrollSpeed = -1.0F;
                        int var21 = this.getMaxScroll();
                        if (var21 < 1) {
                            var21 = 1;
                        }

                        int var13 = (int)((float)((this.yEnd - this.yStart) * (this.yEnd - this.yStart)) / (float)this.getMaxPosition());
                        if (var13 < 32) {
                            var13 = 32;
                        }

                        if (var13 > this.yEnd - this.yStart - 8) {
                            var13 = this.yEnd - this.yStart - 8;
                        }

                        this.scrollSpeed = this.scrollSpeed / ((float)(this.yEnd - this.yStart - var13) / (float)var21);
                    } else {
                        this.scrollSpeed = 1.0F;
                    }

                    if (var7) {
                        this.field_1253 = (float)mouseY;
                    } else {
                        this.field_1253 = -2.0F;
                    }
                } else {
                    this.field_1253 = -2.0F;
                }
            } else if (this.field_1253 >= 0.0F) {
                this.scrollAmount = this.scrollAmount - ((float)mouseY - this.field_1253) * this.scrollSpeed;
                this.field_1253 = (float)mouseY;
            }
        }

        this.capYPosition();
        GL11.glDisable(2896);
        GL11.glDisable(2912);
        Tessellator var16 = Tessellator.INSTANCE;
        this.client.textureManager.bindTexture(this.client.textureManager.getTextureFromPath("/gui/background.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        float var17 = 32.0F;
        var16.begin();
        var16.color(2105376);
        var16.vertex(
                this.xStart, this.yEnd, 0.0, (float)this.xStart / var17, (float)(this.yEnd + (int)this.scrollAmount) / var17
        );
        var16.vertex(
                this.xEnd, this.yEnd, 0.0, (float)this.xEnd / var17, (float)(this.yEnd + (int)this.scrollAmount) / var17
        );
        var16.vertex(
                this.xEnd, this.yStart, 0.0, (float)this.xEnd / var17, (float)(this.yStart + (int)this.scrollAmount) / var17
        );
        var16.vertex(
                this.xStart,
                this.yStart,
                0.0,
                (float)this.xStart / var17,
                (float)(this.yStart + (int)this.scrollAmount) / var17
        );
        var16.end();
        int var18 = this.xStart + this.width / 2 - this.getRowWidth() / 2 + 2;
        int var19 = this.yStart + 4 - (int)this.scrollAmount;
        if (this.renderHeader) {
            this.renderHeader(var18, var19, var16);
        }

        this.renderList(var18, var19, mouseX, mouseY);
        GL11.glDisable(2929);
        byte var20 = 4;
        this.renderHoleBackground(0, this.yStart, 255, 255);
        this.renderHoleBackground(this.yEnd, this.height, 255, 255);
        GL11.glEnable(3042);
        GLXExt.glBlendFuncSeparate(770, 771, 0, 1);
        GL11.glDisable(3008);
        GL11.glShadeModel(7425);
        GL11.glDisable(3553);
        var16.begin();
        var16.color(0, 0);
        var16.vertex(this.xStart, this.yStart + var20, 0.0, 0.0, 1.0);
        var16.vertex(this.xEnd, this.yStart + var20, 0.0, 1.0, 1.0);
        var16.color(0, 255);
        var16.vertex(this.xEnd, this.yStart, 0.0, 1.0, 0.0);
        var16.vertex(this.xStart, this.yStart, 0.0, 0.0, 0.0);
        var16.end();
        var16.begin();
        var16.color(0, 255);
        var16.vertex(this.xStart, this.yEnd, 0.0, 0.0, 1.0);
        var16.vertex(this.xEnd, this.yEnd, 0.0, 1.0, 1.0);
        var16.color(0, 0);
        var16.vertex(this.xEnd, this.yEnd - var20, 0.0, 1.0, 0.0);
        var16.vertex(this.xStart, this.yEnd - var20, 0.0, 0.0, 0.0);
        var16.end();
        int var22 = this.getMaxScroll();
        if (var22 > 0) {
            int var23 = (this.yEnd - this.yStart) * (this.yEnd - this.yStart) / this.getMaxPosition();
            if (var23 < 32) {
                var23 = 32;
            }

            if (var23 > this.yEnd - this.yStart - 8) {
                var23 = this.yEnd - this.yStart - 8;
            }

            int var14 = (int)this.scrollAmount * (this.yEnd - this.yStart - var23) / var22 + this.yStart;
            if (var14 < this.yStart) {
                var14 = this.yStart;
            }

            var16.begin();
            var16.color(0, 255);
            var16.vertex(var5, this.yEnd, 0.0, 0.0, 1.0);
            var16.vertex(var6, this.yEnd, 0.0, 1.0, 1.0);
            var16.vertex(var6, this.yStart, 0.0, 1.0, 0.0);
            var16.vertex(var5, this.yStart, 0.0, 0.0, 0.0);
            var16.end();
            var16.begin();
            var16.color(8421504, 255);
            var16.vertex((double)var5, (double)(var14 + var23), 0.0, 0.0, 1.0);
            var16.vertex((double)var6, (double)(var14 + var23), 0.0, 1.0, 1.0);
            var16.vertex((double)var6, (double)var14, 0.0, 1.0, 0.0);
            var16.vertex((double)var5, (double)var14, 0.0, 0.0, 0.0);
            var16.end();
            var16.begin();
            var16.color(12632256, 255);
            var16.vertex((double)var5, (double)(var14 + var23 - 1), 0.0, 0.0, 1.0);
            var16.vertex((double)(var6 - 1), (double)(var14 + var23 - 1), 0.0, 1.0, 1.0);
            var16.vertex((double)(var6 - 1), (double)var14, 0.0, 1.0, 0.0);
            var16.vertex((double)var5, (double)var14, 0.0, 0.0, 0.0);
            var16.end();
        }

        this.renderDecorations(mouseX, mouseY);
        GL11.glEnable(3553);
        GL11.glShadeModel(7424);
        GL11.glEnable(3008);
        GL11.glDisable(3042);
    }

    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    public boolean isDragging() {
        return this.dragging;
    }

    public int getRowWidth() {
        return 220;
    }

    protected void renderList(int x, int y, int mouseX, int mouseY) {
        int var5 = this.getEntryCount();
        Tessellator var6 = Tessellator.INSTANCE;

        for (int var7 = 0; var7 < var5; var7++) {
            int var8 = y + var7 * this.entryHeight + this.headerHeight;
            int var9 = this.entryHeight - 4;
            if (var8 <= this.yEnd && var8 + var9 >= this.yStart) {
                if (this.renderSelection && this.isEntrySelected(var7)) {
                    int var10 = this.xStart + (this.width / 2 - this.getRowWidth() / 2);
                    int var11 = this.xStart + this.width / 2 + this.getRowWidth() / 2;
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    GL11.glDisable(3553);
                    var6.begin();
                    var6.color(8421504);
                    var6.vertex(var10, var8 + var9 + 2, 0.0, 0.0, 1.0);
                    var6.vertex(var11, var8 + var9 + 2, 0.0, 1.0, 1.0);
                    var6.vertex(var11, var8 - 2, 0.0, 1.0, 0.0);
                    var6.vertex(var10, var8 - 2, 0.0, 0.0, 0.0);
                    var6.color(0);
                    var6.vertex(var10 + 1, var8 + var9 + 1, 0.0, 0.0, 1.0);
                    var6.vertex(var11 - 1, var8 + var9 + 1, 0.0, 1.0, 1.0);
                    var6.vertex(var11 - 1, var8 - 1, 0.0, 1.0, 0.0);
                    var6.vertex(var10 + 1, var8 - 1, 0.0, 0.0, 0.0);
                    var6.end();
                    GL11.glEnable(3553);
                }

                this.method_1055(var7, x, var8, var9, var6, mouseX, mouseY);
            }
        }
    }

    protected int getScrollbarPosition() {
        return this.width / 2 + 124;
    }

    private void renderHoleBackground(int top, int bottom, int topAlpha, int bottomAlpha) {
        Tessellator var5 = Tessellator.INSTANCE;
        this.client.textureManager.bindTexture(this.client.textureManager.getTextureFromPath("/gui/background.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        float var6 = 32.0F;
        var5.begin();
        var5.color(4210752, bottomAlpha);
        var5.vertex(this.xStart, bottom, 0.0, 0.0, (float)bottom / var6);
        var5.vertex(this.xStart + this.width, bottom, 0.0, (float)this.width / var6, (float)bottom / var6);
        var5.color(4210752, topAlpha);
        var5.vertex(this.xStart + this.width, top, 0.0, (float)this.width / var6, (float)top / var6);
        var5.vertex(this.xStart, top, 0.0, 0.0, (float)top / var6);
        var5.end();
    }

    public void setXPos(int x) {
        this.xStart = x;
        this.xEnd = x + this.width;
    }

    public int getItemHeight() {
        return this.entryHeight;
    }
}

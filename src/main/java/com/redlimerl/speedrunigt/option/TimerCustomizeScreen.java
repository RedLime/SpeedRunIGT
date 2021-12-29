package com.redlimerl.speedrunigt.option;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.option.SpeedRunOptions.TimerDecimals;
import com.redlimerl.speedrunigt.option.SpeedRunOptions.TimerDecoration;
import com.redlimerl.speedrunigt.timer.TimerDrawer;
import com.redlimerl.speedrunigt.version.ColorMixer;
import com.redlimerl.speedrunigt.version.ScreenTexts;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.PagedEntryListWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Locale;
/**
 * @author Void_X_Walker
 * @reason Backported to 1.8, redid almost everything because 1.8 screens and buttons work completely different
 */
public class TimerCustomizeScreen extends Screen {

    private final TimerDrawer drawer = new TimerDrawer(false);
    private final Screen parent;

    private boolean changed = false;
    private boolean hide = false;
    private final ArrayList<ButtonWidget> normalOptions = new ArrayList<>();
    private final ArrayList<ButtonWidget> igtOptions = new ArrayList<>();
    private final ArrayList<ButtonWidget> rtaOptions = new ArrayList<>();
    private ButtonWidget normalButton;
    private ButtonWidget igtButton;
    private ButtonWidget rtaButton;
    private ButtonWidget saveButton;

private final String title = SpeedRunIGT.translate("speedrunigt.option.timer_position","Timer Display Options").getString();
    public TimerCustomizeScreen(Screen parent) {
        super();
        this.parent = parent;
    }
    @Override
    public void resize(MinecraftClient client, int width, int height) {
        normalOptions.clear();
        igtOptions.clear();
        rtaOptions.clear();
        super.resize(client, width, height);
    }
    @Override
    public void init() {


        initNormal();
        initIGTButtons();
        initRTAButtons();

        this.normalButton =new ButtonWidget(5001,width / 2 - 89, height / 2 - 48, 58, 20, new TranslatableText("options.title").getString() + "...");
        buttons.add(normalButton);
        this.normalButton.active = false;

        this.igtButton = new ButtonWidget(5002,width / 2 - 29, height / 2 - 48, 58, 20, new LiteralText("IGT...").getString());
        buttons.add(igtButton);

        this.rtaButton =new ButtonWidget(5003,width / 2 + 31, height / 2 - 48, 58, 20, new LiteralText("RTA...").getString());
        this.buttons.add(rtaButton);


        buttons.add(new ButtonWidget(5005,width / 2 - 89, height / 2 + 62, 58, 20, SpeedRunIGT.translate("speedrunigt.option.hide","Hide").getString()));

        this.saveButton = new ButtonWidget(5006,width / 2 - 29, height / 2 + 62, 58, 20, SpeedRunIGT.translate("speedrunigt.option.save","Save").getString());
        buttons.add(saveButton);

        buttons.add(new ButtonWidget(5007,width / 2 + 31, height / 2 + 62, 58, 20, ScreenTexts.CANCEL));


        for (ButtonWidget normalOption : normalOptions) {
            normalOption.visible = true;
        }
        for (ButtonWidget igtOption : igtOptions) {
            igtOption.visible = false;
        }
        for (ButtonWidget rtaOption : rtaOptions) {
            rtaOption.visible = false;
        }

    }
    protected void buttonClicked(ButtonWidget button) {
        if(button.id==5001){

                this.normalButton.active = false;
                this.igtButton.active = true;
                this.rtaButton.active = true;
                for (ButtonWidget normalOption : normalOptions) {
                    normalOption.visible = true;
                }
                for (ButtonWidget igtOption : igtOptions) {
                    igtOption.visible = false;
                }
                for (ButtonWidget rtaOption : rtaOptions) {
                    rtaOption.visible = false;
                }

        }
        else if(button.id==5002){
                this.normalButton.active = true;
                this.igtButton.active = false;
                this.rtaButton.active = true;

                for (ButtonWidget normalOption : normalOptions) {
                    normalOption.visible = false;
                }
                for (ButtonWidget igtOption : igtOptions) {
                    igtOption.visible = true;
                }
                for (ButtonWidget rtaOption : rtaOptions) {
                    rtaOption.visible = false;
                }

        }
        else if(button.id==5003){
                this.normalButton.active = true;
                this.igtButton.active = true;
                this.rtaButton.active = false;

                for (ButtonWidget normalOption : normalOptions) {
                    normalOption.visible = false;
                }
                for (ButtonWidget igtOption : igtOptions) {
                    igtOption.visible = false;
                }
                for (ButtonWidget rtaOption : rtaOptions) {
                    rtaOption.visible = true;
                }

        }
        else if(button.id==5004){
                this.normalButton.active = true;
                this.igtButton.active = true;
                this.rtaButton.active = true;
                for (ButtonWidget normalOption : normalOptions) {
                    normalOption.visible = false;
                }
                for (ButtonWidget igtOption : igtOptions) {
                    igtOption.visible = false;
                }
                for (ButtonWidget rtaOption : rtaOptions) {
                    rtaOption.visible = false;
                }

        }
        else if(button.id==5005){
                hide = !hide;
                for (ButtonWidget normalOption : normalOptions) {
                    normalOption.visible = !hide && !normalButton.active;
                }
                for (ButtonWidget igtOption : igtOptions) {
                    igtOption.visible = !hide && !igtButton.active;
                }
                for (ButtonWidget rtaOption : rtaOptions) {
                    rtaOption.visible = !hide && !rtaButton.active;
                }

                button.message=(SpeedRunIGT.translate("speedrunigt.option." + (!hide ? "hide" : "show"), (!hide ? "Hide" : "Show"))).getString();
        }
        else if(button.id==5006){
                SpeedRunOptions.setOption(SpeedRunOptions.TIMER_IGT_POSITION_X, drawer.getIGT_XPos());
                SpeedRunIGT.TIMER_DRAWER.setIGT_XPos(drawer.getIGT_XPos());
                SpeedRunOptions.setOption(SpeedRunOptions.TIMER_IGT_POSITION_Y, drawer.getIGT_YPos());
                SpeedRunIGT.TIMER_DRAWER.setIGT_YPos(drawer.getIGT_YPos());
                SpeedRunOptions.setOption(SpeedRunOptions.TIMER_IGT_SCALE, drawer.getIGTScale());
                SpeedRunIGT.TIMER_DRAWER.setIGTScale(drawer.getIGTScale());
                SpeedRunOptions.setOption(SpeedRunOptions.TIMER_IGT_COLOR, drawer.getIGTColor());
                SpeedRunIGT.TIMER_DRAWER.setIGTColor(drawer.getIGTColor());
                SpeedRunOptions.setOption(SpeedRunOptions.TIMER_IGT_DECO, drawer.getIGTDecoration());
                SpeedRunIGT.TIMER_DRAWER.setIGTDecoration(drawer.getIGTDecoration());

                SpeedRunOptions.setOption(SpeedRunOptions.TIMER_RTA_POSITION_X, drawer.getRTA_XPos());
                SpeedRunIGT.TIMER_DRAWER.setRTA_XPos(drawer.getRTA_XPos());
                SpeedRunOptions.setOption(SpeedRunOptions.TIMER_RTA_POSITION_Y, drawer.getRTA_YPos());
                SpeedRunIGT.TIMER_DRAWER.setRTA_YPos(drawer.getRTA_YPos());
                SpeedRunOptions.setOption(SpeedRunOptions.TIMER_RTA_SCALE, drawer.getRTAScale());
                SpeedRunIGT.TIMER_DRAWER.setRTAScale(drawer.getRTAScale());
                SpeedRunOptions.setOption(SpeedRunOptions.TIMER_RTA_COLOR, drawer.getRTAColor());
                SpeedRunIGT.TIMER_DRAWER.setRTAColor(drawer.getRTAColor());
                SpeedRunOptions.setOption(SpeedRunOptions.TIMER_RTA_DECO, drawer.getRTADecoration());
                SpeedRunIGT.TIMER_DRAWER.setRTADecoration(drawer.getRTADecoration());

                SpeedRunOptions.setOption(SpeedRunOptions.DISPLAY_TIME_ONLY, drawer.isSimplyTimer());
                SpeedRunIGT.TIMER_DRAWER.setSimplyTimer(drawer.isSimplyTimer());
                SpeedRunOptions.setOption(SpeedRunOptions.LOCK_TIMER_POSITION, drawer.isLocked());
                SpeedRunIGT.TIMER_DRAWER.setLocked(drawer.isLocked());
                SpeedRunOptions.setOption(SpeedRunOptions.DISPLAY_DECIMALS, drawer.getTimerDecimals());
                SpeedRunIGT.TIMER_DRAWER.setTimerDecimals(drawer.getTimerDecimals());

                SpeedRunOptions.setOption(SpeedRunOptions.TIMER_TEXT_FONT, drawer.getTimerFont());
                SpeedRunIGT.TIMER_DRAWER.setTimerFont(drawer.getTimerFont());

                changed = false;
        }
        else if(button.id==5007){
            if (client != null) client.openScreen(parent);
        }
        else if(button.id==5090){
                drawer.setSimplyTimer(!drawer.isSimplyTimer());
                changed = true;
                button.message=(SpeedRunIGT.translate("speedrunigt.option.timer_position.show_time_only","Disable Timer Labels").append(" : ").append(drawer.isSimplyTimer() ? ScreenTexts.ON : ScreenTexts.OFF)).getString();
        }
        else if(button.id==5008){
                drawer.setLocked(!drawer.isLocked());
                changed = true;
                button.message=(SpeedRunIGT.translate("speedrunigt.option.timer_position.lock_timer_position","Lock Timer Position").append(" : ").append(drawer.isLocked() ? ScreenTexts.ON : ScreenTexts.OFF)).getString();
        }
        else if(button.id==5009){
                int order = drawer.getTimerDecimals().ordinal();
                drawer.setTimerDecimals(TimerDecimals.values()[(++order) % TimerDecimals.values().length]);
                changed = true;
                button.message=(SpeedRunIGT.translate("speedrunigt.option.timer_position.show_decimals","Timer Precision").append(" : ").append(String.valueOf(drawer.getTimerDecimals().getNumber()))).getString();
        }



        else if(button.id==5014){
                if (!igtButton.active) {
                    int order = drawer.getIGTDecoration().ordinal();
                    drawer.setIGTDecoration(TimerDecoration.values()[(++order) % TimerDecoration.values().length]);
                    changed = true;
                    button.message=SpeedRunIGT.translate("speedrunigt.option.timer_position.igt.text_decorate", "IGT Text Style").append(" : ").append(SpeedRunIGT.translate("speedrunigt.option.timer_position.text_decorate." + drawer.getIGTDecoration().name().toLowerCase(Locale.ROOT),drawer.getIGTDecoration().name().toLowerCase(Locale.ROOT))).getString();
                } else if (!rtaButton.active) {
                    int order = drawer.getRTADecoration().ordinal();
                    drawer.setRTADecoration(TimerDecoration.values()[(++order) % TimerDecoration.values().length]);
                    changed = true;
                    button.message=SpeedRunIGT.translate("speedrunigt.option.timer_position.rta.text_decorate", "RTA Text Style").append(" : ").append(SpeedRunIGT.translate("speedrunigt.option.timer_position.text_decorate." + drawer.getIGTDecoration().name().toLowerCase(Locale.ROOT),drawer.getRTADecoration().name().toLowerCase(Locale.ROOT))).getString();
                }
        }


        else if(button.id==5019) {
            int order = drawer.getRTADecoration().ordinal();
            drawer.setRTADecoration(TimerDecoration.values()[(++order) % TimerDecoration.values().length]);
            changed = true;
            button.message = SpeedRunIGT.translate("speedrunigt.option.timer_position.rta,text_decorate", "RTA Text Style").append(" : ").append(SpeedRunIGT.translate("speedrunigt.option.timer_position.text_decorate." + drawer.getRTADecoration().name().toLowerCase(Locale.ROOT), drawer.getRTADecoration().name().toLowerCase(Locale.ROOT))).getString();
        }

    }
    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {

        boolean isButtonClick = false;
        for (ButtonWidget widget : this.buttons) {
            if (widget.isMouseOver(this.client, mouseX, mouseY)) {
                isButtonClick = true;
            }
        }
        if (button == 0 && !drawer.isLocked()&&!isButtonClick) {
            if (!this.igtButton.active) {
                drawer.setIGT_XPos(MathHelper.clamp((((float) (mouseX))/ client.width ), 0, 1));
                drawer.setIGT_YPos(MathHelper.clamp((((float) (mouseY))/client.height ), 0, 1));
                changed = true;
            }
            if (!this.rtaButton.active) {
                drawer.setRTA_XPos(MathHelper.clamp((((float) (mouseX))/client.width ), 0, 1));
                drawer.setRTA_YPos(MathHelper.clamp((((float) (mouseY))/client.height  ), 0, 1));
                changed = true;
            }
        }
        super.mouseClicked(mouseX,mouseY,button);
    }

    @Override
    protected void keyPressed(char character, int keyCode) {
        if ( keyCode >= 262 && keyCode <= 265 && client != null && !drawer.isLocked()) {
            int moveX = keyCode == 262 ? 1 : keyCode == 263 ? -1 : 0;
            int moveY = keyCode == 265 ? -1 : keyCode == 264 ? 1 : 0;
            if (!igtButton.active) {
                drawer.setIGT_XPos(MathHelper.clamp(drawer.getIGT_XPos() + moveX * drawer.getIGTScale() / client.width, 0, 1));
                drawer.setIGT_YPos(MathHelper.clamp(drawer.getIGT_YPos() + moveY * drawer.getIGTScale() / client.height, 0, 1));
                changed = true;
            }
            if (!rtaButton.active) {
                drawer.setRTA_XPos(MathHelper.clamp(drawer.getRTA_XPos() + moveX * drawer.getRTAScale() / client.width, 0, 1));
                drawer.setRTA_YPos(MathHelper.clamp(drawer.getRTA_YPos() + moveY * drawer.getRTAScale() / client.height, 0, 1));
                changed = true;
            }
        }
        super.keyPressed(character,keyCode);
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        saveButton.active = changed;

        this.renderBackground();

        drawer.draw();

        this.drawCenteredString(this.textRenderer, this.title, this.width / 2, 15, 16777215);

        if (!hide) {
            if (!igtButton.active || !rtaButton.active) {
                if (drawer.isLocked()) {
                    drawCenteredString( this.textRenderer,
                            SpeedRunIGT.translate("speedrunigt.option.timer_position.description.lock","§cA Timer position is locked!").getString(), this.width / 2, this.height / 2 - 80, 16777215);
                } else {
                    drawCenteredString( this.textRenderer,
                            SpeedRunIGT.translate("speedrunigt.option.timer_position.description","§eClick on a position to change the timer position.").getString(), this.width / 2, this.height / 2 - 80, 16777215);
                    //drawCenteredString( this.textRenderer,
                    //        SpeedRunIGT.translate("speedrunigt.option.timer_position.description.move","You can finely adjust the position by pressing §eCtrl + arrow key.").getString(), this.width / 2, this.height / 2 - 69, 16777215);
                }
            }


        }
        super.render( mouseX, mouseY, delta);
    }



    public void initNormal() {
        ButtonWidget b1= new ButtonWidget(5090,width / 2 - 80, height / 2 - 16, 160, 20, SpeedRunIGT.translate("speedrunigt.option.timer_position.show_time_only","Disable Timer Labels").append(" : ").append(drawer.isSimplyTimer() ? ScreenTexts.ON : ScreenTexts.OFF).getString());
        normalOptions.add(b1);
        buttons.add(b1);
        ButtonWidget b2=new ButtonWidget(5008,width / 2 - 80, height / 2 + 6, 160, 20, SpeedRunIGT.translate("speedrunigt.option.timer_position.lock_timer_position","Lock Timer Position").append(" : ").append(drawer.isLocked() ? ScreenTexts.ON : ScreenTexts.OFF).getString());
        normalOptions.add(b2);
        buttons.add(b2);
        ButtonWidget b3=new ButtonWidget(5009,width / 2 - 80, height / 2 + 28, 160, 20, SpeedRunIGT.translate("speedrunigt.option.timer_position.show_decimals","Timer Precision").append(" : ").append(String.valueOf(drawer.getTimerDecimals().getNumber())).getString());
        normalOptions.add(b3);
        buttons.add(b3);
    }

    public void initIGTButtons() {
        ButtonWidget b =new SliderWidget(new PagedEntryListWidget.Listener() {
            @Override
            public void setBooleanValue(int id, boolean value) {

            }

            @Override
            public void setFloatValue(int id, float value) {
                int color = drawer.getIGTColor();
                drawer.setIGTColor(
                        ColorMixer.getArgb(
                                ColorMixer.getAlpha(color),
                                (int) (value ),
                                ColorMixer.getGreen(color),
                                ColorMixer.getBlue(color)
                        )
                );
                changed = true;
            }

            @Override
            public void setStringValue(int id, String text) {

            }
        }, 5010, width / 2 - 147, height / 2 - 16, SpeedRunIGT.translate("speedrunigt.option.timer_position.igt.color_red", "IGT Red").append(" : ").append(String.valueOf(ColorMixer.getRed(drawer.getIGTColor()))).getString(), 0F, 255F, ColorMixer.getRed(drawer.getIGTColor()),
                (i, string, sliderValue) -> SpeedRunIGT.translate("speedrunigt.option.igt.timer_position.color_red", "IGT Red").append(" : ").append(String.valueOf(ColorMixer.getRed(drawer.getIGTColor()))).getString());
        igtOptions.add(b);
        buttons.add(b);

        ButtonWidget b2 =new SliderWidget(new PagedEntryListWidget.Listener() {
            @Override
            public void setBooleanValue(int id, boolean value) {

            }

            @Override
            public void setFloatValue(int id, float value) {
                int color = drawer.getIGTColor();
                drawer.setIGTColor(
                        ColorMixer.getArgb(
                                ColorMixer.getAlpha(color),
                                ColorMixer.getRed(color),
                                (int) (value),
                                ColorMixer.getBlue(color)
                        )
                );
                changed = true;
            }

            @Override
            public void setStringValue(int id, String text) {

            }
        },5011,width / 2 - 147, height / 2 +6,  SpeedRunIGT.translate("speedrunigt.option.timer_position.igt.color_green", "IGT Green").append(" : ").append(String.valueOf(ColorMixer.getGreen(drawer.getIGTColor()))).getString(),0F, 255F, ColorMixer.getGreen(drawer.getIGTColor()),
                (i, string, sliderValue) -> SpeedRunIGT.translate("speedrunigt.option.timer_position.igt.color_green", "IGT Green").append(" : ").append(String.valueOf(ColorMixer.getGreen(drawer.getIGTColor()))).getString());
        igtOptions.add(b2);
        buttons.add(b2);

        ButtonWidget b3 =new SliderWidget(new PagedEntryListWidget.Listener() {
            @Override
            public void setBooleanValue(int id, boolean value) {

            }

            @Override
            public void setFloatValue(int id, float value) {
                int color = drawer.getIGTColor();
                drawer.setIGTColor(
                        ColorMixer.getArgb(
                                ColorMixer.getAlpha(color),
                                ColorMixer.getRed(color),
                                ColorMixer.getGreen(color),
                                (int) (value)
                        )
                );
                changed = true;
            }

            @Override
            public void setStringValue(int id, String text) {

            }
        },5012,width / 2 - 147, height / 2 +28,SpeedRunIGT.translate("speedrunigt.option.timer_position.igt.color_blue", "IGT Blue").append(" : ").append(String.valueOf(ColorMixer.getBlue(drawer.getIGTColor()))).getString(),0F, 255F, ColorMixer.getBlue(drawer.getIGTColor()),
                (i, string, sliderValue) -> SpeedRunIGT.translate("speedrunigt.option.timer_position.igt.color_blue", "IGT Blue").append(" : ").append(String.valueOf(ColorMixer.getBlue(drawer.getIGTColor()))).getString());
        igtOptions.add(b3);
        buttons.add(b3);


        ButtonWidget b4 =new SliderWidget(new PagedEntryListWidget.Listener() {
            @Override
            public void setBooleanValue(int id, boolean value) {

            }

            @Override
            public void setFloatValue(int id, float value) {
                drawer.setIGTScale(Math.round((value/100) * 3f * 20f)/20f);
                changed = true;
            }

            @Override
            public void setStringValue(int id, String text) {

            }
        },5013,width / 2 + 6, height / 2 - 16, SpeedRunIGT.translate("speedrunigt.option.timer_position.igt.scale","IGT Timer Scale").append(" : ").append(((int) (drawer.getIGTScale() * 100)) + "%").getString(),0F, 300F, drawer.getIGTScale() * 100F,
                (i, string, sliderValue) -> SpeedRunIGT.translate("speedrunigt.option.timer_position.igt.scale", "IGT Timer Scale").append(" : ").append(((int) (drawer.getIGTScale() * 100)) + "%").getString());
        igtOptions.add(b4);
        buttons.add(b4);
        ButtonWidget b5 =new ButtonWidget(5014,width / 2 + 6, height / 2 + 6, 150, 20, SpeedRunIGT.translate("speedrunigt.option.timer_position.igt.text_decorate", "IGT Text Style").append(" : ").append(SpeedRunIGT.translate("speedrunigt.option.timer_position.text_decorate." + drawer.getIGTDecoration().name().toLowerCase(Locale.ROOT),drawer.getIGTDecoration().name().toLowerCase(Locale.ROOT))).getString());

        igtOptions.add(b5);
        buttons.add(b5);
    }

    public void initRTAButtons() {
        ButtonWidget b =new SliderWidget(new PagedEntryListWidget.Listener() {
            @Override
            public void setBooleanValue(int id, boolean value) {

            }

            @Override
            public void setFloatValue(int id, float value) {
                int color = drawer.getRTAColor();
                drawer.setRTAColor(
                        ColorMixer.getArgb(
                                ColorMixer.getAlpha(color),
                                (int) (value ),
                                ColorMixer.getGreen(color),
                                ColorMixer.getBlue(color)
                        )
                );
                changed = true;
            }

            @Override
            public void setStringValue(int id, String text) {

            }
        }, 5010, width / 2 - 147, height / 2 - 16, SpeedRunIGT.translate("speedrunigt.option.timer_position.rta.color_red", "RTA Red").append(" : ").append(String.valueOf(ColorMixer.getRed(drawer.getRTAColor()))).getString(), 0F, 255F, ColorMixer.getRed(drawer.getRTAColor()),
                (i, string, sliderValue) -> SpeedRunIGT.translate("speedrunigt.option.rta.timer_position.color_red", "RTA Red").append(" : ").append(String.valueOf(ColorMixer.getRed(drawer.getRTAColor()))).getString());
        rtaOptions.add(b);
        buttons.add(b);

        ButtonWidget b2 =new SliderWidget(new PagedEntryListWidget.Listener() {
            @Override
            public void setBooleanValue(int id, boolean value) {

            }

            @Override
            public void setFloatValue(int id, float value) {
                int color = drawer.getRTAColor();
                drawer.setRTAColor(
                        ColorMixer.getArgb(
                                ColorMixer.getAlpha(color),
                                ColorMixer.getRed(color),
                                (int) (value),
                                ColorMixer.getBlue(color)
                        )
                );
                changed = true;
            }

            @Override
            public void setStringValue(int id, String text) {

            }
        },5011,width / 2 - 147, height / 2 +6,  SpeedRunIGT.translate("speedrunigt.option.timer_position.rta.color_green", "RTA Green").append(" : ").append(String.valueOf(ColorMixer.getGreen(drawer.getRTAColor()))).getString(),0F, 255F, ColorMixer.getGreen(drawer.getRTAColor()),
                (i, string, sliderValue) -> SpeedRunIGT.translate("speedrunigt.option.timer_position.rta.color_green", "RTA Green").append(" : ").append(String.valueOf(ColorMixer.getGreen(drawer.getRTAColor()))).getString());
        rtaOptions.add(b2);
        buttons.add(b2);

        ButtonWidget b3 =new SliderWidget(new PagedEntryListWidget.Listener() {
            @Override
            public void setBooleanValue(int id, boolean value) {

            }

            @Override
            public void setFloatValue(int id, float value) {
                int color = drawer.getRTAColor();
                drawer.setRTAColor(
                        ColorMixer.getArgb(
                                ColorMixer.getAlpha(color),
                                ColorMixer.getRed(color),
                                ColorMixer.getGreen(color),
                                (int) (value)
                        )
                );
                changed = true;
            }

            @Override
            public void setStringValue(int id, String text) {

            }
        },5012,width / 2 - 147, height / 2 +28,SpeedRunIGT.translate("speedrunigt.option.timer_position.rta.color_blue", "RTA Blue").append(" : ").append(String.valueOf(ColorMixer.getBlue(drawer.getRTAColor()))).getString(),0F, 255F, ColorMixer.getBlue(drawer.getRTAColor()),
                (i, string, sliderValue) -> SpeedRunIGT.translate("speedrunigt.option.timer_position.rta.color_blue", "RTA Blue").append(" : ").append(String.valueOf(ColorMixer.getBlue(drawer.getRTAColor()))).getString());
        rtaOptions.add(b3);
        buttons.add(b3);


        ButtonWidget b4 =new SliderWidget(new PagedEntryListWidget.Listener() {
            @Override
            public void setBooleanValue(int id, boolean value) {

            }

            @Override
            public void setFloatValue(int id, float value) {
                drawer.setRTAScale(Math.round((value/100) * 3f * 20f)/20f);
                changed = true;
            }

            @Override
            public void setStringValue(int id, String text) {

            }
        },5013,width / 2 + 6, height / 2 - 16, SpeedRunIGT.translate("speedrunigt.option.timer_position.rta.scale","RTA Timer Scale").append(" : ").append(((int) (drawer.getRTAScale() * 100)) + "%").getString(),0F, 300F, drawer.getRTAScale() * 100F,
                (i, string, sliderValue) -> SpeedRunIGT.translate("speedrunigt.option.timer_position.rta.scale", "RTA Timer Scale").append(" : ").append(((int) (drawer.getRTAScale() * 100)) + "%").getString());
        rtaOptions.add(b4);
        buttons.add(b4);
        ButtonWidget b5 =new ButtonWidget(5014,width / 2 + 6, height / 2 + 6, 150, 20, SpeedRunIGT.translate("speedrunigt.option.timer_position.rta.text_decorate", "RTA Text Style").append(" : ").append(SpeedRunIGT.translate("speedrunigt.option.timer_position.text_decorate." + drawer.getIGTDecoration().name().toLowerCase(Locale.ROOT),drawer.getRTADecoration().name().toLowerCase(Locale.ROOT))).getString());

       rtaOptions.add(b5);
        buttons.add(b5);
    }




}

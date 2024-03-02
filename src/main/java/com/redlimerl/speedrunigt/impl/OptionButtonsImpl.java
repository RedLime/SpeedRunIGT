package com.redlimerl.speedrunigt.impl;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.api.OptionButtonFactory;
import com.redlimerl.speedrunigt.api.SpeedRunIGTApi;
import com.redlimerl.speedrunigt.gui.screen.SpeedRunCategoryScreen;
import com.redlimerl.speedrunigt.gui.screen.SpeedRunIGTInfoScreen;
import com.redlimerl.speedrunigt.gui.screen.TimerCustomizeScreen;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.utils.ButtonWidgetHelper;
import com.redlimerl.speedrunigt.utils.TranslateHelper;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.function.Supplier;

import static com.redlimerl.speedrunigt.SpeedRunIGTClient.TIMER_DRAWER;

public class OptionButtonsImpl implements SpeedRunIGTApi {

    @Override
    public Collection<OptionButtonFactory> createOptionButtons() {
        ArrayList<OptionButtonFactory> factories = new ArrayList<>();

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Text.literal("SpeedRunIGT Discord"),
                                (ButtonWidget button) -> Util.getOperatingSystem().open("https://discord.gg/7G2tfP7Xpe"))
                )
                .setCategory("speedrunigt.option.category.general")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Text.translatable("speedrunigt.option.timer_position"),
                                (ButtonWidget button) -> MinecraftClient.getInstance().setScreen(new TimerCustomizeScreen(screen)))
                )
                .setCategory("speedrunigt.option.category.general")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Text.translatable("speedrunigt.option.timer_category"),
                                (ButtonWidget button) -> MinecraftClient.getInstance().setScreen(new SpeedRunCategoryScreen(screen)))
                )
                .setCategory("speedrunigt.option.category.general")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Text.translatable("speedrunigt.option.check_info"),
                                (ButtonWidget button) -> MinecraftClient.getInstance().setScreen(new SpeedRunIGTInfoScreen(screen)))
                )
                .setCategory("speedrunigt.option.category.general")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Text.translatable("speedrunigt.option.reload"),
                                (ButtonWidget button) -> MinecraftClient.getInstance().setScreen(new ConfirmScreen(boolean1 -> {
                                    if (boolean1) {
                                        SpeedRunOption.reload();
                                    }
                                    MinecraftClient.getInstance().setScreen(screen);
                                }, Text.translatable("speedrunigt.message.reload_options"), Text.empty())))
                )
                .setCategory("speedrunigt.option.category.general")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Text.translatable("speedrunigt.option.global_options").append(" : ").append(SpeedRunOption.isUsingGlobalConfig() ? ScreenTexts.ON : ScreenTexts.OFF),
                                (ButtonWidget button) -> {
                                    SpeedRunOption.setUseGlobalConfig(!SpeedRunOption.isUsingGlobalConfig());
                                    MinecraftClient.getInstance().setScreen(new ConfirmScreen(boolean1 -> {
                                        if (boolean1) {
                                            SpeedRunOption.reload();
                                        }
                                        MinecraftClient.getInstance().setScreen(screen);
                                    }, Text.translatable("speedrunigt.message.reload_options"), Text.empty()));
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.global_options.description", SpeedRunOption.getConfigPath()))
                .setCategory("speedrunigt.option.category.general")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Text.translatable("speedrunigt.option.safe_font_mode").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.CUSTOM_FONT_SAFE_MODE) ? ScreenTexts.ON : ScreenTexts.OFF),
                                (ButtonWidget button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.CUSTOM_FONT_SAFE_MODE, !SpeedRunOption.getOption(SpeedRunOptions.CUSTOM_FONT_SAFE_MODE));
                                    button.setMessage(Text.translatable("speedrunigt.option.safe_font_mode").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.CUSTOM_FONT_SAFE_MODE) ? ScreenTexts.ON : ScreenTexts.OFF));
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.safe_font_mode.description"))
                .setCategory("speedrunigt.option.category.general")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Text.translatable("speedrunigt.option.always_english_translations").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.ALWAYS_ENGLISH_TRANSLATIONS) ? ScreenTexts.ON : ScreenTexts.OFF),
                                (ButtonWidget button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.ALWAYS_ENGLISH_TRANSLATIONS, !SpeedRunOption.getOption(SpeedRunOptions.ALWAYS_ENGLISH_TRANSLATIONS));
                                    button.setMessage(Text.translatable("speedrunigt.option.always_english_translations").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.ALWAYS_ENGLISH_TRANSLATIONS) ? ScreenTexts.ON : ScreenTexts.OFF));

                                    TranslateHelper.reload();
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.always_english_translations.description"))
                .setCategory("speedrunigt.option.category.general")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Text.translatable("speedrunigt.option.timer_position.toggle_timer").append(" : ").append(TIMER_DRAWER.isToggle() ? ScreenTexts.ON : ScreenTexts.OFF),
                                (ButtonWidget button) -> {
                                    TIMER_DRAWER.setToggle(!TIMER_DRAWER.isToggle());
                                    SpeedRunOption.setOption(SpeedRunOptions.TOGGLE_TIMER, TIMER_DRAWER.isToggle());
                                    button.setMessage(Text.translatable("speedrunigt.option.timer_position.toggle_timer").append(" : ").append(TIMER_DRAWER.isToggle() ? ScreenTexts.ON : ScreenTexts.OFF));
                                })
                )
                .setCategory("speedrunigt.option.category.timer")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Text.translatable("speedrunigt.option.hide_timer_in_options").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.HIDE_TIMER_IN_OPTIONS) ? ScreenTexts.ON : ScreenTexts.OFF),
                                (ButtonWidget button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.HIDE_TIMER_IN_OPTIONS, !SpeedRunOption.getOption(SpeedRunOptions.HIDE_TIMER_IN_OPTIONS));
                                    button.setMessage(Text.translatable("speedrunigt.option.hide_timer_in_options").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.HIDE_TIMER_IN_OPTIONS) ? ScreenTexts.ON : ScreenTexts.OFF));
                                })
                )
                .setCategory("speedrunigt.option.category.timer")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Text.translatable("speedrunigt.option.hide_timer_in_debugs").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.HIDE_TIMER_IN_DEBUGS) ? ScreenTexts.ON : ScreenTexts.OFF),
                                (ButtonWidget button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.HIDE_TIMER_IN_DEBUGS, !SpeedRunOption.getOption(SpeedRunOptions.HIDE_TIMER_IN_DEBUGS));
                                    button.setMessage(Text.translatable("speedrunigt.option.hide_timer_in_debugs").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.HIDE_TIMER_IN_DEBUGS) ? ScreenTexts.ON : ScreenTexts.OFF));
                                })
                )
                .setCategory("speedrunigt.option.category.timer")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Text.translatable("speedrunigt.option.timer_start_type").append(" : ").append(Text.translatable("speedrunigt.option.timer_start_type." + SpeedRunOption.getOption(SpeedRunOptions.WAITING_FIRST_INPUT).name().toLowerCase(Locale.ROOT))),
                                (ButtonWidget button) -> {
                                    int order = SpeedRunOption.getOption(SpeedRunOptions.WAITING_FIRST_INPUT).ordinal() + 1;
                                    SpeedRunOptions.TimerStartType[] intervals = SpeedRunOptions.TimerStartType.values();
                                    SpeedRunOption.setOption(SpeedRunOptions.WAITING_FIRST_INPUT, intervals[order % intervals.length]);
                                    button.setMessage(Text.translatable("speedrunigt.option.timer_start_type").append(" : ").append(Text.translatable("speedrunigt.option.timer_start_type." + SpeedRunOption.getOption(SpeedRunOptions.WAITING_FIRST_INPUT).name().toLowerCase(Locale.ROOT))));
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.timer_start_type.description"))
                .setCategory("speedrunigt.option.category.timing")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Text.translatable("speedrunigt.option.auto_toggle_coop").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.AUTOMATIC_COOP_MODE) ? ScreenTexts.ON : ScreenTexts.OFF),
                                (ButtonWidget button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.AUTOMATIC_COOP_MODE, !SpeedRunOption.getOption(SpeedRunOptions.AUTOMATIC_COOP_MODE));
                                    button.setMessage(Text.translatable("speedrunigt.option.auto_toggle_coop").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.AUTOMATIC_COOP_MODE) ? ScreenTexts.ON : ScreenTexts.OFF));
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.auto_toggle_coop.description"))
                .setCategory("speedrunigt.option.category.timer")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Text.translatable("speedrunigt.option.start_old_worlds").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.TIMER_START_GENERATED_WORLD) ? ScreenTexts.ON : ScreenTexts.OFF),
                                (ButtonWidget button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.TIMER_START_GENERATED_WORLD, !SpeedRunOption.getOption(SpeedRunOptions.TIMER_START_GENERATED_WORLD));
                                    button.setMessage(Text.translatable("speedrunigt.option.start_old_worlds").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.TIMER_START_GENERATED_WORLD) ? ScreenTexts.ON : ScreenTexts.OFF));
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.start_old_worlds.description"))
                .setCategory("speedrunigt.option.category.timing")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Text.translatable("speedrunigt.option.limitless_reset").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.TIMER_LIMITLESS_RESET) ? ScreenTexts.ON : ScreenTexts.OFF),
                                (ButtonWidget button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.TIMER_LIMITLESS_RESET, !SpeedRunOption.getOption(SpeedRunOptions.TIMER_LIMITLESS_RESET));
                                    button.setMessage(Text.translatable("speedrunigt.option.limitless_reset").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.TIMER_LIMITLESS_RESET) ? ScreenTexts.ON : ScreenTexts.OFF));
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.limitless_reset.description"))
                .setCategory("speedrunigt.option.category.timing")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Text.translatable("speedrunigt.option.current_extensions"),
                                (ButtonWidget button) -> {})
                )
                .setToolTip(() -> {
                    StringBuilder extension = new StringBuilder(I18n.translate("speedrunigt.option.current_extensions.description", SpeedRunIGTApi.getProviders().length));
                    extension.append("\n");
                    int auto = 0;
                    for (ModContainer provider : SpeedRunIGTApi.getProviders()) {
                        if (auto++ > 4) {
                            auto = 0;
                            extension.append("\n");
                        }
                        extension.append(String.format("%s v%s,", provider.getMetadata().getName(), provider.getMetadata().getVersion()));
                    }
                    return extension.substring(0, extension.length() - 1);
                })
                .setCategory("speedrunigt.option.category.general")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Text.translatable("speedrunigt.option.legacy_igt_mode").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.TIMER_LEGACY_IGT_MODE) ? ScreenTexts.ON : ScreenTexts.OFF),
                                (ButtonWidget button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.TIMER_LEGACY_IGT_MODE, !SpeedRunOption.getOption(SpeedRunOptions.TIMER_LEGACY_IGT_MODE));
                                    button.setMessage(Text.translatable("speedrunigt.option.legacy_igt_mode").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.TIMER_LEGACY_IGT_MODE) ? ScreenTexts.ON : ScreenTexts.OFF));
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.legacy_igt_mode.description"))
                .setCategory("speedrunigt.option.category.timer")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Text.translatable("speedrunigt.option.auto_save_interval").append(" : ").append(Text.translatable("speedrunigt.option.auto_save_interval." + SpeedRunOption.getOption(SpeedRunOptions.TIMER_DATA_AUTO_SAVE).name().toLowerCase(Locale.ROOT))),
                                (ButtonWidget button) -> {
                                    int order = SpeedRunOption.getOption(SpeedRunOptions.TIMER_DATA_AUTO_SAVE).ordinal() + 1;
                                    SpeedRunOptions.TimerSaveInterval[] intervals = SpeedRunOptions.TimerSaveInterval.values();
                                    SpeedRunOption.setOption(SpeedRunOptions.TIMER_DATA_AUTO_SAVE, intervals[order % intervals.length]);
                                    button.setMessage(Text.translatable("speedrunigt.option.auto_save_interval").append(" : ").append(Text.translatable("speedrunigt.option.auto_save_interval." + SpeedRunOption.getOption(SpeedRunOptions.TIMER_DATA_AUTO_SAVE).name().toLowerCase(Locale.ROOT))));
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.auto_save_interval.description"))
                .setCategory("speedrunigt.option.category.timer")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Text.translatable("speedrunigt.option.practice_detect").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.ENABLE_PRACTICE_DETECT) ? ScreenTexts.ON : ScreenTexts.OFF),
                                (ButtonWidget button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.ENABLE_PRACTICE_DETECT, !SpeedRunOption.getOption(SpeedRunOptions.ENABLE_PRACTICE_DETECT));
                                    button.setMessage(Text.translatable("speedrunigt.option.practice_detect").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.ENABLE_PRACTICE_DETECT) ? ScreenTexts.ON : ScreenTexts.OFF));
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.practice_detect.description"))
                .setCategory("speedrunigt.option.category.practice")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Text.translatable("speedrunigt.option.teleport_to_practice").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.TELEPORT_TO_END_PRACTICE) ? ScreenTexts.ON : ScreenTexts.OFF),
                                (ButtonWidget button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.TELEPORT_TO_END_PRACTICE, !SpeedRunOption.getOption(SpeedRunOptions.TELEPORT_TO_END_PRACTICE));
                                    button.setMessage(Text.translatable("speedrunigt.option.teleport_to_practice").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.TELEPORT_TO_END_PRACTICE) ? ScreenTexts.ON : ScreenTexts.OFF));
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.teleport_to_practice.description"))
                .setCategory("speedrunigt.option.category.practice")
        );

        ButtonWidget alwaysAutoRetimeButton = ButtonWidgetHelper.create(0, 0, 150, 20, Text.translatable("speedrunigt.option.always_use_auto_retime").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.ALWAYS_USE_AUTO_RETIME) ? ScreenTexts.ON : ScreenTexts.OFF),
                (ButtonWidget button) -> {
                    SpeedRunOption.setOption(SpeedRunOptions.ALWAYS_USE_AUTO_RETIME, !SpeedRunOption.getOption(SpeedRunOptions.ALWAYS_USE_AUTO_RETIME));
                    button.setMessage(Text.translatable("speedrunigt.option.always_use_auto_retime").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.ALWAYS_USE_AUTO_RETIME) ? ScreenTexts.ON : ScreenTexts.OFF));
                });

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Text.translatable("speedrunigt.option.auto_retime").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.AUTO_RETIME_FOR_GUIDELINE) ? ScreenTexts.ON : ScreenTexts.OFF),
                                (ButtonWidget button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.AUTO_RETIME_FOR_GUIDELINE, !SpeedRunOption.getOption(SpeedRunOptions.AUTO_RETIME_FOR_GUIDELINE));
                                    button.setMessage(Text.translatable("speedrunigt.option.auto_retime").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.AUTO_RETIME_FOR_GUIDELINE) ? ScreenTexts.ON : ScreenTexts.OFF));
                                    alwaysAutoRetimeButton.active = SpeedRunOption.getOption(SpeedRunOptions.AUTO_RETIME_FOR_GUIDELINE);
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.auto_retime.description"))
                .setCategory("speedrunigt.option.category.retime")
        );

        factories.add(screen -> {
            alwaysAutoRetimeButton.active = SpeedRunOption.getOption(SpeedRunOptions.AUTO_RETIME_FOR_GUIDELINE);
            return new OptionButtonFactory.Builder()
                            .setButtonWidget(alwaysAutoRetimeButton)
                            .setToolTip(() -> I18n.translate("speedrunigt.option.always_use_auto_retime.description"))
                            .setCategory("speedrunigt.option.category.retime");
            }
        );

        factories.add(screen -> {
            Supplier<Text> makeText = () -> {
                int value = SpeedRunOption.getOption(SpeedRunOptions.CHANGE_ANY_TO_AA_OVER);
                return Text.translatable("speedrunigt.option.auto_toggle_aa").append(" : ")
                        .append(value > 0 ? Text.literal(value+"+") : ScreenTexts.OFF);
            };
            return new OptionButtonFactory.Builder()
                            .setButtonWidget(
                                    new SliderWidget(0, 0, 150, 20, makeText.get(), MathHelper.clamp(SpeedRunOption.getOption(SpeedRunOptions.CHANGE_ANY_TO_AA_OVER) / 50.0, 0.0, 1.0)) {
                                        @Override
                                        protected void updateMessage() {
                                            this.setMessage(makeText.get());
                                        }

                                        @Override
                                        protected void applyValue() {
                                            SpeedRunOption.setOption(SpeedRunOptions.CHANGE_ANY_TO_AA_OVER, (int) Math.round(this.value * 50));
                                        }
                                    }
                            )
                            .setToolTip(() -> I18n.translate("speedrunigt.option.auto_toggle_aa.description"))
                            .setCategory("speedrunigt.option.category.timer");
        });

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Text.translatable("speedrunigt.option.generate_record").append(" : ").append(Text.translatable("speedrunigt.option.generate_record." + SpeedRunOption.getOption(SpeedRunOptions.GENERATE_RECORD_FILE).name().toLowerCase(Locale.ROOT))),
                                (ButtonWidget button) -> {
                                    int order = SpeedRunOption.getOption(SpeedRunOptions.GENERATE_RECORD_FILE).ordinal() + 1;
                                    SpeedRunOptions.RecordGenerateType[] intervals = SpeedRunOptions.RecordGenerateType.values();
                                    SpeedRunOption.setOption(SpeedRunOptions.GENERATE_RECORD_FILE, intervals[order % intervals.length]);
                                    button.setMessage(Text.translatable("speedrunigt.option.generate_record").append(" : ").append(Text.translatable("speedrunigt.option.generate_record." + SpeedRunOption.getOption(SpeedRunOptions.GENERATE_RECORD_FILE).name().toLowerCase(Locale.ROOT))));
                                })
                )
                .setCategory("speedrunigt.option.category.records")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Text.translatable("speedrunigt.option.open_records_folder"),
                                (ButtonWidget button) -> Util.getOperatingSystem().open(SpeedRunIGT.getRecordsPath().toFile()))
                )
                .setCategory("speedrunigt.option.category.records")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Text.translatable("speedrunigt.option.delete_all_records"),
                                (ButtonWidget button) -> MinecraftClient.getInstance().setScreen(new ConfirmScreen(boolean1 -> {
                                    if (boolean1) {
                                        try {
                                            FileUtils.deleteDirectory(SpeedRunIGT.getRecordsPath().toFile());
                                            if (!SpeedRunIGT.getRecordsPath().toFile().mkdir()) {
                                                SpeedRunIGT.error("Failed to make records directory");
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    MinecraftClient.getInstance().setScreen(screen);
                                }, Text.translatable("speedrunigt.option.delete_all_records.description"), Text.empty())))
                )
                .setCategory("speedrunigt.option.category.records")
        );

        factories.add(screen -> {
            if (InGameTimer.getInstance().isStopped()) {
                ButtonWidget buttonWidget = ButtonWidgetHelper.create(0, 0, 150, 20, Text.translatable("speedrunigt.option.generate_timer_logs"), (ButtonWidget button) -> {});
                buttonWidget.active = false;
                return new OptionButtonFactory.Builder()
                        .setButtonWidget(
                                buttonWidget
                        )
                        .setToolTip(() -> I18n.translate("speedrunigt.option.generate_timer_logs.description"))
                        .setCategory("speedrunigt.option.category.records");
            }
            return new OptionButtonFactory.Builder()
                            .setButtonWidget(
                                    ButtonWidgetHelper.create(0, 0, 150, 20, Text.translatable("speedrunigt.option.generate_timer_logs"),
                                            (ButtonWidget button) ->
                                                    MinecraftClient.getInstance().setScreen(new ConfirmScreen(boolean1 -> {
                                                        if (boolean1) {
                                                            try {
                                                                InGameTimer.writeTimerLogs(InGameTimer.getInstance());
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                        MinecraftClient.getInstance().setScreen(screen);
                                                    }, Text.translatable("speedrunigt.option.generate_timer_logs.message"), Text.empty()))
                                    )
                            )
                            .setToolTip(() -> I18n.translate("speedrunigt.option.generate_timer_logs.description"))
                            .setCategory("speedrunigt.option.category.records");
            }
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Text.translatable("speedrunigt.option.auto_save_player_data").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.AUTO_SAVE_PLAYER_DATA) ? ScreenTexts.ON : ScreenTexts.OFF),
                                (ButtonWidget button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.AUTO_SAVE_PLAYER_DATA, !SpeedRunOption.getOption(SpeedRunOptions.AUTO_SAVE_PLAYER_DATA));
                                    button.setMessage(Text.translatable("speedrunigt.option.auto_save_player_data").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.AUTO_SAVE_PLAYER_DATA) ? ScreenTexts.ON : ScreenTexts.OFF));
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.auto_save_player_data.description"))
                .setCategory("speedrunigt.option.category.records")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Text.translatable("speedrunigt.option.debug_mode").append(" : ").append(SpeedRunIGT.IS_DEBUG_MODE ? ScreenTexts.ON : ScreenTexts.OFF),
                                (ButtonWidget button) -> {
                                    SpeedRunIGT.IS_DEBUG_MODE = !SpeedRunIGT.IS_DEBUG_MODE;
                                    SpeedRunIGT.error("Debug mode is " + (SpeedRunIGT.IS_DEBUG_MODE ? "enabled" : "disabled") + "!");
                                    button.setMessage(Text.translatable("speedrunigt.option.debug_mode").append(" : ").append(SpeedRunIGT.IS_DEBUG_MODE ? ScreenTexts.ON : ScreenTexts.OFF));
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.debug_mode.description"))
                .setCategory("Debug")
        );


        if (Math.random() < 0.1) {
            factories.add(screen -> new OptionButtonFactory.Builder()
                    .setButtonWidget(ButtonWidgetHelper.create(0, 0, 150, 20, Text.literal("amongus"), (ButtonWidget button) -> {}))
                    .setCategory("???")
            );
        }

        if (Math.random() < 0.05) {
            factories.add(screen -> new OptionButtonFactory.Builder()
                    .setButtonWidget(ButtonWidgetHelper.create(0, 0, 150, 20, Text.literal("Dream Luck : OFF"), (ButtonWidget button) -> button.setMessage(Text.literal("HAHA no u"))))
                    .setCategory("???")
            );
        }

        if (Math.random() < 0.01) {
            factories.add(screen -> new OptionButtonFactory.Builder()
                    .setButtonWidget(ButtonWidgetHelper.create(0, 0, 150, 20, Text.literal("no way LMAO").formatted(Formatting.OBFUSCATED), (ButtonWidget button) -> {}))
                    .setCategory("???")
            );
        }

        return factories;
    }
}

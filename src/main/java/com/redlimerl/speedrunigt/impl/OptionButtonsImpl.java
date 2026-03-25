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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
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
                        ButtonWidgetHelper.create(0, 0, 150, 20, Component.literal("SpeedRunIGT Discord"),
                                (Button button) -> Util.getPlatform().openUri("https://discord.gg/7G2tfP7Xpe"))
                )
                .setCategory("speedrunigt.option.category.general")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Component.translatable("speedrunigt.option.timer_position"),
                                (Button button) -> Minecraft.getInstance().setScreen(new TimerCustomizeScreen(screen)))
                )
                .setCategory("speedrunigt.option.category.general")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Component.translatable("speedrunigt.option.timer_category"),
                                (Button button) -> Minecraft.getInstance().setScreen(new SpeedRunCategoryScreen(screen)))
                )
                .setCategory("speedrunigt.option.category.general")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Component.translatable("speedrunigt.option.check_info"),
                                (Button button) -> Minecraft.getInstance().setScreen(new SpeedRunIGTInfoScreen(screen)))
                )
                .setCategory("speedrunigt.option.category.general")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Component.translatable("speedrunigt.option.reload"),
                                (Button button) -> Minecraft.getInstance().setScreen(new ConfirmScreen(boolean1 -> {
                                    if (boolean1) {
                                        SpeedRunOption.reload();
                                    }
                                    Minecraft.getInstance().setScreen(screen);
                                }, Component.translatable("speedrunigt.message.reload_options"), Component.empty())))
                )
                .setCategory("speedrunigt.option.category.general")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Component.translatable("speedrunigt.option.global_options").append(" : ").append(SpeedRunOption.isUsingGlobalConfig() ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF),
                                (Button button) -> {
                                    SpeedRunOption.setUseGlobalConfig(!SpeedRunOption.isUsingGlobalConfig());
                                    Minecraft.getInstance().setScreen(new ConfirmScreen(boolean1 -> {
                                        if (boolean1) {
                                            SpeedRunOption.reload();
                                        }
                                        Minecraft.getInstance().setScreen(screen);
                                    }, Component.translatable("speedrunigt.message.reload_options"), Component.empty()));
                                })
                )
                .setToolTip(() -> I18n.get("speedrunigt.option.global_options.description", SpeedRunOption.getConfigPath()))
                .setCategory("speedrunigt.option.category.general")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Component.translatable("speedrunigt.option.safe_font_mode").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.CUSTOM_FONT_SAFE_MODE) ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF),
                                (Button button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.CUSTOM_FONT_SAFE_MODE, !SpeedRunOption.getOption(SpeedRunOptions.CUSTOM_FONT_SAFE_MODE));
                                    button.setMessage(Component.translatable("speedrunigt.option.safe_font_mode").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.CUSTOM_FONT_SAFE_MODE) ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF));
                                })
                )
                .setToolTip(() -> I18n.get("speedrunigt.option.safe_font_mode.description"))
                .setCategory("speedrunigt.option.category.general")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Component.translatable("speedrunigt.option.always_english_translations").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.ALWAYS_ENGLISH_TRANSLATIONS) ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF),
                                (Button button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.ALWAYS_ENGLISH_TRANSLATIONS, !SpeedRunOption.getOption(SpeedRunOptions.ALWAYS_ENGLISH_TRANSLATIONS));
                                    button.setMessage(Component.translatable("speedrunigt.option.always_english_translations").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.ALWAYS_ENGLISH_TRANSLATIONS) ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF));

                                    TranslateHelper.reload();
                                })
                )
                .setToolTip(() -> I18n.get("speedrunigt.option.always_english_translations.description"))
                .setCategory("speedrunigt.option.category.general")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Component.translatable("speedrunigt.option.timer_position.toggle_timer").append(" : ").append(TIMER_DRAWER.isToggle() ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF),
                                (Button button) -> {
                                    TIMER_DRAWER.setToggle(!TIMER_DRAWER.isToggle());
                                    SpeedRunOption.setOption(SpeedRunOptions.TOGGLE_TIMER, TIMER_DRAWER.isToggle());
                                    button.setMessage(Component.translatable("speedrunigt.option.timer_position.toggle_timer").append(" : ").append(TIMER_DRAWER.isToggle() ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF));
                                })
                )
                .setCategory("speedrunigt.option.category.timer")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Component.translatable("speedrunigt.option.hide_timer_in_options").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.HIDE_TIMER_IN_OPTIONS) ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF),
                                (Button button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.HIDE_TIMER_IN_OPTIONS, !SpeedRunOption.getOption(SpeedRunOptions.HIDE_TIMER_IN_OPTIONS));
                                    button.setMessage(Component.translatable("speedrunigt.option.hide_timer_in_options").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.HIDE_TIMER_IN_OPTIONS) ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF));
                                })
                )
                .setCategory("speedrunigt.option.category.timer")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Component.translatable("speedrunigt.option.hide_timer_in_debugs").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.HIDE_TIMER_IN_DEBUGS) ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF),
                                (Button button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.HIDE_TIMER_IN_DEBUGS, !SpeedRunOption.getOption(SpeedRunOptions.HIDE_TIMER_IN_DEBUGS));
                                    button.setMessage(Component.translatable("speedrunigt.option.hide_timer_in_debugs").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.HIDE_TIMER_IN_DEBUGS) ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF));
                                })
                )
                .setCategory("speedrunigt.option.category.timer")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Component.translatable("speedrunigt.option.timer_start_type").append(" : ").append(Component.translatable("speedrunigt.option.timer_start_type." + SpeedRunOption.getOption(SpeedRunOptions.WAITING_FIRST_INPUT).name().toLowerCase(Locale.ROOT))),
                                (Button button) -> {
                                    int order = SpeedRunOption.getOption(SpeedRunOptions.WAITING_FIRST_INPUT).ordinal() + 1;
                                    SpeedRunOptions.TimerStartType[] intervals = SpeedRunOptions.TimerStartType.values();
                                    SpeedRunOption.setOption(SpeedRunOptions.WAITING_FIRST_INPUT, intervals[order % intervals.length]);
                                    button.setMessage(Component.translatable("speedrunigt.option.timer_start_type").append(" : ").append(Component.translatable("speedrunigt.option.timer_start_type." + SpeedRunOption.getOption(SpeedRunOptions.WAITING_FIRST_INPUT).name().toLowerCase(Locale.ROOT))));
                                })
                )
                .setToolTip(() -> I18n.get("speedrunigt.option.timer_start_type.description"))
                .setCategory("speedrunigt.option.category.timing")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Component.translatable("speedrunigt.option.auto_toggle_coop").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.AUTOMATIC_COOP_MODE) ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF),
                                (Button button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.AUTOMATIC_COOP_MODE, !SpeedRunOption.getOption(SpeedRunOptions.AUTOMATIC_COOP_MODE));
                                    button.setMessage(Component.translatable("speedrunigt.option.auto_toggle_coop").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.AUTOMATIC_COOP_MODE) ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF));
                                })
                )
                .setToolTip(() -> I18n.get("speedrunigt.option.auto_toggle_coop.description"))
                .setCategory("speedrunigt.option.category.timer")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Component.translatable("speedrunigt.option.start_old_worlds").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.TIMER_START_GENERATED_WORLD) ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF),
                                (Button button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.TIMER_START_GENERATED_WORLD, !SpeedRunOption.getOption(SpeedRunOptions.TIMER_START_GENERATED_WORLD));
                                    button.setMessage(Component.translatable("speedrunigt.option.start_old_worlds").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.TIMER_START_GENERATED_WORLD) ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF));
                                })
                )
                .setToolTip(() -> I18n.get("speedrunigt.option.start_old_worlds.description"))
                .setCategory("speedrunigt.option.category.timing")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Component.translatable("speedrunigt.option.limitless_reset").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.TIMER_LIMITLESS_RESET) ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF),
                                (Button button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.TIMER_LIMITLESS_RESET, !SpeedRunOption.getOption(SpeedRunOptions.TIMER_LIMITLESS_RESET));
                                    button.setMessage(Component.translatable("speedrunigt.option.limitless_reset").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.TIMER_LIMITLESS_RESET) ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF));
                                })
                )
                .setToolTip(() -> I18n.get("speedrunigt.option.limitless_reset.description"))
                .setCategory("speedrunigt.option.category.timing")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Component.translatable("speedrunigt.option.current_extensions"),
                                (Button button) -> {})
                )
                .setToolTip(() -> {
                    StringBuilder extension = new StringBuilder(I18n.get("speedrunigt.option.current_extensions.description", SpeedRunIGTApi.getProviders().length));
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
                        ButtonWidgetHelper.create(0, 0, 150, 20, Component.translatable("speedrunigt.option.legacy_igt_mode").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.TIMER_LEGACY_IGT_MODE) ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF),
                                (Button button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.TIMER_LEGACY_IGT_MODE, !SpeedRunOption.getOption(SpeedRunOptions.TIMER_LEGACY_IGT_MODE));
                                    button.setMessage(Component.translatable("speedrunigt.option.legacy_igt_mode").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.TIMER_LEGACY_IGT_MODE) ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF));
                                })
                )
                .setToolTip(() -> I18n.get("speedrunigt.option.legacy_igt_mode.description"))
                .setCategory("speedrunigt.option.category.timer")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Component.translatable("speedrunigt.option.auto_save_interval").append(" : ").append(Component.translatable("speedrunigt.option.auto_save_interval." + SpeedRunOption.getOption(SpeedRunOptions.TIMER_DATA_AUTO_SAVE).name().toLowerCase(Locale.ROOT))),
                                (Button button) -> {
                                    int order = SpeedRunOption.getOption(SpeedRunOptions.TIMER_DATA_AUTO_SAVE).ordinal() + 1;
                                    SpeedRunOptions.TimerSaveInterval[] intervals = SpeedRunOptions.TimerSaveInterval.values();
                                    SpeedRunOption.setOption(SpeedRunOptions.TIMER_DATA_AUTO_SAVE, intervals[order % intervals.length]);
                                    button.setMessage(Component.translatable("speedrunigt.option.auto_save_interval").append(" : ").append(Component.translatable("speedrunigt.option.auto_save_interval." + SpeedRunOption.getOption(SpeedRunOptions.TIMER_DATA_AUTO_SAVE).name().toLowerCase(Locale.ROOT))));
                                })
                )
                .setToolTip(() -> I18n.get("speedrunigt.option.auto_save_interval.description"))
                .setCategory("speedrunigt.option.category.timer")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Component.translatable("speedrunigt.option.practice_detect").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.ENABLE_PRACTICE_DETECT) ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF),
                                (Button button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.ENABLE_PRACTICE_DETECT, !SpeedRunOption.getOption(SpeedRunOptions.ENABLE_PRACTICE_DETECT));
                                    button.setMessage(Component.translatable("speedrunigt.option.practice_detect").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.ENABLE_PRACTICE_DETECT) ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF));
                                })
                )
                .setToolTip(() -> I18n.get("speedrunigt.option.practice_detect.description"))
                .setCategory("speedrunigt.option.category.practice")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Component.translatable("speedrunigt.option.teleport_to_practice").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.TELEPORT_TO_END_PRACTICE) ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF),
                                (Button button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.TELEPORT_TO_END_PRACTICE, !SpeedRunOption.getOption(SpeedRunOptions.TELEPORT_TO_END_PRACTICE));
                                    button.setMessage(Component.translatable("speedrunigt.option.teleport_to_practice").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.TELEPORT_TO_END_PRACTICE) ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF));
                                })
                )
                .setToolTip(() -> I18n.get("speedrunigt.option.teleport_to_practice.description"))
                .setCategory("speedrunigt.option.category.practice")
        );

        Button alwaysAutoRetimeButton = ButtonWidgetHelper.create(0, 0, 150, 20, Component.translatable("speedrunigt.option.always_use_auto_retime").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.ALWAYS_USE_AUTO_RETIME) ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF),
                (Button button) -> {
                    SpeedRunOption.setOption(SpeedRunOptions.ALWAYS_USE_AUTO_RETIME, !SpeedRunOption.getOption(SpeedRunOptions.ALWAYS_USE_AUTO_RETIME));
                    button.setMessage(Component.translatable("speedrunigt.option.always_use_auto_retime").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.ALWAYS_USE_AUTO_RETIME) ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF));
                });

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Component.translatable("speedrunigt.option.auto_retime").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.AUTO_RETIME_FOR_GUIDELINE) ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF),
                                (Button button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.AUTO_RETIME_FOR_GUIDELINE, !SpeedRunOption.getOption(SpeedRunOptions.AUTO_RETIME_FOR_GUIDELINE));
                                    button.setMessage(Component.translatable("speedrunigt.option.auto_retime").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.AUTO_RETIME_FOR_GUIDELINE) ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF));
                                    alwaysAutoRetimeButton.active = SpeedRunOption.getOption(SpeedRunOptions.AUTO_RETIME_FOR_GUIDELINE);
                                })
                )
                .setToolTip(() -> I18n.get("speedrunigt.option.auto_retime.description"))
                .setCategory("speedrunigt.option.category.retime")
        );

        factories.add(screen -> {
            alwaysAutoRetimeButton.active = SpeedRunOption.getOption(SpeedRunOptions.AUTO_RETIME_FOR_GUIDELINE);
            return new OptionButtonFactory.Builder()
                            .setButtonWidget(alwaysAutoRetimeButton)
                            .setToolTip(() -> I18n.get("speedrunigt.option.always_use_auto_retime.description"))
                            .setCategory("speedrunigt.option.category.retime");
            }
        );

        factories.add(screen -> {
            Supplier<Component> makeText = () -> {
                int value = SpeedRunOption.getOption(SpeedRunOptions.CHANGE_ANY_TO_AA_OVER);
                return Component.translatable("speedrunigt.option.auto_toggle_aa").append(" : ")
                        .append(value > 0 ? Component.literal(value+"+") : CommonComponents.OPTION_OFF);
            };
            return new OptionButtonFactory.Builder()
                            .setButtonWidget(
                                    new AbstractSliderButton(0, 0, 150, 20, makeText.get(), Mth.clamp(SpeedRunOption.getOption(SpeedRunOptions.CHANGE_ANY_TO_AA_OVER) / 50.0, 0.0, 1.0)) {
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
                            .setToolTip(() -> I18n.get("speedrunigt.option.auto_toggle_aa.description"))
                            .setCategory("speedrunigt.option.category.timer");
        });

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Component.translatable("speedrunigt.option.generate_record").append(" : ").append(Component.translatable("speedrunigt.option.generate_record." + SpeedRunOption.getOption(SpeedRunOptions.GENERATE_RECORD_FILE).name().toLowerCase(Locale.ROOT))),
                                (Button button) -> {
                                    int order = SpeedRunOption.getOption(SpeedRunOptions.GENERATE_RECORD_FILE).ordinal() + 1;
                                    SpeedRunOptions.RecordGenerateType[] intervals = SpeedRunOptions.RecordGenerateType.values();
                                    SpeedRunOption.setOption(SpeedRunOptions.GENERATE_RECORD_FILE, intervals[order % intervals.length]);
                                    button.setMessage(Component.translatable("speedrunigt.option.generate_record").append(" : ").append(Component.translatable("speedrunigt.option.generate_record." + SpeedRunOption.getOption(SpeedRunOptions.GENERATE_RECORD_FILE).name().toLowerCase(Locale.ROOT))));
                                })
                )
                .setCategory("speedrunigt.option.category.records")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Component.translatable("speedrunigt.option.open_records_folder"),
                                (Button button) -> Util.getPlatform().openFile(SpeedRunIGT.getRecordsPath().toFile()))
                )
                .setCategory("speedrunigt.option.category.records")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Component.translatable("speedrunigt.option.delete_all_records"),
                                (Button button) -> Minecraft.getInstance().setScreen(new ConfirmScreen(boolean1 -> {
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
                                    Minecraft.getInstance().setScreen(screen);
                                }, Component.translatable("speedrunigt.option.delete_all_records.description"), Component.empty())))
                )
                .setCategory("speedrunigt.option.category.records")
        );

        factories.add(screen -> {
            if (InGameTimer.getInstance().isStopped()) {
                Button buttonWidget = ButtonWidgetHelper.create(0, 0, 150, 20, Component.translatable("speedrunigt.option.generate_timer_logs"), (Button button) -> {});
                buttonWidget.active = false;
                return new OptionButtonFactory.Builder()
                        .setButtonWidget(
                                buttonWidget
                        )
                        .setToolTip(() -> I18n.get("speedrunigt.option.generate_timer_logs.description"))
                        .setCategory("speedrunigt.option.category.records");
            }
            return new OptionButtonFactory.Builder()
                            .setButtonWidget(
                                    ButtonWidgetHelper.create(0, 0, 150, 20, Component.translatable("speedrunigt.option.generate_timer_logs"),
                                            (Button button) ->
                                                    Minecraft.getInstance().setScreen(new ConfirmScreen(boolean1 -> {
                                                        if (boolean1) {
                                                            try {
                                                                InGameTimer.writeTimerLogs(InGameTimer.getInstance());
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                        Minecraft.getInstance().setScreen(screen);
                                                    }, Component.translatable("speedrunigt.option.generate_timer_logs.message"), Component.empty()))
                                    )
                            )
                            .setToolTip(() -> I18n.get("speedrunigt.option.generate_timer_logs.description"))
                            .setCategory("speedrunigt.option.category.records");
            }
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Component.translatable("speedrunigt.option.auto_save_player_data").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.AUTO_SAVE_PLAYER_DATA) ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF),
                                (Button button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.AUTO_SAVE_PLAYER_DATA, !SpeedRunOption.getOption(SpeedRunOptions.AUTO_SAVE_PLAYER_DATA));
                                    button.setMessage(Component.translatable("speedrunigt.option.auto_save_player_data").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.AUTO_SAVE_PLAYER_DATA) ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF));
                                })
                )
                .setToolTip(() -> I18n.get("speedrunigt.option.auto_save_player_data.description"))
                .setCategory("speedrunigt.option.category.records")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        ButtonWidgetHelper.create(0, 0, 150, 20, Component.translatable("speedrunigt.option.debug_mode").append(" : ").append(SpeedRunIGT.IS_DEBUG_MODE ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF),
                                (Button button) -> {
                                    SpeedRunIGT.IS_DEBUG_MODE = !SpeedRunIGT.IS_DEBUG_MODE;
                                    SpeedRunIGT.error("Debug mode is " + (SpeedRunIGT.IS_DEBUG_MODE ? "enabled" : "disabled") + "!");
                                    button.setMessage(Component.translatable("speedrunigt.option.debug_mode").append(" : ").append(SpeedRunIGT.IS_DEBUG_MODE ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF));
                                })
                )
                .setToolTip(() -> I18n.get("speedrunigt.option.debug_mode.description"))
                .setCategory("Debug")
        );


        if (Math.random() < 0.1) {
            factories.add(screen -> new OptionButtonFactory.Builder()
                    .setButtonWidget(ButtonWidgetHelper.create(0, 0, 150, 20, Component.literal("amongus"), (Button button) -> {}))
                    .setCategory("???")
            );
        }

        if (Math.random() < 0.05) {
            factories.add(screen -> new OptionButtonFactory.Builder()
                    .setButtonWidget(ButtonWidgetHelper.create(0, 0, 150, 20, Component.literal("Dream Luck : OFF"), (Button button) -> button.setMessage(Component.literal("HAHA no u"))))
                    .setCategory("???")
            );
        }

        if (Math.random() < 0.01) {
            factories.add(screen -> new OptionButtonFactory.Builder()
                    .setButtonWidget(ButtonWidgetHelper.create(0, 0, 150, 20, Component.literal("no way LMAO").withStyle(ChatFormatting.OBFUSCATED), (Button button) -> {}))
                    .setCategory("???")
            );
        }

        return factories;
    }
}

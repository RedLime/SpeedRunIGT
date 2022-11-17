package com.redlimerl.speedrunigt.impl;

import com.mojang.blaze3d.platform.GLX;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.api.OptionButtonFactory;
import com.redlimerl.speedrunigt.api.SpeedRunIGTApi;
import com.redlimerl.speedrunigt.gui.ConsumerButtonWidget;
import com.redlimerl.speedrunigt.gui.CustomSliderWidget;
import com.redlimerl.speedrunigt.gui.screen.SpeedRunCategoryScreen;
import com.redlimerl.speedrunigt.gui.screen.SpeedRunIGTInfoScreen;
import com.redlimerl.speedrunigt.gui.screen.TheRunUploadKeyScreen;
import com.redlimerl.speedrunigt.gui.screen.TimerCustomizeScreen;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.utils.OperatingUtils;
import com.redlimerl.speedrunigt.version.ScreenTexts;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.utils.TranslateHelper;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.function.Supplier;

import static com.redlimerl.speedrunigt.SpeedRunIGTClient.TIMER_DRAWER;

public class OptionButtonsImpl implements SpeedRunIGTApi {

    private ConsumerButtonWidget alwaysAutoRetimeButton = null;

    @Override
    public Collection<OptionButtonFactory> createOptionButtons() {
        ArrayList<OptionButtonFactory> factories = new ArrayList<>();

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, "SpeedRunIGT Discord",
                                (button) -> OperatingUtils.setUrl("https://discord.gg/7G2tfP7Xpe"))
                )
                .setCategory("speedrunigt.option.category.general")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableText("speedrunigt.option.timer_position").asFormattedString(),
                                (button) -> MinecraftClient.getInstance().openScreen(new TimerCustomizeScreen(screen)))
                )
                .setCategory("speedrunigt.option.category.general")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableText("speedrunigt.option.timer_category").asFormattedString(),
                                (button) -> MinecraftClient.getInstance().openScreen(new SpeedRunCategoryScreen(screen)))
                )
                .setCategory("speedrunigt.option.category.general")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableText("speedrunigt.option.check_info").asFormattedString(),
                                (button) -> MinecraftClient.getInstance().openScreen(new SpeedRunIGTInfoScreen(screen)))
                )
                .setCategory("speedrunigt.option.category.general")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableText("speedrunigt.option.reload").asFormattedString(),
                                (button) -> MinecraftClient.getInstance().openScreen(new ConfirmScreen((boolean1, i) -> {
                                    if (boolean1) {
                                        SpeedRunOption.reload();
                                    }
                                    MinecraftClient.getInstance().openScreen(screen);
                                }, new TranslatableText("speedrunigt.message.reload_options").getString(), "", 0)))
                )
                .setCategory("speedrunigt.option.category.general")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableText("speedrunigt.option.global_options").append(" : ").append(SpeedRunOption.isUsingGlobalConfig() ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString(),
                                (button) -> {
                                    SpeedRunOption.setUseGlobalConfig(!SpeedRunOption.isUsingGlobalConfig());
                                    MinecraftClient.getInstance().openScreen(new ConfirmScreen((boolean1, i) -> {
                                        if (boolean1) {
                                            SpeedRunOption.reload();
                                        }
                                        MinecraftClient.getInstance().openScreen(screen);
                                    }, new TranslatableText("speedrunigt.message.reload_options").getString(), "", 0));
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.global_options.description", SpeedRunOption.getConfigPath()))
                .setCategory("speedrunigt.option.category.general")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableText("speedrunigt.option.safe_font_mode").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.CUSTOM_FONT_SAFE_MODE) ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString(),
                                (button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.CUSTOM_FONT_SAFE_MODE, !SpeedRunOption.getOption(SpeedRunOptions.CUSTOM_FONT_SAFE_MODE));
                                    button.message = (new TranslatableText("speedrunigt.option.safe_font_mode").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.CUSTOM_FONT_SAFE_MODE) ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString());
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.safe_font_mode.description"))
                .setCategory("speedrunigt.option.category.general")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableText("speedrunigt.option.always_english_translations").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.ALWAYS_ENGLISH_TRANSLATIONS) ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString(),
                                (button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.ALWAYS_ENGLISH_TRANSLATIONS, !SpeedRunOption.getOption(SpeedRunOptions.ALWAYS_ENGLISH_TRANSLATIONS));
                                    button.message = (new TranslatableText("speedrunigt.option.always_english_translations").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.ALWAYS_ENGLISH_TRANSLATIONS) ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString());

                                    TranslateHelper.reload();
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.always_english_translations.description"))
                .setCategory("speedrunigt.option.category.general")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableText("speedrunigt.option.timer_position.toggle_timer").append(" : ").append(TIMER_DRAWER.isToggle() ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString(),
                                (button) -> {
                                    TIMER_DRAWER.setToggle(!TIMER_DRAWER.isToggle());
                                    SpeedRunOption.setOption(SpeedRunOptions.TOGGLE_TIMER, TIMER_DRAWER.isToggle());
                                    button.message = (new TranslatableText("speedrunigt.option.timer_position.toggle_timer").append(" : ").append(TIMER_DRAWER.isToggle() ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString());
                                })
                )
                .setCategory("speedrunigt.option.category.timer")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableText("speedrunigt.option.hide_timer_in_options").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.HIDE_TIMER_IN_OPTIONS) ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString(),
                                (button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.HIDE_TIMER_IN_OPTIONS, !SpeedRunOption.getOption(SpeedRunOptions.HIDE_TIMER_IN_OPTIONS));
                                    button.message = (new TranslatableText("speedrunigt.option.hide_timer_in_options").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.HIDE_TIMER_IN_OPTIONS) ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString());
                                })
                )
                .setCategory("speedrunigt.option.category.timer")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableText("speedrunigt.option.hide_timer_in_debugs").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.HIDE_TIMER_IN_DEBUGS) ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString(),
                                (button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.HIDE_TIMER_IN_DEBUGS, !SpeedRunOption.getOption(SpeedRunOptions.HIDE_TIMER_IN_DEBUGS));
                                    button.message = (new TranslatableText("speedrunigt.option.hide_timer_in_debugs").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.HIDE_TIMER_IN_DEBUGS) ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString());
                                })
                )
                .setCategory("speedrunigt.option.category.timer")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableText("speedrunigt.option.timer_start_type").append(" : ").append(new TranslatableText("speedrunigt.option.timer_start_type." + SpeedRunOption.getOption(SpeedRunOptions.WAITING_FIRST_INPUT).name().toLowerCase(Locale.ROOT))).asFormattedString(),
                                (button) -> {
                                    int order = SpeedRunOption.getOption(SpeedRunOptions.WAITING_FIRST_INPUT).ordinal() + 1;
                                    SpeedRunOptions.TimerStartType[] intervals = SpeedRunOptions.TimerStartType.values();
                                    SpeedRunOption.setOption(SpeedRunOptions.WAITING_FIRST_INPUT, intervals[order % intervals.length]);
                                    button.message = (new TranslatableText("speedrunigt.option.timer_start_type").append(" : ").append(new TranslatableText("speedrunigt.option.timer_start_type." + SpeedRunOption.getOption(SpeedRunOptions.WAITING_FIRST_INPUT).name().toLowerCase(Locale.ROOT))).asFormattedString());
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.timer_start_type.description"))
                .setCategory("speedrunigt.option.category.timing")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableText("speedrunigt.option.auto_toggle_coop").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.AUTOMATIC_COOP_MODE) ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString(),
                                (button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.AUTOMATIC_COOP_MODE, !SpeedRunOption.getOption(SpeedRunOptions.AUTOMATIC_COOP_MODE));
                                    button.message = (new TranslatableText("speedrunigt.option.auto_toggle_coop").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.AUTOMATIC_COOP_MODE) ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString());
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.auto_toggle_coop.description"))
                .setCategory("speedrunigt.option.category.timer")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableText("speedrunigt.option.start_old_worlds").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.TIMER_START_GENERATED_WORLD) ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString(),
                                (button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.TIMER_START_GENERATED_WORLD, !SpeedRunOption.getOption(SpeedRunOptions.TIMER_START_GENERATED_WORLD));
                                    button.message = (new TranslatableText("speedrunigt.option.start_old_worlds").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.TIMER_START_GENERATED_WORLD) ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString());
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.start_old_worlds.description"))
                .setCategory("speedrunigt.option.category.timing")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableText("speedrunigt.option.limitless_reset").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.TIMER_LIMITLESS_RESET) ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString(),
                                (button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.TIMER_LIMITLESS_RESET, !SpeedRunOption.getOption(SpeedRunOptions.TIMER_LIMITLESS_RESET));
                                    button.message = (new TranslatableText("speedrunigt.option.limitless_reset").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.TIMER_LIMITLESS_RESET) ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString());
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.limitless_reset.description"))
                .setCategory("speedrunigt.option.category.timing")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableText("speedrunigt.option.current_extensions").getString(), (buttonWidget) -> {})
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
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableText("speedrunigt.option.legacy_igt_mode").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.TIMER_LEGACY_IGT_MODE) ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString(),
                                (button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.TIMER_LEGACY_IGT_MODE, !SpeedRunOption.getOption(SpeedRunOptions.TIMER_LEGACY_IGT_MODE));
                                    button.message = (new TranslatableText("speedrunigt.option.legacy_igt_mode").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.TIMER_LEGACY_IGT_MODE) ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString());
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.legacy_igt_mode.description"))
                .setCategory("speedrunigt.option.category.timer")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableText("speedrunigt.option.auto_save_interval").append(" : ").append(new TranslatableText("speedrunigt.option.auto_save_interval." + SpeedRunOption.getOption(SpeedRunOptions.TIMER_DATA_AUTO_SAVE).name().toLowerCase(Locale.ROOT)).asFormattedString()).asFormattedString(),
                                (button) -> {
                                    int order = SpeedRunOption.getOption(SpeedRunOptions.TIMER_DATA_AUTO_SAVE).ordinal() + 1;
                                    SpeedRunOptions.TimerSaveInterval[] intervals = SpeedRunOptions.TimerSaveInterval.values();
                                    SpeedRunOption.setOption(SpeedRunOptions.TIMER_DATA_AUTO_SAVE, intervals[order % intervals.length]);
                                    button.message = (new TranslatableText("speedrunigt.option.auto_save_interval").append(" : ").append(new TranslatableText("speedrunigt.option.auto_save_interval." + SpeedRunOption.getOption(SpeedRunOptions.TIMER_DATA_AUTO_SAVE).name().toLowerCase(Locale.ROOT)).asFormattedString()).asFormattedString());
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.auto_save_interval.description"))
                .setCategory("speedrunigt.option.category.timer")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableText("speedrunigt.option.practice_detect").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.ENABLE_PRACTICE_DETECT) ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString(),
                                (button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.ENABLE_PRACTICE_DETECT, !SpeedRunOption.getOption(SpeedRunOptions.ENABLE_PRACTICE_DETECT));
                                    button.message = (new TranslatableText("speedrunigt.option.practice_detect").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.ENABLE_PRACTICE_DETECT) ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString());
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.practice_detect.description"))
                .setCategory("speedrunigt.option.category.practice")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableText("speedrunigt.option.teleport_to_practice").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.TELEPORT_TO_END_PRACTICE) ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString(),
                                (button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.TELEPORT_TO_END_PRACTICE, !SpeedRunOption.getOption(SpeedRunOptions.TELEPORT_TO_END_PRACTICE));
                                    button.message = (new TranslatableText("speedrunigt.option.teleport_to_practice").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.TELEPORT_TO_END_PRACTICE) ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString());
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.teleport_to_practice.description"))
                .setCategory("speedrunigt.option.category.practice")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableText("speedrunigt.option.auto_retime").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.AUTO_RETIME_FOR_GUIDELINE) ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString(),
                                (button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.AUTO_RETIME_FOR_GUIDELINE, !SpeedRunOption.getOption(SpeedRunOptions.AUTO_RETIME_FOR_GUIDELINE));
                                    button.message = (new TranslatableText("speedrunigt.option.auto_retime").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.AUTO_RETIME_FOR_GUIDELINE) ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString());
                                    alwaysAutoRetimeButton.active = SpeedRunOption.getOption(SpeedRunOptions.AUTO_RETIME_FOR_GUIDELINE);
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.auto_retime.description"))
                .setCategory("speedrunigt.option.category.retime")
        );

        factories.add(screen -> {
            alwaysAutoRetimeButton = new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableText("speedrunigt.option.always_use_auto_retime").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.ALWAYS_USE_AUTO_RETIME) ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString(),
                    (button) -> {
                        SpeedRunOption.setOption(SpeedRunOptions.ALWAYS_USE_AUTO_RETIME, !SpeedRunOption.getOption(SpeedRunOptions.ALWAYS_USE_AUTO_RETIME));
                        button.message = (new TranslatableText("speedrunigt.option.always_use_auto_retime").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.ALWAYS_USE_AUTO_RETIME) ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString());
                    });
            alwaysAutoRetimeButton.active = SpeedRunOption.getOption(SpeedRunOptions.AUTO_RETIME_FOR_GUIDELINE);
            return new OptionButtonFactory.Builder()
                            .setButtonWidget(alwaysAutoRetimeButton)
                            .setToolTip(() -> I18n.translate("speedrunigt.option.always_use_auto_retime.description"))
                            .setCategory("speedrunigt.option.category.retime");
            }
        );

        factories.add(screen -> {
            Supplier<String> makeText = () -> {
                int value = SpeedRunOption.getOption(SpeedRunOptions.CHANGE_ANY_TO_AA_OVER);
                return new TranslatableText("speedrunigt.option.auto_toggle_aa").append(" : ")
                        .append(value > 0 ? (value+"+") : ScreenTexts.OFF).asFormattedString();
            };
            return new OptionButtonFactory.Builder()
                            .setButtonWidget(
                                    new CustomSliderWidget(0, 0, 150, 20, MathHelper.clamp(SpeedRunOption.getOption(SpeedRunOptions.CHANGE_ANY_TO_AA_OVER) / 50.0f, 0.0f, 1.0f), new CustomSliderWidget.SliderWorker() {
                                        @Override
                                        public String updateMessage() {
                                            return makeText.get();
                                        }

                                        @Override
                                        public void applyValue(float value) {
                                            SpeedRunOption.setOption(SpeedRunOptions.CHANGE_ANY_TO_AA_OVER, Math.round(value * 50));
                                        }
                                    })
                            )
                            .setToolTip(() -> I18n.translate("speedrunigt.option.auto_toggle_aa.description"))
                            .setCategory("speedrunigt.option.category.timer");
        });

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableText("speedrunigt.option.generate_record").append(" : ").append(new TranslatableText("speedrunigt.option.generate_record." + SpeedRunOption.getOption(SpeedRunOptions.GENERATE_RECORD_FILE).name().toLowerCase(Locale.ROOT))).asFormattedString(),
                                (button) -> {
                                    int order = SpeedRunOption.getOption(SpeedRunOptions.GENERATE_RECORD_FILE).ordinal() + 1;
                                    SpeedRunOptions.RecordGenerateType[] intervals = SpeedRunOptions.RecordGenerateType.values();
                                    SpeedRunOption.setOption(SpeedRunOptions.GENERATE_RECORD_FILE, intervals[order % intervals.length]);
                                    button.message = (new TranslatableText("speedrunigt.option.generate_record").append(" : ").append(new TranslatableText("speedrunigt.option.generate_record." + SpeedRunOption.getOption(SpeedRunOptions.GENERATE_RECORD_FILE).name().toLowerCase(Locale.ROOT))).asFormattedString());
                                })
                )
                .setCategory("speedrunigt.option.category.records")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableText("speedrunigt.option.open_records_folder").asFormattedString(),
                                (button) -> GLX.method_12553(SpeedRunIGT.getRecordsPath().toFile()))
                )
                .setCategory("speedrunigt.option.category.records")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableText("speedrunigt.option.delete_all_records").asFormattedString(),
                                (button) -> MinecraftClient.getInstance().openScreen(new ConfirmScreen((boolean1, i) -> {
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
                                    MinecraftClient.getInstance().openScreen(screen);
                                }, new TranslatableText("speedrunigt.option.delete_all_records.description").asFormattedString(), "", 0)))
                )
                .setCategory("speedrunigt.option.category.records")
        );

        factories.add(screen -> {
            if (InGameTimer.getInstance().isStopped()) {
                ConsumerButtonWidget buttonWidget = new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableText("speedrunigt.option.generate_timer_logs").asFormattedString(), (button) -> {});
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
                                    new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableText("speedrunigt.option.generate_timer_logs").asFormattedString(),
                                            (button) ->
                                                    MinecraftClient.getInstance().openScreen(new ConfirmScreen((boolean1, i) -> {
                                                        if (boolean1) {
                                                            try {
                                                                InGameTimer.writeTimerLogs(InGameTimer.getInstance());
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                        MinecraftClient.getInstance().openScreen(screen);
                                                    }, new TranslatableText("speedrunigt.option.generate_timer_logs.message").asFormattedString(), "", 0))
                                    )
                            )
                            .setToolTip(() -> I18n.translate("speedrunigt.option.generate_timer_logs.description"))
                            .setCategory("speedrunigt.option.category.records");
            }
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableText("speedrunigt.option.auto_save_player_data").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.AUTO_SAVE_PLAYER_DATA) ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString(),
                                (button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.AUTO_SAVE_PLAYER_DATA, !SpeedRunOption.getOption(SpeedRunOptions.AUTO_SAVE_PLAYER_DATA));
                                    button.message = (new TranslatableText("speedrunigt.option.auto_save_player_data").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.AUTO_SAVE_PLAYER_DATA) ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString());
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.auto_save_player_data.description"))
                .setCategory("speedrunigt.option.category.records")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableText("speedrunigt.option.therun_gg.open_therun_gg").asFormattedString(),
                                (button) -> OperatingUtils.setUrl("https://therun.gg/"))
                )
                .setCategory("therun.gg")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableText("speedrunigt.option.therun_gg.edit_upload_key").asFormattedString(),
                                (button) -> MinecraftClient.getInstance().openScreen(new TheRunUploadKeyScreen(screen)))
                )
                .setCategory("therun.gg")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableText("speedrunigt.option.therun_gg.toggle_live").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.ENABLE_THERUN_GG_LIVE) ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString(),
                                (button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.ENABLE_THERUN_GG_LIVE, !SpeedRunOption.getOption(SpeedRunOptions.ENABLE_THERUN_GG_LIVE));
                                    button.message = (new TranslatableText("speedrunigt.option.therun_gg.toggle_live").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.ENABLE_THERUN_GG_LIVE) ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString());
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.therun_gg.toggle_live.description"))
                .setCategory("therun.gg")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableText("speedrunigt.option.debug_mode").append(" : ").append(SpeedRunIGT.IS_DEBUG_MODE ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString(),
                                (button) -> {
                                    SpeedRunIGT.IS_DEBUG_MODE = !SpeedRunIGT.IS_DEBUG_MODE;
                                    SpeedRunIGT.error("Debug mode is " + (SpeedRunIGT.IS_DEBUG_MODE ? "enabled" : "disabled") + "!");
                                    button.message = (new TranslatableText("speedrunigt.option.debug_mode").append(" : ").append(SpeedRunIGT.IS_DEBUG_MODE ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString());
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.debug_mode.description"))
                .setCategory("Debug")
        );


        if (Math.random() < 0.1) {
            factories.add(screen -> new OptionButtonFactory.Builder()
                    .setButtonWidget(new ConsumerButtonWidget(0, 0, 150, 20, "amongus", (button) -> {}))
                    .setCategory("???")
            );
        }

        if (Math.random() < 0.05) {
            factories.add(screen -> new OptionButtonFactory.Builder()
                    .setButtonWidget(new ConsumerButtonWidget(0, 0, 150, 20, "Dream Luck : OFF", (button) -> button.message = ("HAHA no u")))
                    .setCategory("???")
            );
        }

        if (Math.random() < 0.01) {
            factories.add(screen -> new OptionButtonFactory.Builder()
                    .setButtonWidget(new ConsumerButtonWidget(0, 0, 150, 20, new LiteralText("no way LMAO").setStyle(new Style().setFormatting(Formatting.OBFUSCATED)).asFormattedString(), (button) -> {}))
                    .setCategory("???")
            );
        }

        return factories;
    }
}

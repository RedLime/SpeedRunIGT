package com.redlimerl.speedrunigt.impl;

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
import net.minecraft.class_1009;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableTextContent;
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
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableTextContent("speedrunigt.option.timer_position").method_10865(),
                                (button) -> MinecraftClient.getInstance().setScreen(new TimerCustomizeScreen(screen)))
                )
                .setCategory("speedrunigt.option.category.general")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableTextContent("speedrunigt.option.timer_category").method_10865(),
                                (button) -> MinecraftClient.getInstance().setScreen(new SpeedRunCategoryScreen(screen)))
                )
                .setCategory("speedrunigt.option.category.general")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableTextContent("speedrunigt.option.check_info").method_10865(),
                                (button) -> MinecraftClient.getInstance().setScreen(new SpeedRunIGTInfoScreen(screen)))
                )
                .setCategory("speedrunigt.option.category.general")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableTextContent("speedrunigt.option.reload").method_10865(),
                                (button) -> MinecraftClient.getInstance().setScreen(new ConfirmScreen((boolean1, i) -> {
                                    if (boolean1) {
                                        SpeedRunOption.reload();
                                    }
                                    MinecraftClient.getInstance().setScreen(screen);
                                }, new TranslatableTextContent("speedrunigt.message.reload_options").method_0_5147(), "", 0)))
                )
                .setCategory("speedrunigt.option.category.general")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableTextContent("speedrunigt.option.global_options").append(" : ").append(SpeedRunOption.isUsingGlobalConfig() ? ScreenTexts.ON : ScreenTexts.OFF).method_10865(),
                                (button) -> {
                                    SpeedRunOption.setUseGlobalConfig(!SpeedRunOption.isUsingGlobalConfig());
                                    MinecraftClient.getInstance().setScreen(new ConfirmScreen((boolean1, i) -> {
                                        if (boolean1) {
                                            SpeedRunOption.reload();
                                        }
                                        MinecraftClient.getInstance().setScreen(screen);
                                    }, new TranslatableTextContent("speedrunigt.message.reload_options").method_0_5147(), "", 0));
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.global_options.description", SpeedRunOption.getConfigPath()))
                .setCategory("speedrunigt.option.category.general")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableTextContent("speedrunigt.option.safe_font_mode").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.CUSTOM_FONT_SAFE_MODE) ? ScreenTexts.ON : ScreenTexts.OFF).method_10865(),
                                (button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.CUSTOM_FONT_SAFE_MODE, !SpeedRunOption.getOption(SpeedRunOptions.CUSTOM_FONT_SAFE_MODE));
                                    button.field_2074 = (new TranslatableTextContent("speedrunigt.option.safe_font_mode").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.CUSTOM_FONT_SAFE_MODE) ? ScreenTexts.ON : ScreenTexts.OFF).method_10865());
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.safe_font_mode.description"))
                .setCategory("speedrunigt.option.category.general")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableTextContent("speedrunigt.option.always_english_translations").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.ALWAYS_ENGLISH_TRANSLATIONS) ? ScreenTexts.ON : ScreenTexts.OFF).method_10865(),
                                (button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.ALWAYS_ENGLISH_TRANSLATIONS, !SpeedRunOption.getOption(SpeedRunOptions.ALWAYS_ENGLISH_TRANSLATIONS));
                                    button.field_2074 = (new TranslatableTextContent("speedrunigt.option.always_english_translations").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.ALWAYS_ENGLISH_TRANSLATIONS) ? ScreenTexts.ON : ScreenTexts.OFF).method_10865());

                                    TranslateHelper.reload();
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.always_english_translations.description"))
                .setCategory("speedrunigt.option.category.general")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableTextContent("speedrunigt.option.timer_position.toggle_timer").append(" : ").append(TIMER_DRAWER.isToggle() ? ScreenTexts.ON : ScreenTexts.OFF).method_10865(),
                                (button) -> {
                                    TIMER_DRAWER.setToggle(!TIMER_DRAWER.isToggle());
                                    SpeedRunOption.setOption(SpeedRunOptions.TOGGLE_TIMER, TIMER_DRAWER.isToggle());
                                    button.field_2074 = (new TranslatableTextContent("speedrunigt.option.timer_position.toggle_timer").append(" : ").append(TIMER_DRAWER.isToggle() ? ScreenTexts.ON : ScreenTexts.OFF).method_10865());
                                })
                )
                .setCategory("speedrunigt.option.category.timer")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableTextContent("speedrunigt.option.hide_timer_in_options").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.HIDE_TIMER_IN_OPTIONS) ? ScreenTexts.ON : ScreenTexts.OFF).method_10865(),
                                (button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.HIDE_TIMER_IN_OPTIONS, !SpeedRunOption.getOption(SpeedRunOptions.HIDE_TIMER_IN_OPTIONS));
                                    button.field_2074 = (new TranslatableTextContent("speedrunigt.option.hide_timer_in_options").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.HIDE_TIMER_IN_OPTIONS) ? ScreenTexts.ON : ScreenTexts.OFF).method_10865());
                                })
                )
                .setCategory("speedrunigt.option.category.timer")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableTextContent("speedrunigt.option.hide_timer_in_debugs").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.HIDE_TIMER_IN_DEBUGS) ? ScreenTexts.ON : ScreenTexts.OFF).method_10865(),
                                (button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.HIDE_TIMER_IN_DEBUGS, !SpeedRunOption.getOption(SpeedRunOptions.HIDE_TIMER_IN_DEBUGS));
                                    button.field_2074 = (new TranslatableTextContent("speedrunigt.option.hide_timer_in_debugs").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.HIDE_TIMER_IN_DEBUGS) ? ScreenTexts.ON : ScreenTexts.OFF).method_10865());
                                })
                )
                .setCategory("speedrunigt.option.category.timer")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableTextContent("speedrunigt.option.timer_start_type").append(" : ").append(new TranslatableTextContent("speedrunigt.option.timer_start_type." + SpeedRunOption.getOption(SpeedRunOptions.WAITING_FIRST_INPUT).name().toLowerCase(Locale.ROOT))).method_10865(),
                                (button) -> {
                                    int order = SpeedRunOption.getOption(SpeedRunOptions.WAITING_FIRST_INPUT).ordinal() + 1;
                                    SpeedRunOptions.TimerStartType[] intervals = SpeedRunOptions.TimerStartType.values();
                                    SpeedRunOption.setOption(SpeedRunOptions.WAITING_FIRST_INPUT, intervals[order % intervals.length]);
                                    button.field_2074 = (new TranslatableTextContent("speedrunigt.option.timer_start_type").append(" : ").append(new TranslatableTextContent("speedrunigt.option.timer_start_type." + SpeedRunOption.getOption(SpeedRunOptions.WAITING_FIRST_INPUT).name().toLowerCase(Locale.ROOT))).method_10865());
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.timer_start_type.description"))
                .setCategory("speedrunigt.option.category.timing")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableTextContent("speedrunigt.option.auto_toggle_coop").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.AUTOMATIC_COOP_MODE) ? ScreenTexts.ON : ScreenTexts.OFF).method_10865(),
                                (button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.AUTOMATIC_COOP_MODE, !SpeedRunOption.getOption(SpeedRunOptions.AUTOMATIC_COOP_MODE));
                                    button.field_2074 = (new TranslatableTextContent("speedrunigt.option.auto_toggle_coop").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.AUTOMATIC_COOP_MODE) ? ScreenTexts.ON : ScreenTexts.OFF).method_10865());
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.auto_toggle_coop.description"))
                .setCategory("speedrunigt.option.category.timer")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableTextContent("speedrunigt.option.start_old_worlds").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.TIMER_START_GENERATED_WORLD) ? ScreenTexts.ON : ScreenTexts.OFF).method_10865(),
                                (button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.TIMER_START_GENERATED_WORLD, !SpeedRunOption.getOption(SpeedRunOptions.TIMER_START_GENERATED_WORLD));
                                    button.field_2074 = (new TranslatableTextContent("speedrunigt.option.start_old_worlds").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.TIMER_START_GENERATED_WORLD) ? ScreenTexts.ON : ScreenTexts.OFF).method_10865());
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.start_old_worlds.description"))
                .setCategory("speedrunigt.option.category.timing")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableTextContent("speedrunigt.option.limitless_reset").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.TIMER_LIMITLESS_RESET) ? ScreenTexts.ON : ScreenTexts.OFF).method_10865(),
                                (button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.TIMER_LIMITLESS_RESET, !SpeedRunOption.getOption(SpeedRunOptions.TIMER_LIMITLESS_RESET));
                                    button.field_2074 = (new TranslatableTextContent("speedrunigt.option.limitless_reset").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.TIMER_LIMITLESS_RESET) ? ScreenTexts.ON : ScreenTexts.OFF).method_10865());
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.limitless_reset.description"))
                .setCategory("speedrunigt.option.category.timing")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableTextContent("speedrunigt.option.current_extensions").method_0_5147(), (buttonWidget) -> {})
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
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableTextContent("speedrunigt.option.legacy_igt_mode").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.TIMER_LEGACY_IGT_MODE) ? ScreenTexts.ON : ScreenTexts.OFF).method_10865(),
                                (button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.TIMER_LEGACY_IGT_MODE, !SpeedRunOption.getOption(SpeedRunOptions.TIMER_LEGACY_IGT_MODE));
                                    button.field_2074 = (new TranslatableTextContent("speedrunigt.option.legacy_igt_mode").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.TIMER_LEGACY_IGT_MODE) ? ScreenTexts.ON : ScreenTexts.OFF).method_10865());
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.legacy_igt_mode.description"))
                .setCategory("speedrunigt.option.category.timer")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableTextContent("speedrunigt.option.auto_save_interval").append(" : ").append(new TranslatableTextContent("speedrunigt.option.auto_save_interval." + SpeedRunOption.getOption(SpeedRunOptions.TIMER_DATA_AUTO_SAVE).name().toLowerCase(Locale.ROOT)).method_10865()).method_10865(),
                                (button) -> {
                                    int order = SpeedRunOption.getOption(SpeedRunOptions.TIMER_DATA_AUTO_SAVE).ordinal() + 1;
                                    SpeedRunOptions.TimerSaveInterval[] intervals = SpeedRunOptions.TimerSaveInterval.values();
                                    SpeedRunOption.setOption(SpeedRunOptions.TIMER_DATA_AUTO_SAVE, intervals[order % intervals.length]);
                                    button.field_2074 = (new TranslatableTextContent("speedrunigt.option.auto_save_interval").append(" : ").append(new TranslatableTextContent("speedrunigt.option.auto_save_interval." + SpeedRunOption.getOption(SpeedRunOptions.TIMER_DATA_AUTO_SAVE).name().toLowerCase(Locale.ROOT)).method_10865()).method_10865());
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.auto_save_interval.description"))
                .setCategory("speedrunigt.option.category.timer")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableTextContent("speedrunigt.option.practice_detect").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.ENABLE_PRACTICE_DETECT) ? ScreenTexts.ON : ScreenTexts.OFF).method_10865(),
                                (button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.ENABLE_PRACTICE_DETECT, !SpeedRunOption.getOption(SpeedRunOptions.ENABLE_PRACTICE_DETECT));
                                    button.field_2074 = (new TranslatableTextContent("speedrunigt.option.practice_detect").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.ENABLE_PRACTICE_DETECT) ? ScreenTexts.ON : ScreenTexts.OFF).method_10865());
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.practice_detect.description"))
                .setCategory("speedrunigt.option.category.practice")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableTextContent("speedrunigt.option.teleport_to_practice").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.TELEPORT_TO_END_PRACTICE) ? ScreenTexts.ON : ScreenTexts.OFF).method_10865(),
                                (button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.TELEPORT_TO_END_PRACTICE, !SpeedRunOption.getOption(SpeedRunOptions.TELEPORT_TO_END_PRACTICE));
                                    button.field_2074 = (new TranslatableTextContent("speedrunigt.option.teleport_to_practice").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.TELEPORT_TO_END_PRACTICE) ? ScreenTexts.ON : ScreenTexts.OFF).method_10865());
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.teleport_to_practice.description"))
                .setCategory("speedrunigt.option.category.practice")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableTextContent("speedrunigt.option.auto_retime").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.AUTO_RETIME_FOR_GUIDELINE) ? ScreenTexts.ON : ScreenTexts.OFF).method_10865(),
                                (button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.AUTO_RETIME_FOR_GUIDELINE, !SpeedRunOption.getOption(SpeedRunOptions.AUTO_RETIME_FOR_GUIDELINE));
                                    button.field_2074 = (new TranslatableTextContent("speedrunigt.option.auto_retime").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.AUTO_RETIME_FOR_GUIDELINE) ? ScreenTexts.ON : ScreenTexts.OFF).method_10865());
                                    alwaysAutoRetimeButton.field_2078 = SpeedRunOption.getOption(SpeedRunOptions.AUTO_RETIME_FOR_GUIDELINE);
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.auto_retime.description"))
                .setCategory("speedrunigt.option.category.retime")
        );

        factories.add(screen -> {
            alwaysAutoRetimeButton = new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableTextContent("speedrunigt.option.always_use_auto_retime").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.ALWAYS_USE_AUTO_RETIME) ? ScreenTexts.ON : ScreenTexts.OFF).method_10865(),
                    (button) -> {
                        SpeedRunOption.setOption(SpeedRunOptions.ALWAYS_USE_AUTO_RETIME, !SpeedRunOption.getOption(SpeedRunOptions.ALWAYS_USE_AUTO_RETIME));
                        button.field_2074 = (new TranslatableTextContent("speedrunigt.option.always_use_auto_retime").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.ALWAYS_USE_AUTO_RETIME) ? ScreenTexts.ON : ScreenTexts.OFF).method_10865());
                    });
            alwaysAutoRetimeButton.field_2078 = SpeedRunOption.getOption(SpeedRunOptions.AUTO_RETIME_FOR_GUIDELINE);
            return new OptionButtonFactory.Builder()
                            .setButtonWidget(alwaysAutoRetimeButton)
                            .setToolTip(() -> I18n.translate("speedrunigt.option.always_use_auto_retime.description"))
                            .setCategory("speedrunigt.option.category.retime");
            }
        );

        factories.add(screen -> {
            Supplier<String> makeText = () -> {
                int value = SpeedRunOption.getOption(SpeedRunOptions.CHANGE_ANY_TO_AA_OVER);
                return new TranslatableTextContent("speedrunigt.option.auto_toggle_aa").append(" : ")
                        .append(value > 0 ? (value+"+") : ScreenTexts.OFF).method_10865();
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
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableTextContent("speedrunigt.option.generate_record").append(" : ").append(new TranslatableTextContent("speedrunigt.option.generate_record." + SpeedRunOption.getOption(SpeedRunOptions.GENERATE_RECORD_FILE).name().toLowerCase(Locale.ROOT))).method_10865(),
                                (button) -> {
                                    int order = SpeedRunOption.getOption(SpeedRunOptions.GENERATE_RECORD_FILE).ordinal() + 1;
                                    SpeedRunOptions.RecordGenerateType[] intervals = SpeedRunOptions.RecordGenerateType.values();
                                    SpeedRunOption.setOption(SpeedRunOptions.GENERATE_RECORD_FILE, intervals[order % intervals.length]);
                                    button.field_2074 = (new TranslatableTextContent("speedrunigt.option.generate_record").append(" : ").append(new TranslatableTextContent("speedrunigt.option.generate_record." + SpeedRunOption.getOption(SpeedRunOptions.GENERATE_RECORD_FILE).name().toLowerCase(Locale.ROOT))).method_10865());
                                })
                )
                .setCategory("speedrunigt.option.category.records")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableTextContent("speedrunigt.option.open_records_folder").method_10865(),
                                (button) -> class_1009.method_0_9032(SpeedRunIGT.getRecordsPath().toFile()))
                )
                .setCategory("speedrunigt.option.category.records")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableTextContent("speedrunigt.option.delete_all_records").method_10865(),
                                (button) -> MinecraftClient.getInstance().setScreen(new ConfirmScreen((boolean1, i) -> {
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
                                }, new TranslatableTextContent("speedrunigt.option.delete_all_records.description").method_10865(), "", 0)))
                )
                .setCategory("speedrunigt.option.category.records")
        );

        factories.add(screen -> {
            if (InGameTimer.getInstance().isStopped()) {
                ConsumerButtonWidget buttonWidget = new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableTextContent("speedrunigt.option.generate_timer_logs").method_10865(), (button) -> {});
                buttonWidget.field_2078 = false;
                return new OptionButtonFactory.Builder()
                        .setButtonWidget(
                                buttonWidget
                        )
                        .setToolTip(() -> I18n.translate("speedrunigt.option.generate_timer_logs.description"))
                        .setCategory("speedrunigt.option.category.records");
            }
            return new OptionButtonFactory.Builder()
                            .setButtonWidget(
                                    new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableTextContent("speedrunigt.option.generate_timer_logs").method_10865(),
                                            (button) ->
                                                    MinecraftClient.getInstance().setScreen(new ConfirmScreen((boolean1, i) -> {
                                                        if (boolean1) {
                                                            try {
                                                                InGameTimer.writeTimerLogs(InGameTimer.getInstance());
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                        MinecraftClient.getInstance().setScreen(screen);
                                                    }, new TranslatableTextContent("speedrunigt.option.generate_timer_logs.message").method_10865(), "", 0))
                                    )
                            )
                            .setToolTip(() -> I18n.translate("speedrunigt.option.generate_timer_logs.description"))
                            .setCategory("speedrunigt.option.category.records");
            }
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableTextContent("speedrunigt.option.auto_save_player_data").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.AUTO_SAVE_PLAYER_DATA) ? ScreenTexts.ON : ScreenTexts.OFF).method_10865(),
                                (button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.AUTO_SAVE_PLAYER_DATA, !SpeedRunOption.getOption(SpeedRunOptions.AUTO_SAVE_PLAYER_DATA));
                                    button.field_2074 = (new TranslatableTextContent("speedrunigt.option.auto_save_player_data").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.AUTO_SAVE_PLAYER_DATA) ? ScreenTexts.ON : ScreenTexts.OFF).method_10865());
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.auto_save_player_data.description"))
                .setCategory("speedrunigt.option.category.records")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableTextContent("speedrunigt.option.therun_gg.open_therun_gg").method_10865(),
                                (button) -> OperatingUtils.setUrl("https://therun.gg/"))
                )
                .setCategory("therun.gg")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableTextContent("speedrunigt.option.therun_gg.edit_upload_key").method_10865(),
                                (button) -> MinecraftClient.getInstance().setScreen(new TheRunUploadKeyScreen(screen)))
                )
                .setCategory("therun.gg")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableTextContent("speedrunigt.option.therun_gg.toggle_live").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.ENABLE_THERUN_GG_LIVE) ? ScreenTexts.ON : ScreenTexts.OFF).method_10865(),
                                (button) -> {
                                    SpeedRunOption.setOption(SpeedRunOptions.ENABLE_THERUN_GG_LIVE, !SpeedRunOption.getOption(SpeedRunOptions.ENABLE_THERUN_GG_LIVE));
                                    button.field_2074 = (new TranslatableTextContent("speedrunigt.option.therun_gg.toggle_live").append(" : ").append(SpeedRunOption.getOption(SpeedRunOptions.ENABLE_THERUN_GG_LIVE) ? ScreenTexts.ON : ScreenTexts.OFF).method_10865());
                                })
                )
                .setToolTip(() -> I18n.translate("speedrunigt.option.therun_gg.toggle_live.description"))
                .setCategory("therun.gg")
        );

        factories.add(screen -> new OptionButtonFactory.Builder()
                .setButtonWidget(
                        new ConsumerButtonWidget(0, 0, 150, 20, new TranslatableTextContent("speedrunigt.option.debug_mode").append(" : ").append(SpeedRunIGT.IS_DEBUG_MODE ? ScreenTexts.ON : ScreenTexts.OFF).method_10865(),
                                (button) -> {
                                    SpeedRunIGT.IS_DEBUG_MODE = !SpeedRunIGT.IS_DEBUG_MODE;
                                    SpeedRunIGT.error("Debug mode is " + (SpeedRunIGT.IS_DEBUG_MODE ? "enabled" : "disabled") + "!");
                                    button.field_2074 = (new TranslatableTextContent("speedrunigt.option.debug_mode").append(" : ").append(SpeedRunIGT.IS_DEBUG_MODE ? ScreenTexts.ON : ScreenTexts.OFF).method_10865());
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
                    .setButtonWidget(new ConsumerButtonWidget(0, 0, 150, 20, "Dream Luck : OFF", (button) -> button.field_2074 = ("HAHA no u")))
                    .setCategory("???")
            );
        }

        if (Math.random() < 0.01) {
            factories.add(screen -> new OptionButtonFactory.Builder()
                    .setButtonWidget(new ConsumerButtonWidget(0, 0, 150, 20, new LiteralTextContent("no way LMAO").setStyle(new Style().withColor(Formatting.OBFUSCATED)).method_10865(), (button) -> {}))
                    .setCategory("???")
            );
        }

        return factories;
    }
}

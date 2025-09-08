package com.redlimerl.speedrunigt.mixins.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.redlimerl.speedrunigt.timer.category.RunCategoryArgumentType;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.command.arguments.serialize.ArgumentSerializer;
import net.minecraft.command.arguments.serialize.ConstantArgumentSerializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArgumentTypes.class)
public abstract class ArgumentTypesMixin {
    @Shadow
    public static <T extends ArgumentType<?>> void register(String id, Class<T> class_, ArgumentSerializer<T> argumentSerializer) {
    }

    @Inject(method = "register()V", at = @At("TAIL"))
    private static void registerRunCategoryArgumentType(CallbackInfo ci) {
        register("speedrunigt:run_category", RunCategoryArgumentType.class, new ConstantArgumentSerializer<>(RunCategoryArgumentType::new));
    }
}

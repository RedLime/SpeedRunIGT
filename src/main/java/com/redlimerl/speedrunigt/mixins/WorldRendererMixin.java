package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.utils.MixinValues;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @ModifyArg(method = "getChunksDebugString",
            at = @At(value = "INVOKE", target = "Ljava/lang/String;format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;"),
            index = 1)
    private Object[] redirectChunks(Object[] object) {
        MixinValues.COMPLETED_RENDER_CHUNKS = (int) object[0];
        return object;
    }

    @Inject(method = "setWorld", at = @At("TAIL"))
    private void onSetWorld(ClientWorld clientWorld, CallbackInfo ci) {
        MixinValues.IS_CHANGED_WORLD = true;
    }

}

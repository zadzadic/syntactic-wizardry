package com.proxpero.syntacticwizardry.mixin;

import com.proxpero.syntacticwizardry.EclipseLightRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockAndTintGetter.class)
public interface EclipseRawBrightnessMixin {
    @Inject(method = "getRawBrightness", at = @At("RETURN"), cancellable = true)
    private void syntacticwizardry$eclipseRawBrightness(
            BlockPos pos,
            int ambientDarkening,
            CallbackInfoReturnable<Integer> cir) {
        if (!((Object)this instanceof Level level)) return;
        if (EclipseLightRuntime.get(level) <= 0) return;

        BlockAndTintGetter getter = (BlockAndTintGetter)(Object)this;
        int blockLight = getter.getBrightness(LightLayer.BLOCK, pos);
        int skyLight = getter.getBrightness(LightLayer.SKY, pos);
        int effectiveSky = Math.max(0, skyLight - level.getSkyDarken());
        cir.setReturnValue(Math.max(blockLight, effectiveSky));
    }
}

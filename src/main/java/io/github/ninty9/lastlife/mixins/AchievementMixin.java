package io.github.ninty9.lastlife.mixins;

import io.github.ninty9.lastlife.Initializer;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static io.github.ninty9.lastlife.Initializer.LOGGER;

@Mixin(PlayerAdvancementTracker.class)
public class AchievementMixin {
    @Redirect( method = "grantCriterion", at = @At( value = "INVOKE",
    target = "Lnet/minecraft/server/network/ServerPlayerEntity;getDisplayName()Lnet/minecraft/text/Text;"))
    private Text ChangePlayer(ServerPlayerEntity instance) {
        LOGGER.info("testmix");
        return Team.decorateName(instance.getScoreboardTeam(), new LiteralText("A player"));
    }
}
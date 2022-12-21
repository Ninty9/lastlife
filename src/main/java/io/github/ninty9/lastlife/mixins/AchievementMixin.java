package io.github.ninty9.lastlife.mixins;

import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
@Mixin(PlayerAdvancementTracker.class)
public class AchievementMixin {
    @Redirect( method = "grantCriterion", at = @At( value = "INVOKE",
    target = "Lnet/minecraft/server/network/ServerPlayerEntity;getDisplayName()Lnet/minecraft/text/Text;"))
    private Text ChangePlayer(ServerPlayerEntity instance) {
        return Team.decorateName(instance.getScoreboardTeam(), new LiteralText("A player"));
    }
}
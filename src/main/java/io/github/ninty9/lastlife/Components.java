package io.github.ninty9.lastlife;

import dev.onyxstudios.cca.api.v3.component.*;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.component.ComponentFactory;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public final class Components implements EntityComponentInitializer {
    public static final ComponentKey<LivesComponent> LIVES =
            ComponentRegistry.getOrCreate(new Identifier("lastlife", "lives"), LivesComponent.class);
    ComponentFactory<PlayerEntity, LivesComponent> PlayerFac;
    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(LIVES, player -> new PlayerLivesComponent(), RespawnCopyStrategy.ALWAYS_COPY);

    }
}

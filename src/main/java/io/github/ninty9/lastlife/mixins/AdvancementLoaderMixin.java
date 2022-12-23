package io.github.ninty9.lastlife.mixins;


import com.google.gson.Gson;
import net.minecraft.advancement.Advancement;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Mixin(ServerAdvancementLoader.class)
public abstract class AdvancementLoaderMixin extends JsonDataLoader {
    public AdvancementLoaderMixin(Gson gson, String string) {
        super(gson, string);
    }

    @ModifyArg(
            method = "apply(Ljava/util/Map;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/AdvancementManager;load(Ljava/util/Map;)V")
    )
    private Map<Identifier, Advancement> filterMap(Map<Identifier, Advancement> map) {


        //todo: this should probably be disable able in config
        List<String> filter_paths = new ArrayList<>();
        filter_paths.add("minecraft:adventure");
        filter_paths.add("minecraft:story");
        filter_paths.add("minecraft:end");
        filter_paths.add("minecraft:nether");
        filter_paths.add("minecraft:husbandry");
        Predicate<Map.Entry<Identifier, Advancement>> path_predicate =
                (entry) -> filter_paths.stream().anyMatch(path -> entry.getKey().toString().startsWith(path));


            map.entrySet().removeIf(path_predicate);

        return map;
    }
}
package us.potatoboy.skywars.kit;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import us.potatoboy.skywars.SkyWars;
import xyz.nucleoid.plasmid.registry.TinyRegistry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;

public class KitRegistry {
    private static final TinyRegistry<Kit> KITS = TinyRegistry.newStable();

    public static void register() {
        ResourceManagerHelper serverData = ResourceManagerHelper.get(ResourceType.SERVER_DATA);

        serverData.registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return SkyWars.identifier("kits");
            }

            @Override
            public void apply(ResourceManager manager) {
                KITS.clear();

                Collection<Identifier> resources = manager.findResources("kits", path -> path.endsWith(".json"));

                for (Identifier path : resources) {
                    try {
                        Resource resource = manager.getResource(path);
                        try (Reader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
                            JsonElement json = new JsonParser().parse(reader);

                            Identifier identifier = identifierFromPath(path);

                            DataResult<Kit> result = Kit.CODEC.decode(JsonOps.INSTANCE, json).map(Pair::getFirst);

                            result.result().ifPresent(game -> KITS.register(identifier, game));

                            result.error().ifPresent(error -> SkyWars.LOGGER.error("Failed to decode kit at {}: {}", path, error.toString()));
                        }
                    } catch (IOException e) {
                        SkyWars.LOGGER.error("Failed to kit at {}", path, e);
                    }
                }
            }
        });
    }

    private static Identifier identifierFromPath(Identifier location) {
        String path = location.getPath();
        path = path.substring("kits/".length(), path.length() - ".json".length());
        return new Identifier(location.getNamespace(), path);
    }

    @Nullable
    public static Kit get(Identifier identifier) {
        return KITS.get(identifier);
    }

    @Nullable
    public static Identifier getId(Kit kit) {
        return KITS.getIdentifier(kit);
    }

    public static TinyRegistry<Kit> getKITS() {
        return KITS;
    }
}

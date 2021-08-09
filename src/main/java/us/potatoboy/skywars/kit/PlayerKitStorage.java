package us.potatoboy.skywars.kit;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.storage.ServerStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerKitStorage implements ServerStorage {
    private final Map<UUID, Identifier> kits;

    public PlayerKitStorage() {
        this.kits = new HashMap<>();
    }

    public void putPlayerKit(UUID uuid, Identifier identifier) {
        kits.put(uuid, identifier);
    }

    public Identifier getPlayerKit(UUID uuid) {
        return this.kits.getOrDefault(uuid, null);
    }

    @Override
    public NbtCompound toTag() {
        var nbt = new NbtCompound();
        var kitsList = new NbtList();
        kits.forEach((uuid, identifier) -> {
            if (identifier != null) {
                var entryTag = new NbtCompound();
                entryTag.putUuid("UUID", uuid);
                entryTag.putString("Kit", identifier.toString());
                kitsList.add(entryTag);
            }
        });

        nbt.put("Kits", kitsList);

        return nbt;
    }

    @Override
    public void fromTag(NbtCompound compoundTag) {
        compoundTag.getList("Kits", 10).forEach(entry -> {
            var entryTag = (NbtCompound) entry;
            kits.put(entryTag.getUuid("UUID"), Identifier.tryParse(entryTag.getString("Kit")));
        });
    }
}

package us.potatoboy.skywars.kit;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.storage.ServerStorage;

import java.util.HashMap;
import java.util.List;
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
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        ListTag kitsTag = new ListTag();
        kits.forEach((uuid, identifier) -> {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUuid("UUID", uuid);
            entryTag.putString("Kit", identifier.toString());
            kitsTag.add(entryTag);
        });

        tag.put("Kits", kitsTag);

        return tag;
    }

    @Override
    public void fromTag(CompoundTag compoundTag) {
        compoundTag.getList("Kits", 10).forEach(entry -> {
            CompoundTag entryTag = (CompoundTag) entry;
            kits.put(entryTag.getUuid("UUID"), Identifier.tryParse(entryTag.getString("Kit")));
        });
    }
}

package us.potatoboy.skywars.kit;

import eu.pb4.playerdata.api.PlayerDataApi;
import eu.pb4.playerdata.api.storage.JsonDataStorage;
import eu.pb4.playerdata.api.storage.PlayerDataStorage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class PlayerKitStorage {
    public static final PlayerDataStorage<PlayerKitStorage> STORAGE = new JsonDataStorage<>("skywars_kits", PlayerKitStorage.class);
    public Identifier selectedKit = null;

    public static PlayerKitStorage get(ServerPlayerEntity player) {
        var data = PlayerDataApi.getCustomDataFor(player, STORAGE);
        if (data == null) {
            data = new PlayerKitStorage();
            PlayerDataApi.setCustomDataFor(player, STORAGE, data);
        }
        return data;
    }
}

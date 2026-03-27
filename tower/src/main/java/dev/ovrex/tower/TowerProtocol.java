package dev.ovrex.tower;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.ovrex.tower.enums.TowerTypeProtocol;
import dev.ovrex.tower.model.TowerServerInfo;

public class TowerProtocol {
    private static final Gson GSON = new Gson();


    public static JsonObject createAuthMessage(String login, String password) {
        final JsonObject json = new JsonObject();
        json.addProperty("type", TowerTypeProtocol.AUTH.getName());
        json.addProperty("login", login);
        json.addProperty("password", password);
        return json;
    }

    public static JsonObject createRegisterMessage(TowerServerInfo info) {
        final JsonObject json = new JsonObject();
        json.addProperty("type", TowerTypeProtocol.REGISTER.getName());
        json.addProperty("name", info.getName());
        json.addProperty("host", info.getHost());
        json.addProperty("port", info.getPort());
        json.addProperty("serverType", info.getType());
        json.addProperty("maxPlayers", info.getMaxPlayers());
        return json;
    }

    public static JsonObject createHeartbeat() {
        final JsonObject json = new JsonObject();
        json.addProperty("type", TowerTypeProtocol.HEARTBEAT.getName());
        json.addProperty("timestamp", System.currentTimeMillis());
        return json;
    }

    public static JsonObject createResponse(boolean success, String message) {
        final JsonObject json = new JsonObject();
        json.addProperty("type", TowerTypeProtocol.RESPONSE.getName());
        json.addProperty("success", success);
        json.addProperty("message", message);
        return json;
    }

    public static String serialize(JsonObject json) {
        return GSON.toJson(json);
    }

    public static JsonObject deserialize(String message) {
        return GSON.fromJson(message, JsonObject.class);
    }
}

package com.gaetanl.websocket.message;

import java.lang.reflect.Type;

import com.google.gson.*;

public class WsMessageDeserializer implements JsonDeserializer<WsMessage> {
    @Override
    public WsMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        String type = jsonObject.get("type").getAsString();

		try {
			Class<?> clazz = Class.forName(type);
	        return context.deserialize(jsonObject, clazz);
		}
		catch (ClassNotFoundException e) {
			throw new JsonParseException(e.getMessage());
		}
    }
}

package com.holmsted.gerrit.data.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.holmsted.gerrit.data.Identity;

import java.lang.reflect.Type;

/**
 * To reduce the size of JSON files this serializer will replace the Identity object with
 * the username where the Identity object can later be lookedup.
 */
public class IdentityUsernameSerializer implements JsonSerializer<Identity> {
    @Override
    public JsonElement serialize(Identity identity, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(identity.getUsername());
    }
}
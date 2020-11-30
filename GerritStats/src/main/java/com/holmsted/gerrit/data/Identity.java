package com.holmsted.gerrit.data;

import com.google.common.base.Strings;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Identity {
    public final String name;
    public final String email;
    public final String username;


    public Identity(@Nullable String name, @Nullable String email, @Nullable String username) {
        this.name = name;
        this.email = email;
        this.username = username;
    }


    public static Identity fromJson(JSONObject ownerJson) {
        return new Identity(ownerJson.optString("name"),
                ownerJson.optString("email"),
                ownerJson.optString("username"));
    }

    @Nonnull
    static List<Identity> fromJsonArray(@Nullable JSONArray identitiesJson) {
        List<Identity> result = new ArrayList<>();
        if (identitiesJson != null) {
            for (int i = 0; i < identitiesJson.length(); ++i) {
                JSONObject identityJson = identitiesJson.getJSONObject(i);
                result.add(Identity.fromJson(identityJson));
            }
        }
        return result;
    }


    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getIdentifier() {
        String identifier = username;
        if (Strings.isNullOrEmpty(identifier)) {
            identifier = Strings.nullToEmpty(email).replace(".", "_");
            int atMarkIndex = identifier.indexOf('@');
            if (atMarkIndex != -1) {
                identifier = identifier.substring(0, atMarkIndex);
            } else {
                identifier = "anonymous_coward";
            }
        }
        return identifier;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Identity)) {
            return false;
        }
        Identity otherIdentity = (Identity) other;
        return getIdentifier().equals(otherIdentity.getIdentifier());
    }

    public int compareTo(@Nonnull Identity other) {
        if (email != null && other.email != null) {
            return email.compareTo(other.email);
        } else if (username != null && other.username != null) {
            return username.compareTo(other.username);
        } else {
            return 0;
        }
    }

    @Override
    public int hashCode() {
        return getIdentifier().hashCode();
    }

    @Override
    public String toString() {
        if (email != null && !email.isEmpty()) {
            return email;
        } else if (name != null && !name.isEmpty()) {
            return name;
        } else if (username != null && !username.isEmpty()) {
            return username;
        } else {
            return super.toString();
        }
    }
}

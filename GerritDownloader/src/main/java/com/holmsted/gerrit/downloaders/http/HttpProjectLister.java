package com.holmsted.gerrit.downloaders.http;

import com.holmsted.gerrit.GerritServer;
import com.holmsted.gerrit.downloaders.GerritProjectLister;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Creates a listing of all Gerrit projects on the given server.
 */
public class HttpProjectLister extends GerritProjectLister {

    public HttpProjectLister(@Nonnull GerritServer gerritServer) {
        super(gerritServer);
    }

    @Nonnull
    public List<String> getProjectListing() {
        ArrayList<String> output = new ArrayList<>();
        try {
            JSONObject json = HttpUtils.createJSONGetRequest(getGerritServer().getServerAddress() + "/projects/");
            output.addAll(json.keySet());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return output;
    }
}

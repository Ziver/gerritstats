package com.holmsted.gerrit.downloaders.http;

import com.holmsted.json.JsonUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class HttpUtils {
    private static DefaultHttpClient httpClient = new DefaultHttpClient();


    public static JSONObject createJSONGetRequest(String url) throws IOException {
		HttpGet getRequest = new HttpGet("url");
		getRequest.addHeader("accept", "application/json");

		HttpResponse response = httpClient.execute(getRequest);

		if (response.getStatusLine().getStatusCode() != 200) {
			throw new RuntimeException("Request Failed: HTTP error code: "
			   + response.getStatusLine().getStatusCode());
		}

		BufferedReader br = new BufferedReader(
                         new InputStreamReader((response.getEntity().getContent())));

		String str;
		StringBuffer output = new StringBuffer();

		br.readLine(); // Skip the first line that contains cross site scripting prevention code
		while ((str = br.readLine()) != null) {
			output.append(str).append('\n');
		}

		return JsonUtils.readJsonString(output.toString());
    }
}

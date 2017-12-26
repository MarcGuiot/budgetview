package com.budgetview.shared.license;

import com.budgetview.shared.http.Http;
import org.apache.http.client.fluent.Request;
import org.json.JSONObject;

import java.io.IOException;

public class LicenseAPI {
  public JSONObject getDesktopVersion() throws IOException {
    String url = LicenseConstants.DESKTOP_VERSION;
    String serverUrl = LicenseConstants.getServerUrl(url);
    Request request = Request.Get(serverUrl);
    return Http.executeAndGetJson(serverUrl, request);
  }
}

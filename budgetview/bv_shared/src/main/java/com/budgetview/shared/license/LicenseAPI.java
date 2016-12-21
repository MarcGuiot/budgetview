package com.budgetview.shared.license;

import org.apache.http.client.fluent.Request;
import org.globsframework.json.JsonGlobFormat;
import org.globsframework.utils.Strings;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import static com.budgetview.shared.json.Json.json;

public class LicenseAPI {

  public static Date getCloudSubscriptionEndDate(String email) throws IOException, ParseException {
    String url = LicenseConstants.getServerUrl(LicenseConstants.CLOUD_SUBSCRIPTION_END_DATE);
    JSONObject result = json(Request.Get(url).addHeader(LicenseConstants.CLOUD_EMAIL, email), url);
    String expirationDate = result.optString(LicenseConstants.CLOUD_END_DATE);
    return Strings.isNullOrEmpty(expirationDate) ? null : JsonGlobFormat.parseDate(expirationDate);
  }
}

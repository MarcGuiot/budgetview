package com.budgetview.shared.cloud;

import com.budgetview.shared.http.Http;
import com.budgetview.shared.model.Provider;
import org.apache.http.client.fluent.Request;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.json.JSONObject;

import java.io.IOException;

public class CloudAPI {

  public JSONObject signup(String email, String lang) throws IOException {
    String url = "/user";
    String serverUrl = CloudConstants.getServerUrl(url);
    Request request = Request.Post(serverUrl)
      .addHeader(CloudConstants.EMAIL, email)
      .addHeader(CloudConstants.LANG, lang);
    return Http.executeAndGetJson(serverUrl, request);
  }

  public JSONObject validateSignup(String email, String validationCode) throws IOException {
    String url = "/user/validation";
    Request request = Request.Post(CloudConstants.getServerUrl(url))
      .addHeader(CloudConstants.EMAIL, email)
      .addHeader(CloudConstants.VALIDATION_CODE, validationCode);
    return Http.executeAndGetJson(url, request);
  }

  public JSONObject modifyEmailAddress(Integer cloudUserId, Integer deviceId, String deviceToken, String currentEmail, String newEmail) throws IOException {
    String url = "/user/email";
    String serverUrl = CloudConstants.getServerUrl(url);
    Request request = Request.Post(serverUrl)
      .addHeader(CloudConstants.CLOUD_USER_ID, Integer.toString(cloudUserId))
      .addHeader(CloudConstants.DEVICE_ID, Integer.toString(deviceId))
      .addHeader(CloudConstants.DEVICE_TOKEN, deviceToken)
      .addHeader(CloudConstants.EMAIL, currentEmail)
      .addHeader(CloudConstants.NEW_EMAIL, newEmail);
    return Http.executeAndGetJson(url, request);
  }

  public JSONObject validateEmailModification(Integer cloudUserId, Integer deviceId, String deviceToken, String newEmail, String validationCode) throws IOException {
    String url = "/user/email/validation";
    Request request = Request.Post(CloudConstants.getServerUrl(url))
      .addHeader(CloudConstants.CLOUD_USER_ID, Integer.toString(cloudUserId))
      .addHeader(CloudConstants.DEVICE_ID, Integer.toString(deviceId))
      .addHeader(CloudConstants.DEVICE_TOKEN, deviceToken)
      .addHeader(CloudConstants.NEW_EMAIL, newEmail)
      .addHeader(CloudConstants.VALIDATION_CODE, validationCode);
    return Http.executeAndGetJson(url, request);
  }

  public JSONObject getTemporaryBudgeaToken(Integer cloudUserId, Integer deviceId, String deviceToken) throws IOException {
    checkIdAndToken(cloudUserId, deviceId, deviceToken);
    if (Strings.isNullOrEmpty(deviceToken)) {
      throw new InvalidParameter("A proper token must be provided to get a token");
    }
    String url = CloudConstants.getServerUrl("/budgea/token");
    Request request = Request.Get(url)
      .addHeader(CloudConstants.CLOUD_USER_ID, Integer.toString(cloudUserId))
      .addHeader(CloudConstants.DEVICE_ID, Integer.toString(deviceId))
      .addHeader(CloudConstants.DEVICE_TOKEN, deviceToken);
    return Http.executeAndGetJson(url, request);
  }

  public boolean isProviderAccessRegistered(Integer cloudUserId, Integer deviceId, String deviceToken) throws IOException {
    checkIdAndToken(cloudUserId, deviceId, deviceToken);

    String url = cloudUrl("/provider/access");
    Request request = Request.Get(url)
      .addHeader(CloudConstants.CLOUD_USER_ID, Integer.toString(cloudUserId))
      .addHeader(CloudConstants.DEVICE_ID, Integer.toString(deviceId))
      .addHeader(CloudConstants.DEVICE_TOKEN, deviceToken)
      .addHeader(CloudConstants.PROVIDER_ID, Integer.toString(Provider.BUDGEA.getId()));
    JSONObject json = Http.executeAndGetJson(url, request);
    return Utils.equal("ok", json.optString("status"));
  }

  public void addProviderAccess(Integer cloudUserId, Integer deviceId, String deviceToken, String budgeaToken, Integer budgeaUserId) throws IOException {
    checkIdAndToken(cloudUserId, deviceId, deviceToken);
    if (Strings.isNullOrEmpty(budgeaToken)) {
      throw new InvalidParameter("A non-empty Budgea token must be provided to create the connection");
    }
    if (budgeaUserId == null) {
      throw new InvalidParameter("A budgea userId must be provided to create the connection");
    }

    String url = cloudUrl("/provider/access");
    Request request = Request.Post(url)
      .addHeader(CloudConstants.CLOUD_USER_ID, Integer.toString(cloudUserId))
      .addHeader(CloudConstants.DEVICE_ID, Integer.toString(deviceId))
      .addHeader(CloudConstants.DEVICE_TOKEN, deviceToken)
      .addHeader(CloudConstants.PROVIDER_ID, Integer.toString(Provider.BUDGEA.getId()))
      .addHeader(CloudConstants.PROVIDER_TOKEN, budgeaToken)
      .addHeader(CloudConstants.PROVIDER_USER_ID, Integer.toString(budgeaUserId));
    Http.execute(url, request);
  }

  public void addBankConnection(Integer cloudUserId, Integer deviceId, String deviceToken, int connectionId) throws IOException {
    checkIdAndToken(cloudUserId, deviceId, deviceToken);
    String url = "/banks/connections";
    Request request = Request.Post(cloudUrl(url))
      .addHeader(CloudConstants.CLOUD_USER_ID, Integer.toString(cloudUserId))
      .addHeader(CloudConstants.DEVICE_ID, Integer.toString(deviceId))
      .addHeader(CloudConstants.DEVICE_TOKEN, deviceToken)
      .addHeader(CloudConstants.PROVIDER_ID, Integer.toString(Provider.BUDGEA.getId()))
      .addHeader(CloudConstants.PROVIDER_CONNECTION_ID, Integer.toString(connectionId));
    Http.execute(url, request);
  }

  public JSONObject getBankConnections(Integer cloudUserId, Integer deviceId, String deviceToken) throws IOException {
    checkIdAndToken(cloudUserId, deviceId, deviceToken);
    String url = "/banks/connections";
    Request request = Request.Get(cloudUrl(url))
      .addHeader(CloudConstants.CLOUD_USER_ID, Integer.toString(cloudUserId))
      .addHeader(CloudConstants.DEVICE_ID, Integer.toString(deviceId))
      .addHeader(CloudConstants.DEVICE_TOKEN, deviceToken)
      .addHeader(CloudConstants.PROVIDER_ID, Integer.toString(Provider.BUDGEA.getId()));
    return Http.executeAndGetJson(url, request);
  }

  public JSONObject checkBankConnection(Integer cloudUserId, Integer deviceId, String deviceToken, int connectionId) throws IOException {
    checkIdAndToken(cloudUserId, deviceId, deviceToken);
    String url = "/banks/connections";
    Request request = Request.Get(cloudUrl(url))
      .addHeader(CloudConstants.CLOUD_USER_ID, Integer.toString(cloudUserId))
      .addHeader(CloudConstants.DEVICE_ID, Integer.toString(deviceId))
      .addHeader(CloudConstants.DEVICE_TOKEN, deviceToken)
      .addHeader(CloudConstants.PROVIDER_ID, Integer.toString(Provider.BUDGEA.getId()))
      .addHeader(CloudConstants.PROVIDER_CONNECTION_ID, Integer.toString(connectionId));
    return Http.executeAndGetJson(url, request);
  }

  public void deleteConnection(Integer cloudUserId, Integer deviceId, String deviceToken, Integer providerId, Integer providerConnectionId) throws IOException {
    checkIdAndToken(cloudUserId, deviceId, deviceToken);
    String url = "/banks/connections";
    Request request = Request.Delete(cloudUrl(url))
      .addHeader(CloudConstants.CLOUD_USER_ID, Integer.toString(cloudUserId))
      .addHeader(CloudConstants.DEVICE_ID, Integer.toString(deviceId))
      .addHeader(CloudConstants.DEVICE_TOKEN, deviceToken)
      .addHeader(CloudConstants.PROVIDER_ID, Integer.toString(providerId))
      .addHeader(CloudConstants.PROVIDER_CONNECTION_ID, Integer.toString(providerConnectionId));
    Http.execute(url, request);
  }

  public JSONObject getStatement(Integer cloudUserId, Integer deviceId, String deviceToken, Integer lastUpdate) throws IOException {
    checkIdAndToken(cloudUserId, deviceId, deviceToken);
    String url = lastUpdate == null ? "/statement" : "/statement/" + lastUpdate;
    Request request = Request.Get(cloudUrl(url))
      .addHeader(CloudConstants.CLOUD_USER_ID, Integer.toString(cloudUserId))
      .addHeader(CloudConstants.DEVICE_ID, Integer.toString(deviceId))
      .addHeader(CloudConstants.DEVICE_TOKEN, deviceToken);
    return Http.executeAndGetJson(url, request);
  }

  public void deleteCloudAccount(Integer cloudUserId, Integer deviceId, String deviceToken) throws IOException {
    checkIdAndToken(cloudUserId, deviceId, deviceToken);
    String url = "/user";
    Request request = Request.Delete(cloudUrl(url))
      .addHeader(CloudConstants.CLOUD_USER_ID, Integer.toString(cloudUserId))
      .addHeader(CloudConstants.DEVICE_ID, Integer.toString(deviceId))
      .addHeader(CloudConstants.DEVICE_TOKEN, deviceToken);
    Http.execute(url, request);
  }

  private String cloudUrl(String path) {
    return CloudConstants.getServerUrl(path);
  }

  public void checkIdAndToken(Integer cloudUserId, Integer deviceId, String deviceToken) {
    if (cloudUserId == null) {
      throw new InvalidParameter("cloudUserId must not be null");
    }
    if (deviceId == null) {
      throw new InvalidParameter("deviceId must not be null");
    }
    if (Strings.isNullOrEmpty(deviceToken)) {
      throw new InvalidParameter("A proper token must be provided");
    }
  }
}


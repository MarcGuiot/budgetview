package com.budgetview.shared.cloud;

import org.globsframework.utils.Utils;

public class CloudConstants {

  public static final int CURRENT_API_VERSION = 1;

  public static final String PROD_CLOUD_SERVER_URL = "https://register.mybudgetview.fr:1445";
  public static final String LOCAL_SERVER_URL = "http://127.0.0.1:8080";

  public static final String EMAIL = "email";
  public static final String NEW_EMAIL = "new_email";
  public static final String LANG = "lang";
  public static final String CLOUD_USER_ID = "cloud_id";
  public static final String DEVICE_ID = "device_id";
  public static final String DEVICE_TOKEN = "device_token";
  public static final String VALIDATION_CODE = "validation_code";
  public static final String STATUS = "status";
  public static final String API_VERSION = "api_version";

  public static final String SUBSCRIPTION_STATUS = "subscription_status";
  public static final String SUBSCRIPTION_END_DATE = "subscription_end_date";

  public static final String PROVIDER_ID = "provider_id";
  public static final String PROVIDER_USER_ID = "provider_user_id";
  public static final String PROVIDER_TOKEN = "provider_token";
  public static final String PROVIDER_TOKEN_REGISTERED = "provider_token_registered";
  public static final String PROVIDER_CONNECTION_ID = "provider_connection_id";
  public static final String PROVIDER_ACCOUNT_ID = "provider_account_id";
  public static final String PROVIDER_BANK_ID = "provider_bank_id";
  public static final String EXISTING_STATEMENTS = "existing_statements";

  public static final String NAME = "name";
  public static final String NUMBER = "number";
  public static final String ENABLED = "enabled";
  public static final String BANK_NAME = "bank_name";
  public static final String INITIALIZED = "initialized";
  public static final String PASSWORD_ERROR = "password_error";
  public static final String ACTION_NEEDED = "action_needed";


  public static final String APPNAME = "budgetview";
  public static final String CLOUD_URL_PROPERTY = APPNAME + ".sync.url";

  public static final String CLOUD_EMAIL = "email";
  public static final String CLOUD_END_DATE = "end_date";

  public static String getServerUrl(String path) {
    String url = PROD_CLOUD_SERVER_URL + path;
    Utils.beginRemove();
    url = System.getProperty(CLOUD_URL_PROPERTY, PROD_CLOUD_SERVER_URL) + path;
    Utils.endRemove();
    return url;
  }

  public static String getSubscriptionValidationUrl(String code) {
    return getServerUrl("/subscription/validation?code=" + code);
  }
}

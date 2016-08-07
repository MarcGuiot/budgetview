package com.budgetview.server.cloud;

import org.json.JSONObject;

public class UserDataService {
  public UserDataSet getDataSet(String budgeaToken) {
    return new DefaultUserDataSet();
  }

  private class DefaultUserDataSet implements UserDataSet {

    public void processBudgeaUpdate(JSONObject root) {

    }
  }
}

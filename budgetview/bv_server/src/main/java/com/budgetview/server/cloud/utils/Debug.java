package com.budgetview.server.cloud.utils;

import com.budgetview.server.cloud.model.CloudUser;
import org.globsframework.model.Glob;

public class Debug {
  public static boolean isTestUser(Glob user) {
    return user != null && "regis.medina@gmail.com".equals(user.get(CloudUser.EMAIL));
  }
}

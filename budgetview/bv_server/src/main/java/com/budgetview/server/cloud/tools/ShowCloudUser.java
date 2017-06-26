package com.budgetview.server.cloud.tools;

import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.server.utils.Args;
import org.globsframework.sqlstreams.constraints.Where;

public class ShowCloudUser {
  public static void main(String... args) throws Exception {
    System.out.println(dump(args));
  }

  public static String dump(String... args) throws Exception {
    String configFile = Args.toString(args, 0);
    String email = Args.toEmail(args, 1);
    if (configFile == null || email == null) {
      return "Usage: script <config_file> <email>";
    }
    return CloudUserDump.get(configFile, Where.fieldEquals(CloudUser.EMAIL, email)).toString();
  }
}

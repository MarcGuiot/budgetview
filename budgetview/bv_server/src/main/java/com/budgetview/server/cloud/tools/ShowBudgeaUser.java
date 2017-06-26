package com.budgetview.server.cloud.tools;

import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.server.utils.Args;
import org.globsframework.sqlstreams.constraints.Where;

public class ShowBudgeaUser {
  public static void main(String... args) throws Exception {
    System.out.println(dump(args));
  }

  public static String dump(String... args) throws Exception {
    String configFile = Args.toString(args, 0);
    Integer budgeaId = Args.toInt(args, 1);
    if (configFile == null || budgeaId == null) {
      return "Usage: script <config_file> <budgea_user_id>";
    }
    return CloudUserDump.get(configFile, Where.fieldEquals(CloudUser.PROVIDER_USER_ID, budgeaId)).toString();
  }
}

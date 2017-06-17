package com.budgetview.server.cloud.tools;

import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.server.utils.Args;
import org.globsframework.sqlstreams.constraints.Where;

public class ShowStripeUser {
  public static void main(String... args) throws Exception {
    System.out.println(dump(args));
  }

  public static String dump(String... args) throws Exception {
    String configFile = Args.toString(args, 0);
    String stripeCustomerId = Args.toString(args, 1);
    if (configFile == null || stripeCustomerId == null) {
      return "Usage: script <config_file> <stripe_customer_id>";
    }
    return ShowCloudUser.print(configFile, Where.fieldEquals(CloudUser.STRIPE_CUSTOMER_ID, stripeCustomerId));
  }
}

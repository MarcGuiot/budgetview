package com.budgetview.server.cloud.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class BudgeaTools {
  public static String getMasterToken(String[] args) throws Exception {
    if (args.length == 0) {
      System.out.println("Expecting the path of a file containing the Budgea master token");
      System.exit(-1);
    }

    File file = new File(args[0]);
    if (!file.exists()) {
      System.out.println(args[0] + " not found - expecting the path of a file containing the Budgea master token");
      System.exit(-1);
    }

    BufferedReader reader = new BufferedReader(new FileReader(file));
    String token = reader.readLine();
    reader.close();

    return token.trim();
  }
}

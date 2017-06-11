package com.budgetview.server.cloud.tools;

import com.budgetview.server.cloud.services.AuthenticationService;
import com.budgetview.server.cloud.utils.CloudDb;
import com.budgetview.server.config.ConfigService;
import com.budgetview.server.utils.Args;
import org.apache.log4j.Logger;
import org.globsframework.model.Glob;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.utils.Dates;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import java.io.IOException;
import java.util.Date;

public class AddUser {

  private static Logger logger = Logger.getLogger("AddUser");

  public static void main(String... args) throws IOException {

    String configFile = Args.toString(args, 0);
    String email = Args.toEmail(args, 1);
    Integer months = Args.toInt(args, 2);
    if (configFile == null || email == null || months == null) {
      System.out.println("Usage: script <config_file> <email> <#months>");
      return;
    }

    logger.info("Adding user " + email + " for " + months + " months...");

    Date endDate = Dates.monthsLater(months);

    ConfigService config = new ConfigService(args);
    Directory directory = new DefaultDirectory();
    directory.add(config);
    directory.add(GlobsDatabase.class, CloudDb.create(config));
    AuthenticationService authentication = new AuthenticationService(directory);
    Glob user = authentication.findUserWithEmail(email);
    if (user == null) {
      logger.info("User not found for '" + email + "', creating it");
      user = authentication.createUser(email, "fr");
    }
    authentication.setSubscriptionEndDate(user, endDate);

    logger.info("User updated. Last month: " + Dates.toString(endDate));
  }
}

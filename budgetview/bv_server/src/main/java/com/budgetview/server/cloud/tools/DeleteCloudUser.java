package com.budgetview.server.cloud.tools;

import com.budgetview.server.cloud.CloudServer;
import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.server.cloud.services.AuthenticationService;
import com.budgetview.server.cloud.services.UserService;
import com.budgetview.server.cloud.utils.CloudDb;
import com.budgetview.server.config.ConfigService;
import com.budgetview.server.utils.Args;
import org.globsframework.model.Glob;
import org.globsframework.sqlstreams.constraints.Where;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import java.util.Scanner;

public class DeleteCloudUser {

  public static void main(String... args) throws Exception {
    System.out.println(dump(args));
  }

  public static String dump(String... args) throws Exception {

    final StringBuilder builder = new StringBuilder();

    String configFile = Args.toString(args, 0);
    final String email = Args.toString(args, 1);
    String skipConfirm = Args.toString(args, 2);
    if (configFile == null || email == null) {
      builder.append("Usage: script <config_file> <email> [skipConfirm]").append("\n");
      return builder.toString();
    }

    boolean askConfirmation = true;
    if (skipConfirm != null) {
      if ("skipConfirm".equalsIgnoreCase(skipConfirm)) {
        askConfirmation = false;
      }
      else {
        builder.append("Invalid argument: ").append(skipConfirm).append("\n");
        return builder.toString();
      }
    }

    if (askConfirmation) {
      CloudUserDump cloudUserDump = CloudUserDump.get(configFile, Where.fieldEquals(CloudUser.EMAIL, email));
      if (!cloudUserDump.userFound()) {
        return "User not found";
      }
      System.out.println(cloudUserDump.toString());
      System.out.println("Are you sure you want to delete this user? (y/n)");
      Scanner input = new Scanner(System.in);
      String answer = input.next();
      if (!"y".equalsIgnoreCase(answer)) {
        builder.append("Deletion cancelled");
        return builder.toString();
      }
    }

    ConfigService config = new ConfigService(configFile);
    final Directory directory = CloudServer.createDirectory(config, CloudDb.create(config));
    final Glob user = directory.get(AuthenticationService.class).findUserWithEmail(email);
    if (user == null) {
      builder.append("Could not find user:").append(email).append("\n");
      return builder.toString();
    }

    final Integer userId = user.get(CloudUser.ID);
    boolean success = directory.get(UserService.class).deleteUser(user, new UserService.DeletionCallback() {
      public void processOk() {
        builder.append("Deleted account for user ").append(userId).append(" with email ").append(email).append("\n");
      }

      public void processError(String message, Exception e) {
        builder.append("Failed to delete user with email ").append(email).append("\n")
          .append("Message: ").append(message).append("\n")
          .append(Utils.toString(e)).append("\n");
      }
    });

    builder.append(success ? "Deletion completed" : "Deletion failed");

    return builder.toString();
  }
}

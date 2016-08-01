package com.budgetview.client;

import com.budgetview.model.User;
import com.budgetview.model.UserPreferences;
import org.globsframework.model.GlobRepository;

import javax.swing.*;
import java.io.IOException;

public class ConnectionStatus {
  public static void setOk(GlobRepository repository) {
    updateConnectedStatus(true, repository);
  }

  public static void setDisconnected(GlobRepository repository) {
    updateConnectedStatus(false, repository);
  }

  public static void checkException(GlobRepository repository, Exception e) {
    if (e instanceof IOException) {
      updateConnectedStatus(false, repository);
    }
  }

  private static void updateConnectedStatus(final boolean isConnected, GlobRepository repository) {
    if (repository.find(User.KEY) != null && !repository.get(User.KEY).get(User.CONNECTED)) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          if (isUserLogged(repository)) {
            repository.update(User.KEY, User.CONNECTED, isConnected);
          }
        }
      });
    }
  }

  private static boolean isUserLogged(GlobRepository repository) {
    return repository.find(UserPreferences.KEY) != null;
  }

}

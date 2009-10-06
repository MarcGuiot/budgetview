package org.designup.picsou.gui.utils;

import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Log;
import org.globsframework.utils.directory.Directory;

public class ExceptionHandler {
  private static GlobRepository repository;
  private static Directory directory;

  public void handle(Throwable exception) {
    Log.write("Exception thrown: ", exception);
    if (repository != null) {
      DataCheckerAction action = new DataCheckerAction(repository, directory);
      action.actionPerformed(null);
    }
  }

  public static void setRepository(GlobRepository repository, Directory directory) {
    ExceptionHandler.repository = repository;
    ExceptionHandler.directory = directory;
  }

  public static void registerHandler() {
    System.setProperty("sun.awt.exception.handler", ExceptionHandler.class.getName());
  }
}

package com.budgetview.desktop.utils;

import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Log;
import org.globsframework.utils.directory.Directory;

// attention cette classe est référencée dans picsou_exe_gen.jsmooth


public class AwtExceptionHandler {
  private static GlobRepository repository;
  private static Directory directory;

  public AwtExceptionHandler() {
  }

  public void handle(Throwable exception) {
    Log.write("Exception thrown: ", exception);
    if (repository != null) {
      DataCheckerAction action = new DataCheckerAction(repository, directory);
      action.check(exception);
    }
  }

  public static void setRepository(GlobRepository repository, Directory directory) {
    AwtExceptionHandler.repository = repository;
    AwtExceptionHandler.directory = directory;
  }

  public static void registerHandler() {
    System.setProperty("sun.awt.exception.handler", AwtExceptionHandler.class.getName());
  }
}

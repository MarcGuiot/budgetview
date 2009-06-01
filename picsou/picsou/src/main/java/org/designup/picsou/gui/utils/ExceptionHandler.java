package org.designup.picsou.gui.utils;

import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Log;

public class ExceptionHandler {
  private static GlobRepository repository;


  public void handle(Throwable exception) {
    Log.write("exception catch : ", exception);
    if (repository != null) {
      DataCheckerAction action = new DataCheckerAction(repository);
      action.actionPerformed(null);
    }
  }

  public static void setRepository(GlobRepository repository) {
    ExceptionHandler.repository = repository;
  }

  public static void registerHandler() {
    System.setProperty("sun.awt.exception.handler", ExceptionHandler.class.getName());
  }
}

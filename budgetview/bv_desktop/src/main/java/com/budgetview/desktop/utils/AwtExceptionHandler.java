package com.budgetview.desktop.utils;

import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Log;
import org.globsframework.utils.directory.Directory;

import java.awt.EventQueue;

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

    // La propriété sun.awt.exception.handler n'est plus lue depuis Java 7 : sans ce relais, une exception
    // levée dans l'IHM n'ouvrait plus le contrôle des données. On ne traite que le fil de l'IHM (EDT),
    // comme le faisait la propriété ; les autres fils gardent le comportement par défaut.
    final Thread.UncaughtExceptionHandler previous = Thread.getDefaultUncaughtExceptionHandler();
    Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
      public void uncaughtException(Thread thread, Throwable exception) {
        if (EventQueue.isDispatchThread()) {
          new AwtExceptionHandler().handle(exception);
        }
        else if (previous != null) {
          previous.uncaughtException(thread, exception);
        }
        else {
          System.err.print("Exception in thread \"" + thread.getName() + "\" ");
          exception.printStackTrace(System.err);
        }
      }
    });
  }
}

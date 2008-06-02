package org.crossbowlabs.globs.utils.logging;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.GlobRepository;

public class Debug {

  private static HtmlLogger logger;
  private static boolean enabled = false;

  public static void setEnabled(boolean enabled) {
    Debug.enabled = enabled;
  }

  public static void print(String html) {
    if (!enabled) {
      return;
    }
    getLogger().writeBlock(html);
  }

  public static void enter(String htmlTitle) {
    if (!enabled) {
      return;
    }
    getLogger().startBlock(htmlTitle);
  }

  public static void exit() {
    if (!enabled) {
      return;
    }
    getLogger().endBlock();
  }

  public static void print(GlobRepository repository, GlobType... types) {
    if (!enabled) {
      return;
    }
    print(null, repository, types);
  }

  public static void print(String message, GlobRepository repository, GlobType... types) {
    if (!enabled) {
      return;
    }
    HtmlRepositoryPrinter repositoryPrinter = new HtmlRepositoryPrinter(message, getLogger(), repository,
                                                                        types);
    repositoryPrinter.run();
  }

  private static HtmlLogger getLogger() {
    if (logger == null) {
      logger = new HtmlLogger();
    }
    return logger;
  }

  public static void printChanges(GlobRepository repository) {
    if (!enabled) {
      return;
    }
    new HtmlChangeSetPrinter(getLogger(), repository);
  }

  public static void printFile(String fileName) {
    if (!enabled) {
      return;
    }
    getLogger().writeFile(fileName);
  }
}


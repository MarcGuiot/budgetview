package org.globsframework.utils.logging;

import org.globsframework.metamodel.GlobType;
import org.globsframework.model.GlobRepository;

public class HtmlTracker {
  public static void install(GlobRepository repository, GlobType... trackedTypes) {
    HtmlLogger logger = new HtmlLogger();
    HtmlChangeSetPrinter printer = new HtmlChangeSetPrinter(logger, repository, trackedTypes);
  }
}

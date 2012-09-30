package com.budgetview.android;

import org.globsframework.model.GlobRepository;
import org.globsframework.model.repository.DefaultGlobRepository;
import org.globsframework.model.repository.GlobIdGenerator;

public class App {

  private static GlobRepository repository;

  public static GlobRepository getRepository() {
    if (repository == null) {
      repository = new DefaultGlobRepository();
    }
    return repository;
  }
}

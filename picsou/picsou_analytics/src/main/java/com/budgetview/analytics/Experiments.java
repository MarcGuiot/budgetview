package com.budgetview.analytics;

import com.budgetview.analytics.model.Experiment;
import org.globsframework.model.GlobRepository;

import static org.globsframework.model.FieldValue.value;

public class Experiments {

  private GlobRepository repository;

  public Experiments(GlobRepository repository) {
    this.repository = repository;
  }

  public void register() {
    add(201209, "2.24: version EN relookee sur plusieurs sites");
    add(201205, "2.23: corrections dates + aide interne import + impression");
    add(201202, "Refonte site /support");
    add(201149, "Enregistrement toocharger");
    add(201148, "Mailing Franck");
    add(201147, "Enregistrement 01net");
    add(201146, "Article Olivier Lobet");
  }

  private void add(int weekId, String action) {
    repository.create(Experiment.TYPE,
                      value(Experiment.WEEK, weekId),
                      value(Experiment.ACTION, action));
  }
}

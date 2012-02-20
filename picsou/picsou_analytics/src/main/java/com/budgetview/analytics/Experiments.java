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
    add(201205, "2.23: corrections dates + aide interne import + impression", "");
    add(201202, "Refonte site /support", "Nécessité aide");
    add(201149, "Enregistrement toocharger", "Efficacité sites download");
    add(201148, "Mailing Franck", "");
    add(201147, "Enregistrement 01net", "Efficacité sites download");
    add(201146, "Article Olivier Lobet", "Manque références indépendantes");
  }

  private void add(int weekId, String action, String cause) {
    repository.create(Experiment.TYPE,
                      value(Experiment.WEEK, weekId),
                      value(Experiment.CAUSE, cause),
                      value(Experiment.ACTION, action));
  }
}

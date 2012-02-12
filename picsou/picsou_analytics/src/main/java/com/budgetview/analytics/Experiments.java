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
    add(201202, "Nécessité aide", "Refonte site /support");
    add(201149, "Efficacité sites download", "Enregistrement toocharger");
    add(201148, "", "Mailing Franck");
    add(201147, "Efficacité sites download", "Enregistrement 01net");
    add(201146, "Manque références indépendantes", "Article Olivier Lobet");
  }

  private void add(int weekId, String cause, String action) {
    repository.create(Experiment.TYPE,
                      value(Experiment.WEEK, weekId),
                      value(Experiment.CAUSE, cause),
                      value(Experiment.ACTION, action));
  }
}

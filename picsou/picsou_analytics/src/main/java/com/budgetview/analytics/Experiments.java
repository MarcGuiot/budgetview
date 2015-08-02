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
    add(201519, "4.01: Minor fixes (incl. La Banque Postale)");
    add(201519, "Facebook ad");
    add(201516, "4.0: Freemium + UI revamping + 19€90");
    add(201440, "3.14.4: Multi-accounts + Envelope groups");
    add(201430, "3.14: Multi-accounts (strict + envelopes duplication)");
    add(201401, "3.13: Minor UI improvements");
  }

  private void add(int weekId, String action) {
    repository.create(Experiment.TYPE,
                      value(Experiment.WEEK, weekId),
                      value(Experiment.ACTION, action));
  }
}

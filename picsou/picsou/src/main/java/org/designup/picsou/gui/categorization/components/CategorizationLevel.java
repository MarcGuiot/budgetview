package org.designup.picsou.gui.categorization.components;

import org.globsframework.model.*;
import static org.globsframework.model.utils.GlobMatchers.not;
import static org.globsframework.model.utils.GlobMatchers.isTrue;
import org.globsframework.metamodel.GlobType;
import org.globsframework.utils.Updatable;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.UserPreferences;
import org.designup.picsou.model.Series;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;

public class CategorizationLevel implements ChangeSetListener {

  private List<Updatable> listeners = new ArrayList<Updatable>();
  private GlobRepository repository;
  private double total;
  private double percentage;

  public CategorizationLevel(GlobRepository repository) {
    this.repository = repository;
    this.repository.addChangeListener(this);
  }

  public void addListener(Updatable updatable) {
    listeners.add(updatable);
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(Transaction.TYPE) || changeSet.containsChanges(UserPreferences.KEY)) {
      update();
    }
  }

  public double getTotal() {
    return total;
  }

  public double getPercentage() {
    return percentage;
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(Transaction.TYPE) || changedTypes.contains(UserPreferences.TYPE)) {
      update();
    }
  }

  private void update() {
    GlobList transactions =
      repository.getAll(Transaction.TYPE, not(isTrue(Transaction.PLANNED)));

    total = 0;
    double uncategorized = 0;
    for (Glob transaction : transactions) {
      double amount = Math.abs(transaction.get(Transaction.AMOUNT));
      total += amount;
      if (Series.UNCATEGORIZED_SERIES_ID.equals(transaction.get(Transaction.SERIES))) {
        uncategorized += amount;
      }
    }

    percentage = total == 0 ? 1 : uncategorized / total;
    if (percentage > 0 && percentage < 0.01) {
      percentage = 0.01;
    }

    updateAll();
  }

  private void updateAll() {
    for (Updatable updatable : listeners) {
      updatable.update();
    }
  }
}

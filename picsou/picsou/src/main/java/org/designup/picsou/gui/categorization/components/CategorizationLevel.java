package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.model.*;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Updatable;
import org.globsframework.utils.directory.Directory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.*;

public class CategorizationLevel implements ChangeSetListener {

  private List<Updatable> listeners = new ArrayList<Updatable>();
  private GlobRepository repository;
  private double total;
  private double percentage;
  private Set<Integer> selectedMonths = Collections.emptySet();
  private boolean filterOnCurrentMonth;
  private boolean hasNoTransactions;

  public CategorizationLevel(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.repository.addChangeListener(this);
    directory.get(SelectionService.class).addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        selectedMonths = selection.getAll(Month.TYPE).getValueSet(Month.ID);
        update();
      }
    }, Month.TYPE);
  }

  public void setFilterOnCurrentMonth() {
    this.filterOnCurrentMonth = true;
  }

  public void addListener(Updatable updatable) {
    listeners.add(updatable);
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(Transaction.TYPE) || changeSet.containsChanges(UserPreferences.KEY)
        || changeSet.containsChanges(SignpostStatus.TYPE)) {
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
    if (changedTypes.contains(Transaction.TYPE) || changedTypes.contains(UserPreferences.TYPE)
        || changedTypes.contains(SignpostStatus.TYPE)) {
      update();
    }
  }

  private void update() {
    GlobMatcher monthFilter =
      filterOnCurrentMonth ? GlobMatchers.fieldIn(Transaction.MONTH, selectedMonths) : GlobMatchers.ALL;
    GlobList transactions =
      repository.getAll(Transaction.TYPE, and(not(isTrue(Transaction.PLANNED)), monthFilter));

    hasNoTransactions = transactions.isEmpty();

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

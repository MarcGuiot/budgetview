package com.budgetview.gui.categorization.components;

import com.budgetview.gui.categorization.utils.Uncategorized;
import com.budgetview.model.SignpostStatus;
import com.budgetview.model.Month;
import com.budgetview.model.Transaction;
import com.budgetview.model.UserPreferences;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Updatable;
import org.globsframework.utils.directory.Directory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class CategorizationLevel implements ChangeSetListener {

  private List<Updatable> listeners = new ArrayList<Updatable>();
  private GlobRepository repository;
  private double total;
  private double percentage;
  private Set<Integer> selectedMonths = Collections.emptySet();
  private boolean filterOnCurrentMonth;

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
    if (changedTypes.contains(Transaction.TYPE) ||
        changedTypes.contains(UserPreferences.TYPE) ||
        changedTypes.contains(SignpostStatus.TYPE)) {
      update();
    }
  }

  private void update() {
    Uncategorized.Level level = Uncategorized.getLevel(selectedMonths, filterOnCurrentMonth, repository);
    this.total = level.total;
    percentage = total == 0 ? 1 : level.uncategorized / total;
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

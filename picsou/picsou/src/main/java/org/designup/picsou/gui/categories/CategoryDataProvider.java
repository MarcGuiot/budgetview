package org.designup.picsou.gui.categories;

import org.crossbowlabs.globs.gui.GlobSelection;
import org.crossbowlabs.globs.gui.GlobSelectionListener;
import org.crossbowlabs.globs.gui.SelectionService;
import org.crossbowlabs.globs.gui.views.GlobTableView;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.*;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.designup.picsou.gui.model.MonthStat;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.Month;

import java.util.*;

public class CategoryDataProvider implements ChangeSetListener, GlobSelectionListener {

  private GlobTableView view;
  private GlobRepository repository;
  private Set<Integer> currentMonths = Collections.emptySet();
  private Map<Integer, Double> categoryAmounts = new HashMap<Integer, Double>();
  private Map<Integer, Double> categoryDispensabilities = new HashMap<Integer, Double>();
  private Integer currentAccountId = Account.SUMMARY_ACCOUNT_ID;

  public CategoryDataProvider(GlobRepository repository, Directory directory) {
    this.repository = repository;
    initValues();
    repository.addChangeListener(this);
    directory.get(SelectionService.class).addListener(this, Month.TYPE, Account.TYPE);
  }

  private void initValues() {
    for (Glob category : repository.getAll(Category.TYPE)) {
      Integer categoryId = category.get(Category.ID);
      categoryAmounts.put(categoryId, 0.0);
      categoryDispensabilities.put(categoryId, 0.0);
    }
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(MonthStat.TYPE)) {
      update();
    }
  }

  public void globsReset(GlobRepository repository, List<GlobType> changedTypes) {
    update();
  }

  public void selectionUpdated(GlobSelection selection) {
    if (selection.isRelevantForType(Month.TYPE)) {
      currentMonths = selection.getAll(Month.TYPE).getValueSet(Month.ID);
    }
    if (selection.isRelevantForType(Account.TYPE)) {
      GlobList accounts = selection.getAll(Account.TYPE);
      if (accounts.size() == 1) {
        currentAccountId = accounts.get(0).get(Account.ID);
      }
      else {
        currentAccountId = null;
      }
    }
    update();
  }

  private void update() {
    for (Glob category : repository.getAll(Category.TYPE)) {
      int categoryId = category.get(Category.ID);
      double amount = 0.0;
      double dispensability = 0.0;
      if (currentAccountId != null) {
        for (Integer month : currentMonths) {
          Key key = MonthStat.getKey(month, categoryId, currentAccountId);
          Glob stat = repository.get(key);
          if (categoryId == Category.NONE) {
            amount += stat.get(MonthStat.INCOME) + stat.get(MonthStat.EXPENSES);
          }
          else {
            amount += stat.get(MonthStat.INCOME) - stat.get(MonthStat.EXPENSES);
          }

          dispensability += stat.get(MonthStat.DISPENSABLE);
        }
      }
      categoryAmounts.put(categoryId, amount);
      categoryDispensabilities.put(categoryId, dispensability);
    }
    if (view != null) {
      view.refresh();
    }
  }

  public void setView(GlobTableView view) {
    this.view = view;
  }

  public double getAmount(Integer categoryId) {
    return categoryAmounts.get(categoryId);
  }

  public double getDispensability(Integer categoryId) {
    return categoryDispensabilities.get(categoryId);
  }
}

package org.designup.picsou.gui.time;

import org.crossbowlabs.globs.gui.GlobSelection;
import org.crossbowlabs.globs.gui.GlobSelectionListener;
import org.crossbowlabs.globs.gui.SelectionService;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.*;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.designup.picsou.gui.model.MonthStat;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.Month;

import javax.swing.*;
import java.util.*;

public class MonthDataProvider implements ChangeSetListener, GlobSelectionListener {

  private JTable table;
  private GlobRepository repository;
  private Map<Integer, MonthData> monthData = new HashMap<Integer, MonthData>();
  private Set<Integer> categoryIds = Collections.singleton(Category.ALL);
  private Integer currentAccountId = Account.SUMMARY_ACCOUNT_ID;

  public MonthDataProvider(JTable table, GlobRepository repository, Directory directory) {
    this.table = table;
    this.repository = repository;
    repository.addChangeListener(this);
    directory.get(SelectionService.class).addListener(this, Category.TYPE, Account.TYPE);
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository globRepository) {
    if (changeSet.containsChanges(MonthStat.TYPE)) {
      update();
    }
  }

  public void globsReset(GlobRepository globRepository, List<GlobType> changedTypes) {
    update();
  }

  public void selectionUpdated(GlobSelection selection) {
    if (selection.isRelevantForType(Category.TYPE)) {
      categoryIds = selection.getAll(Category.TYPE).getValueSet(Category.ID);
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
    monthData.clear();
    if (currentAccountId != null) {
      for (Integer month : repository.getAll(Month.TYPE).getValueSet(Month.ID)) {
        MonthData data = new MonthData();
        for (Integer categoryId : categoryIds) {
          Glob stat = repository.get(MonthStat.getKey(month, categoryId, currentAccountId));
          data.income += stat.get(MonthStat.INCOME);
          data.expenses += stat.get(MonthStat.EXPENSES);
        }
        monthData.put(month, data);
      }
    }
    table.repaint();
  }

  public Double getIncome(int month) {
    if (!monthData.containsKey(month)) {
      return null;
    }
    return monthData.get(month).income;
  }

  public Double getExpenses(int month) {
    if (!monthData.containsKey(month)) {
      return null;
    }
    return monthData.get(month).expenses;
  }

  private static class MonthData {
    public double income;
    public double expenses;

    public String toString() {
      return income + "/" + expenses;
    }
  }
}

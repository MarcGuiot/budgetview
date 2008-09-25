package org.designup.picsou.gui.monthsummary;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.description.PicsouDescriptionService;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

public class BalanceSummaryView extends View implements GlobSelectionListener {
  private SelectionService parentSelectionService;
  private JLabel balance;
  private JLabel income;
  private JLabel fixe;
  private JLabel total;
  private JLabel savings;
  private JLabel projects;
  private JPanel contentPanel;

  public BalanceSummaryView(GlobRepository repository, Directory parentDirectory) {
    super(repository, createDirectory(parentDirectory));
    parentSelectionService = parentDirectory.get(SelectionService.class);
    parentSelectionService.addListener(this, Month.TYPE);
    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(SeriesStat.TYPE) || changeSet.containsUpdates(Transaction.BALANCE)) {
          updateDetails();
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
      }
    });
  }

  private static Directory createDirectory(Directory parentDirectory) {
    Directory directory = new DefaultDirectory(parentDirectory);
    directory.add(new SelectionService());
    return directory;
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/balanceSummaryView.splits", repository, directory);

    total = builder.add("totalLabel", new JLabel());
    balance = builder.add("balanceLabel", new JLabel());
    income = builder.add("incomeLabel", new JLabel());
    fixe = builder.add("fixedLabel", new JLabel());
    savings = builder.add("savingsLabel", new JLabel());
    projects = builder.add("projectsLabel", new JLabel());
    contentPanel = builder.add("content", new JPanel());
    contentPanel.setVisible(false);

    parentBuilder.add("balanceSummaryView", builder);
  }

  public void selectionUpdated(GlobSelection selection) {
    updateDetails();
  }

  private void updateDetails() {
    SortedSet<Integer> currentMonths = parentSelectionService.getSelection(Month.TYPE).getSortedSet(Month.ID);
    Glob[] transactions = getSortedTransactions(currentMonths);
    if (transactions.length == 0) {
      total.setText("");
      contentPanel.setVisible(false);
      return;
    }
    Double balanceAmount = null;
    int i;
    for (i = transactions.length - 1; i >= 0; i--) {
      Glob transaction = transactions[i];
      if (transaction.get(Transaction.PLANNED)) {
        continue;
      }
      balanceAmount = transaction.get(Transaction.BALANCE);
      if (balanceAmount != null) {
        break;
      }
    }
    if (balanceAmount == null) {
      return;
    }
    String label = PicsouDescriptionService.toString(balanceAmount);
    balance.setText(label);
    GlobList allSeries = repository.getAll(Series.TYPE);
    Set<Integer> incomeSeries = new HashSet<Integer>();
    Set<Integer> fixeSeries = new HashSet<Integer>();
    Set<Integer> savingsSeries = new HashSet<Integer>();
    Set<Integer> projectsSeries = new HashSet<Integer>();

    for (Glob series : allSeries) {
      if (BudgetArea.INCOME.getId().equals(series.get(Series.BUDGET_AREA))) {
        incomeSeries.add(series.get(Series.ID));
      }
      else if (BudgetArea.RECURRING_EXPENSES.getId().equals(series.get(Series.BUDGET_AREA))) {
        fixeSeries.add(series.get(Series.ID));
      }
      else if (BudgetArea.SAVINGS.getId().equals(series.get(Series.BUDGET_AREA))) {
        savingsSeries.add(series.get(Series.ID));
      }
      else if (BudgetArea.PROJECTS.getId().equals(series.get(Series.BUDGET_AREA))) {
        projectsSeries.add(series.get(Series.ID));
      }
    }
    double incomeAmount = 0;
    double fixedAmount = 0;
    double savingsAmount = 0;
    double projectsAmount = 0;
    for (; i < transactions.length; i++) {
      Glob transaction = transactions[i];
      if (transaction.get(Transaction.PLANNED)) {
        Integer transactionSeries = transaction.get(Transaction.SERIES);
        if (incomeSeries.contains(transactionSeries)) {
          incomeAmount += transaction.get(Transaction.AMOUNT);
        }
        if (fixeSeries.contains(transactionSeries)) {
          fixedAmount += transaction.get(Transaction.AMOUNT);
        }
        if (savingsSeries.contains(transactionSeries)) {
          savingsAmount += transaction.get(Transaction.AMOUNT);
        }
        if (projectsSeries.contains(transactionSeries)) {
          projectsAmount += transaction.get(Transaction.AMOUNT);
        }
      }
    }

    double totalAmount = balanceAmount + incomeAmount + fixedAmount + savingsAmount + projectsAmount;
    
    income.setText(PicsouDescriptionService.toString(incomeAmount));
    fixe.setText(PicsouDescriptionService.toString(fixedAmount));
    savings.setText(PicsouDescriptionService.toString(savingsAmount));
    projects.setText(PicsouDescriptionService.toString(projectsAmount));
    total.setText(PicsouDescriptionService.toString(totalAmount));
    contentPanel.setVisible(true);
  }

  private Glob[] getSortedTransactions(SortedSet<Integer> currentMonths) {
    SortedSet<Glob> tmp = repository.getSorted(Transaction.TYPE, TransactionComparator.ASCENDING_BANK,
                                               GlobMatchers.fieldIn(Transaction.MONTH, currentMonths));
    return tmp.toArray(new Glob[tmp.size()]);
  }
}

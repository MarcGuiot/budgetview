package org.designup.picsou.gui.projects;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.components.DefaultTableCellPainter;
import org.designup.picsou.gui.components.PicsouTableHeaderPainter;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.description.MonthYearStringifier;
import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.gui.model.SavingsBudgetStat;
import org.designup.picsou.gui.utils.AmountColors;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.*;
import org.designup.picsou.model.util.Amounts;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.gui.views.LabelCustomizer;
import static org.globsframework.gui.views.utils.LabelCustomizers.*;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.format.utils.AbstractGlobStringifier;
import org.globsframework.model.utils.*;
import org.globsframework.utils.CompositeComparator;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.SortedSet;

public class NextProjectsView extends View implements GlobSelectionListener {
  private GlobTableView tableView;
  private Integer currentMonthId;
  private GlobStringifier budgetStatStringifier;
  private GlobStringifier savingsBudgetStatStringifier;
  private AmountColors amountColors;

  public static final int NAME_COLUMN_INDEX = 1;
  private static final int[] COLUMN_SIZES = {9, 25};

  public NextProjectsView(GlobRepository repository, Directory directory) {
    super(repository, directory);
    selectionService.addListener(this, Month.TYPE);
    budgetStatStringifier = descriptionService.getStringifier(BudgetStat.END_OF_MONTH_ACCOUNT_POSITION);
    savingsBudgetStatStringifier = descriptionService.getStringifier(SavingsBudgetStat.END_OF_MONTH_POSITION);
    amountColors = new AmountColors(directory);
  }

  public void registerComponents(GlobsPanelBuilder builder) {

    tableView = GlobTableView.init(SeriesBudget.TYPE,
                                   repository,
                                   new CompositeComparator<Glob>(
                                     new GlobFieldComparator(SeriesBudget.MONTH),
                                     descriptionService.getStringifier(SeriesBudget.SERIES).getComparator(repository)),
                                   directory);
    tableView.setDefaultFont(Gui.DEFAULT_TABLE_FONT);
    tableView.setDefaultBackgroundPainter(new DefaultTableCellPainter(directory));
    tableView.setFilter(GlobMatchers.NONE);
    repository.addChangeListener(new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(Series.TYPE)) {
          tableView.refresh();
        }
      }
    });

    PicsouTableHeaderPainter.install(tableView, directory);

    tableView.addColumn(Lang.get("month"), new MonthYearStringifier(SeriesBudget.MONTH),
                        fontSize(9));
    tableView.addColumn(new ProjectNameColumn(tableView, descriptionService, repository, directory));
    tableView.addColumn(Lang.get("amount"), SeriesBudget.AMOUNT);

    LabelCustomizer positionCustomizer = chain(fontSize(9), ALIGN_RIGHT);
    final MainAccountsPositionStringifier mainAccountsPosition = new MainAccountsPositionStringifier();
    tableView.addColumn(Lang.get("nextprojects.main.position"),
                        mainAccountsPosition, chain(positionCustomizer, mainAccountsPosition));
    final SavingsAccountsPositionStringifier savingsAccountsStringifier = new SavingsAccountsPositionStringifier();
    tableView.addColumn(Lang.get("nextprojects.savings.position"),
                        savingsAccountsStringifier, chain(positionCustomizer, savingsAccountsStringifier));
    tableView.addColumn(Lang.get("nextprojects.total.position"), new TotalAccountsPositionStringifier(),
                        positionCustomizer);

    final JTable table = tableView.getComponent();
    Gui.setColumnSizes(table, COLUMN_SIZES);
    Gui.installRolloverOnButtons(table, NAME_COLUMN_INDEX);

    builder.add("nextProjects", tableView);
  }

  public void selectionUpdated(GlobSelection selection) {
    final SortedSet<Integer> monthIds = selection.getAll(Month.TYPE).getSortedSet(Month.ID);
    if (monthIds.isEmpty()) {
      currentMonthId = null;
      tableView.setFilter(GlobMatchers.NONE);
      return;
    }

    currentMonthId = monthIds.first();
    tableView.setFilter(new GlobMatcher() {
      public boolean matches(Glob seriesBudget, GlobRepository repository) {
        if ((seriesBudget.get(SeriesBudget.MONTH) < currentMonthId) ||
            Amounts.isNullOrZero(seriesBudget.get(SeriesBudget.AMOUNT))) {
          return false;
        }
        Glob series = repository.get(Key.create(Series.TYPE, seriesBudget.get(SeriesBudget.SERIES)));
        return BudgetArea.SPECIAL.getId().equals(series.get(Series.BUDGET_AREA));
      }
    });
  }

  private class MainAccountsPositionStringifier extends AbstractGlobStringifier implements LabelCustomizer {
    public String toString(Glob seriesBudget, GlobRepository repository) {
      Glob budgetStat = getBudgetStat(seriesBudget, repository);
      return budgetStatStringifier.toString(budgetStat, repository);
    }

    public void process(JLabel label, Glob seriesBudget, boolean isSelected, boolean hasFocus, int row, int column) {
      Glob budgetStat = getBudgetStat(seriesBudget, repository);
      final Double position = budgetStat.get(BudgetStat.END_OF_MONTH_ACCOUNT_POSITION);
      final Double threshold = AccountPositionThreshold.getValue(repository);
      if ((position == null) || (threshold == null)) {
        return;
      }
      label.setForeground(amountColors.getTextColor(position - threshold));
    }

    private Glob getBudgetStat(Glob seriesBudget, GlobRepository repository) {
      Integer monthId = seriesBudget.get(SeriesBudget.MONTH);
      return repository.get(Key.create(BudgetStat.TYPE, monthId));
    }
  }

  private class SavingsAccountsPositionStringifier extends AbstractGlobStringifier implements LabelCustomizer {
    public String toString(Glob seriesBudget, GlobRepository repository) {
      Glob budgetStat = getBudgetStat(seriesBudget, repository);
      if (budgetStat == null) {
        return "";
      }
      return savingsBudgetStatStringifier.toString(budgetStat, repository);
    }

    public void process(JLabel label, Glob seriesBudget, boolean isSelected, boolean hasFocus, int row, int column) {
      Glob budgetStat = getBudgetStat(seriesBudget, repository);
      if (budgetStat == null) {
        return;
      }
      Double position = budgetStat.get(SavingsBudgetStat.END_OF_MONTH_POSITION);
      if (position == null) {
        return;
      }
      label.setForeground(amountColors.getTextColor(position));
    }

    private Glob getBudgetStat(Glob seriesBudget, GlobRepository repository) {
      return SavingsBudgetStat.findSummary(seriesBudget.get(SeriesBudget.MONTH), repository);
    }
  }

  private class TotalAccountsPositionStringifier extends AbstractGlobStringifier {
    public String toString(Glob seriesBudget, GlobRepository repository) {
      Integer monthId = seriesBudget.get(SeriesBudget.MONTH);
      Glob budgetStat = repository.get(Key.create(BudgetStat.TYPE, monthId));
      Glob savingsBudgetStat = SavingsBudgetStat.findSummary(monthId, repository);
      Double mainPosition = GlobUtils.safeGet(budgetStat, BudgetStat.END_OF_MONTH_ACCOUNT_POSITION);
      Double savingsPosition = GlobUtils.safeGet(savingsBudgetStat, SavingsBudgetStat.END_OF_MONTH_POSITION);
      if (mainPosition == null && savingsPosition == null) {
        return "";
      }
      double total = 0;
      if (mainPosition != null) {
        total += mainPosition;
      }
      if (savingsPosition != null) {
        total += savingsPosition;
      }
      return Formatting.toString(total);
    }
  }

}

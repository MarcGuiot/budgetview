package org.designup.picsou.gui.projects;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.components.PicsouTableHeaderPainter;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.description.MonthYearStringifier;
import org.designup.picsou.gui.model.BalanceStat;
import org.designup.picsou.gui.model.SavingsBalanceStat;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.gui.views.LabelCustomizer;
import org.globsframework.gui.views.utils.LabelCustomizers;
import static org.globsframework.gui.views.utils.LabelCustomizers.*;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.format.utils.AbstractGlobStringifier;
import org.globsframework.model.utils.GlobFieldComparator;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.CompositeComparator;
import org.globsframework.utils.directory.Directory;

import java.util.SortedSet;

public class NextProjectsView extends View implements GlobSelectionListener {
  private GlobTableView tableView;
  private Integer currentMonthId;
  private GlobStringifier balanceStatStringifier;
  private GlobStringifier savingsBalanceStatStringifier;

  public NextProjectsView(GlobRepository repository, Directory directory) {
    super(repository, directory);
    selectionService.addListener(this, Month.TYPE);
    balanceStatStringifier = descriptionService.getStringifier(BalanceStat.END_OF_MONTH_ACCOUNT_POSITION);
    savingsBalanceStatStringifier = descriptionService.getStringifier(SavingsBalanceStat.END_OF_MONTH_POSITION);
  }

  public void registerComponents(GlobsPanelBuilder builder) {

    tableView = GlobTableView.init(SeriesBudget.TYPE,
                                   repository,
                                   new CompositeComparator<Glob>(
                                     new GlobFieldComparator(SeriesBudget.MONTH),
                                     descriptionService.getStringifier(SeriesBudget.SERIES).getComparator(repository)),
                                   directory);
    tableView.setDefaultFont(Gui.DEFAULT_TABLE_FONT);
    tableView.setFilter(GlobMatchers.NONE);

    PicsouTableHeaderPainter.install(tableView, directory);

    tableView.addColumn(Lang.get("month"), new MonthYearStringifier(SeriesBudget.MONTH),
                        fontSize(9));
    tableView.addColumn(Lang.get("nextprojects.series"),
                        descriptionService.getStringifier(SeriesBudget.SERIES),
                        LabelCustomizers.BOLD);
    tableView.addColumn(Lang.get("amount"), SeriesBudget.AMOUNT);

    LabelCustomizer positionCustomizer = chain(fontSize(9), ALIGN_RIGHT);
    tableView.addColumn(Lang.get("nextprojects.main.position"), new MainAccountsPositionStringifier(),
                        positionCustomizer);
    tableView.addColumn(Lang.get("nextprojects.savings.position"), new SavingsAccountsPositionStringifier(),
                        positionCustomizer);
    tableView.addColumn(Lang.get("nextprojects.total.position"), new TotalAccountsPositionStringifier(),
                        positionCustomizer);

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
        if (seriesBudget.get(SeriesBudget.MONTH) < currentMonthId) {
          return false;
        }
        Glob series = repository.get(Key.create(Series.TYPE, seriesBudget.get(SeriesBudget.SERIES)));
        return BudgetArea.SPECIAL.getId().equals(series.get(Series.BUDGET_AREA));
      }
    });
  }

  private class MainAccountsPositionStringifier extends AbstractGlobStringifier {
    public String toString(Glob seriesBudget, GlobRepository repository) {
      Integer monthId = seriesBudget.get(SeriesBudget.MONTH);
      Glob balanceStat = repository.get(Key.create(BalanceStat.TYPE, monthId));
      return balanceStatStringifier.toString(balanceStat, repository);
    }
  }

  private class SavingsAccountsPositionStringifier extends AbstractGlobStringifier {
    public String toString(Glob seriesBudget, GlobRepository repository) {
      Integer monthId = seriesBudget.get(SeriesBudget.MONTH);
      Glob balanceStat = repository.get(Key.create(SavingsBalanceStat.MONTH, monthId,
                                                   SavingsBalanceStat.ACCOUNT, Account.SAVINGS_SUMMARY_ACCOUNT_ID));
      return savingsBalanceStatStringifier.toString(balanceStat, repository);
    }
  }

  private class TotalAccountsPositionStringifier extends AbstractGlobStringifier {
    public String toString(Glob seriesBudget, GlobRepository repository) {
      Integer monthId = seriesBudget.get(SeriesBudget.MONTH);
      Glob balanceStat = repository.get(Key.create(BalanceStat.TYPE, monthId));
      Glob savingsBalanceStat = repository.get(Key.create(SavingsBalanceStat.MONTH, monthId,
                                                   SavingsBalanceStat.ACCOUNT, Account.SAVINGS_SUMMARY_ACCOUNT_ID));
      return Formatting.toString(balanceStat.get(BalanceStat.END_OF_MONTH_ACCOUNT_POSITION) +
                                 savingsBalanceStat.get(SavingsBalanceStat.END_OF_MONTH_POSITION));
    }
  }
}

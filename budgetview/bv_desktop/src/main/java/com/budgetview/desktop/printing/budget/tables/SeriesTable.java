package com.budgetview.desktop.printing.budget.tables;

import com.budgetview.desktop.description.Formatting;
import com.budgetview.desktop.description.SeriesAndGroupsComparator;
import com.budgetview.desktop.model.SeriesStat;
import com.budgetview.desktop.printing.utils.BudgetReportUtils;
import com.budgetview.desktop.series.utils.SeriesOrGroup;
import com.budgetview.model.BudgetArea;
import com.budgetview.model.Month;
import com.budgetview.model.util.ClosedMonthRange;
import com.budgetview.shared.utils.Amounts;
import com.budgetview.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.budgetview.desktop.model.SeriesStat.isSummaryForBudgetArea;
import static org.globsframework.model.utils.GlobMatchers.*;

public class SeriesTable {

  private static final int MAX_ROWS_PER_PAGE = 30;

  public static List<SeriesTable> getAll(Integer currentMonth,
                                         ClosedMonthRange monthRange,
                                         GlobRepository repository) {
    List<SeriesTable> result = new ArrayList<SeriesTable>();

    List<Integer> months = monthRange.asList();
    for (BudgetArea budgetArea : BudgetReportUtils.BUDGET_AREAS) {
      List<SeriesRow> rows = new ArrayList<SeriesRow>();
      GlobList seriesStatList =
        repository.getAll(SeriesStat.TYPE,
                          and(isSummaryForBudgetArea(budgetArea),
                              fieldIn(SeriesStat.MONTH, months),
                              isTrue(SeriesStat.ACTIVE),
                              isNotNull(SeriesStat.SUMMARY_AMOUNT),
                              not(fieldEquals(SeriesStat.SUMMARY_AMOUNT, 0.00))));

      GlobList targetList = SeriesStat.getTargets(seriesStatList, repository);
      Collections.sort(targetList, new SeriesAndGroupsComparator(repository));
      for (Glob target : targetList) {
        rows.add(new SeriesRow(new SeriesOrGroup(target), months, budgetArea, repository));
      }
      for (List<SeriesRow> pageList : Utils.split(rows, MAX_ROWS_PER_PAGE)) {
        result.add(new SeriesTable(pageList, budgetArea, currentMonth, months));
      }
    }
    return result;
  }

  private List<SeriesRow> seriesRows;
  private BudgetArea budgetArea;
  private Integer currentMonth;
  private List<Integer> months;

  public SeriesTable(List<SeriesRow> rows, BudgetArea budgetArea,
                     Integer currentMonth, List<Integer> months) {
    this.seriesRows = rows;
    this.budgetArea = budgetArea;
    this.currentMonth = currentMonth;
    this.months = months;
  }

  public String getTitle() {
    return budgetArea.getLabel();
  }

  public int getColumnCount() {
    return months.size() + 2;
  }

  public String getColumnTitle(int column) {
    if (column == 0) {
      return Lang.get("print.seriesTable.series");
    }
    if (column == 1) {
      return Lang.get("print.seriesTable.total");
    }
    return Month.getShortMonthLabelWithShortYear(months.get(column - 2));
  }

  public boolean isColumnSelected(int column) {
    return column > 2 && Utils.equal(currentMonth, months.get(column - 2));
  }

  public List<SeriesRow> rows() {
    return Collections.unmodifiableList(seriesRows);
  }

  public int getRowCount() {
    return rows().size();
  }

  public static class SeriesRow implements Comparable<SeriesRow> {
    private SeriesOrGroup seriesOrGroup;
    private List<Integer> months;
    private BudgetArea budgetArea;
    private GlobRepository repository;
    private Double total;

    public SeriesRow(SeriesOrGroup seriesOrGroup, List<Integer> months, BudgetArea budgetArea, GlobRepository repository) {
      this.seriesOrGroup = seriesOrGroup;
      this.months = months;
      this.budgetArea = budgetArea;
      this.repository = repository;
      this.total = getTotalForPeriod();
    }

    private Double getTotalForPeriod() {
      Double result = null;
      for (Integer monthId : months) {
        Double value = getValueForMonth(monthId);
        if (value != null) {
          result = result == null ? value : result + value;
        }
      }
      return result;
    }

    public String getValue(int column) {
      if (column == 0) {
        return seriesOrGroup.getName(repository);
      }
      if (column == 1) {
        return total != null ? Formatting.toString(total, budgetArea) : "";
      }
      int monthId = months.get(column - 2);
      Double value = getValueForMonth(monthId);
      if (Amounts.isNullOrZero(value)) {
        return "";
      }
      return Formatting.toString(value, budgetArea);
    }

    private Double getValueForMonth(int monthId) {
      Glob stat = repository.find(seriesOrGroup.createSeriesStatKey(monthId));
      if (stat == null) {
        return null;
      }
      return stat.get(SeriesStat.SUMMARY_AMOUNT);
    }

    public boolean isInGroup() {
      return seriesOrGroup.isInGroup(repository);
    }

    public int compareTo(SeriesRow other) {
      int multiplier = budgetArea.isIncome() ? -1 : 1;
      return Double.compare(total, other.total) * multiplier;
    }
  }
}

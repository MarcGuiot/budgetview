package org.designup.picsou.gui.printing.report;

import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.util.Amounts;
import org.designup.picsou.model.util.ClosedMonthRange;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.globsframework.model.utils.GlobMatchers.*;

public class SeriesTable {

  private static final int MAX_ROWS_PER_PAGE = 30;

  private static BudgetArea[] BUDGET_AREA_TABLES =
    {BudgetArea.INCOME, BudgetArea.RECURRING, BudgetArea.VARIABLE, BudgetArea.SAVINGS, BudgetArea.EXTRAS};

  public static List<SeriesTable> getAll(Integer currentMonth,
                                         ClosedMonthRange monthRange,
                                         GlobRepository repository) {
    List<SeriesTable> result = new ArrayList<SeriesTable>();

    List<Integer> months = monthRange.asList();
    for (BudgetArea budgetArea : BUDGET_AREA_TABLES) {
      List<SeriesRow> rows = new ArrayList<SeriesRow>();
      GlobList SeriesStatList =
        repository.getAll(SeriesStat.TYPE,
                          and(linkTargetFieldEquals(SeriesStat.SERIES, Series.BUDGET_AREA, budgetArea.getId()),
                              fieldIn(SeriesStat.MONTH, months),
                              isTrue(SeriesStat.ACTIVE),
                              isNotNull(SeriesStat.SUMMARY_AMOUNT),
                              not(fieldEquals(SeriesStat.SUMMARY_AMOUNT, 0.00))));

      GlobList seriesList = SeriesStatList.getTargets(SeriesStat.SERIES, repository).sort(Series.NAME);
      for (Glob series : seriesList) {
        rows.add(new SeriesRow(series, months, budgetArea, repository));
      }
      Collections.sort(rows);
      for (List<SeriesRow> subList : Utils.split(rows, MAX_ROWS_PER_PAGE)) {
        result.add(new SeriesTable(subList, budgetArea, currentMonth, months));
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
    private Glob series;
    private List<Integer> months;
    private BudgetArea budgetArea;
    private GlobRepository repository;
    private Double total;

    public SeriesRow(Glob series, List<Integer> months, BudgetArea budgetArea, GlobRepository repository) {
      this.series = series;
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
        return series.get(Series.NAME);
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
      Glob stat = repository.find(SeriesStat.createKey(series.get(Series.ID), monthId));
      if (stat == null) {
        return null;
      }
      return stat.get(SeriesStat.SUMMARY_AMOUNT);
    }

    public int compareTo(SeriesRow other) {
      int multiplier = budgetArea.isIncome() ? -1 : 1;
      return Double.compare(total, other.total) * multiplier;
    }
  }
}

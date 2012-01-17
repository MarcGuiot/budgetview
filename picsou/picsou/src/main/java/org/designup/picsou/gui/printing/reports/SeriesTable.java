package org.designup.picsou.gui.printing.reports;

import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.util.Amounts;
import org.designup.picsou.model.util.MonthRange;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.globsframework.model.utils.GlobMatchers.*;

public class SeriesTable {

  List<SeriesRow> seriesRows = new ArrayList<SeriesRow>();
  private BudgetArea budgetArea;
  private GlobRepository repository;
  private List<Integer> months;

  public SeriesTable(BudgetArea budgetArea, MonthRange monthRange, GlobRepository repository, Directory directory) {
    this.budgetArea = budgetArea;
    this.repository = repository;
    this.months = monthRange.asList();

    GlobList SeriesStatList =
      repository.getAll(SeriesStat.TYPE,
                        and(linkTargetFieldEquals(SeriesStat.SERIES, Series.BUDGET_AREA, budgetArea.getId()),
                            isTrue(SeriesStat.ACTIVE),
                            isNotNull(SeriesStat.SUMMARY_AMOUNT),
                            not(fieldEquals(SeriesStat.SUMMARY_AMOUNT, 0.00))));

    GlobList seriesList = SeriesStatList.getTargets(SeriesStat.SERIES, repository).sort(Series.NAME);
    for (Glob series : seriesList) {
      seriesRows.add(new SeriesRow(series));
    }
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

  public List<SeriesRow> rows() {
    return Collections.unmodifiableList(seriesRows);
  }

  public int getRowCount() {
    return rows().size();
  }

  public class SeriesRow {
    private Glob series;

    public SeriesRow(Glob series) {
      this.series = series;
    }

    public String getValue(int column) {
      if (column == 0) {
        return series.get(Series.NAME);
      }
      if (column == 1) {
        return "???";
      }
      int monthId = months.get(column - 2);
      Glob stat = repository.find(Key.create(SeriesStat.SERIES, series.get(Series.ID),
                                             SeriesStat.MONTH, monthId));
      if (stat == null) {
        return "";
      }
      Double value = stat.get(SeriesStat.SUMMARY_AMOUNT);
      if (Amounts.isNullOrZero(value)) {
        return "";
      }
      return Formatting.toString(value, budgetArea);
    }
  }
}

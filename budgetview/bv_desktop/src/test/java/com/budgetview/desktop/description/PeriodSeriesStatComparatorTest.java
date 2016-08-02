package com.budgetview.desktop.description;

import com.budgetview.desktop.description.PeriodSeriesStatComparator;
import com.budgetview.desktop.model.SeriesType;
import com.budgetview.model.BudgetArea;
import junit.framework.TestCase;
import com.budgetview.desktop.model.PeriodSeriesStat;
import com.budgetview.model.Series;
import com.budgetview.model.SeriesGroup;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;
import org.globsframework.model.utils.ReverseGlobFieldComparator;

import java.util.HashMap;
import java.util.Map;

import static org.globsframework.model.FieldValue.value;

public class PeriodSeriesStatComparatorTest extends TestCase {

  private Map<String, Glob> seriesMap = new HashMap<String, Glob>();
  private Map<String, Glob> groupMap = new HashMap<String, Glob>();
  private GlobRepository repository;

  protected void setUp() throws Exception {
    repository = GlobRepositoryBuilder.init().get();
  }

  public void testWithChildren() throws Exception {
    check("Group1/a stat",
          stat("a stat", 100.00),
          group("Group1", 100.00, "a stat"));

    check("Group1/a stat/Group2",
          group("Group2", 100.00),
          stat("a stat", 50.00),
          group("Group1", 100.00, "a stat"));

    check("Group2/Group1/a stat",
          group("Group2", 200.00),
          stat("a stat", 100.00),
          group("Group1", 100.00, "a stat"));
  }

  public void testWithActiveAndInactiveSeries() throws Exception {
    check("Group1/Stat1/Stat2/Group2",
          stat("Stat1", 100.00),
          stat("Stat2", 50.00, false),
          group("Group1", 150.00, "Stat1", "Stat2"),
          group("Group2", 200.00, false));

    check("Group1/Stat2/Stat1/Group2",
          stat("Stat1", 100.00, false),
          stat("Stat2", 50.00),
          group("Group1", 150.00, "Stat1", "Stat2"),
          group("Group2", 200.00, false));
  }

  private void check(String sequence, Glob... statArray) {
    PeriodSeriesStatComparator comparator = new PeriodSeriesStatComparator(repository,
                                                                           new ReverseGlobFieldComparator(PeriodSeriesStat.ABS_SUM_AMOUNT));
    GlobList statList = new GlobList(statArray);
    boolean first = true;
    StringBuilder builder = new StringBuilder();
    for (Glob stat : statList.sortSelf(comparator)) {
      if (!first) {
        builder.append("/");
      }
      builder.append(PeriodSeriesStat.getName(stat, repository));
      first = false;
    }
    assertEquals(sequence, builder.toString());

    repository.deleteAll();
    seriesMap.clear();
    groupMap.clear();
  }

  private Glob stat(String name, double amount) {
    return stat(name, amount, Boolean.TRUE, null);
  }

  private Glob stat(String name, double amount, boolean active) {
    return stat(name, amount, active, null);
  }

  private Glob stat(String name, double amount, Boolean active, String groupName) {
    Glob series = repository.create(Series.TYPE,
                                    value(Series.BUDGET_AREA, BudgetArea.RECURRING.getId()),
                                    value(Series.GROUP, getGroupId(groupName)),
                                    value(Series.NAME, name));
    seriesMap.put(name, series);
    Glob stat = repository.create(PeriodSeriesStat.TYPE,
                                  value(PeriodSeriesStat.TARGET, series.get(Series.ID)),
                                  value(PeriodSeriesStat.TARGET_TYPE, SeriesType.SERIES.getId()),
                                  value(PeriodSeriesStat.ABS_SUM_AMOUNT, amount),
                                  value(PeriodSeriesStat.ACTIVE, active));
    return stat;
  }

  private Glob group(String name, double amount, String... seriesToAdd) {
    return group(name, amount, true, seriesToAdd);
  }

  private Glob group(String name, double amount, boolean active, String... seriesToAdd) {
    Glob group = repository.create(SeriesGroup.TYPE,
                                   value(SeriesGroup.BUDGET_AREA, BudgetArea.RECURRING.getId()),
                                   value(SeriesGroup.NAME, name));
    groupMap.put(name, group);
    Glob stat = repository.create(PeriodSeriesStat.TYPE,
                                  value(PeriodSeriesStat.TARGET, group.get(SeriesGroup.ID)),
                                  value(PeriodSeriesStat.TARGET_TYPE, SeriesType.SERIES_GROUP.getId()),
                                  value(PeriodSeriesStat.ABS_SUM_AMOUNT, amount),
                                  value(PeriodSeriesStat.ACTIVE, active));

    for (String seriesName : seriesToAdd) {
      repository.update(seriesMap.get(seriesName).getKey(),
                        value(Series.GROUP, group.get(SeriesGroup.ID)));
    }
    return stat;
  }

  private Integer getGroupId(String groupName) {
    Glob group = seriesMap.get(groupName);
    if (group == null) {
      return null;
    }
    return group.get(SeriesGroup.ID);
  }
}

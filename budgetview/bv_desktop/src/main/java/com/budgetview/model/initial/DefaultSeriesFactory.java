package com.budgetview.model.initial;

import com.budgetview.desktop.description.Labels;
import com.budgetview.model.*;
import com.budgetview.shared.model.BudgetArea;
import com.budgetview.shared.model.DefaultSeries;
import org.globsframework.model.FieldValuesBuilder;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import java.util.HashMap;
import java.util.Map;

import static org.globsframework.model.FieldValue.value;

public class DefaultSeriesFactory {

  public static boolean AUTO_CREATE_DEFAULT_SERIES = true;

  private GlobRepository repository;

  public static void run(GlobRepository repository, Directory directory) {
    DefaultSeriesFactory factory = new DefaultSeriesFactory(repository);
    factory.createSystemSeries();
    if (AUTO_CREATE_DEFAULT_SERIES && !repository.contains(Series.TYPE, Series.USER_SERIES_MATCHER)) {
      factory.createUserSeries();
    }
  }

  private DefaultSeriesFactory(GlobRepository repository) {
    this.repository = repository;
  }

  private void createSystemSeries() {
    repository.findOrCreate(Key.create(Series.TYPE, Series.UNCATEGORIZED_SERIES_ID),
                            value(Series.BUDGET_AREA, BudgetArea.UNCATEGORIZED.getId()),
                            value(Series.PROFILE_TYPE, ProfileType.IRREGULAR.getId()),
                            value(Series.IS_AUTOMATIC, false),
                            value(Series.DAY, 1),
                            value(Series.NAME, Series.getUncategorizedName()));
    repository.findOrCreate(Key.create(Series.TYPE, Series.ACCOUNT_SERIES_ID),
                            value(Series.BUDGET_AREA, BudgetArea.OTHER.getId()),
                            value(Series.PROFILE_TYPE, ProfileType.IRREGULAR.getId()),
                            value(Series.IS_AUTOMATIC, false),
                            value(Series.DAY, 1),
                            value(Series.NAME, Series.getAccountSeriesName()));
  }

  private void createUserSeries() {

    Map<DefaultSeries, Key> seriesToKeys = new HashMap<DefaultSeries, Key>();

    for (DefaultSeries defaultSeries : DefaultSeries.values()) {
      DefaultSeries parent = defaultSeries.getParent();
      if (parent == null) {
        BudgetArea budgetArea = defaultSeries.getBudgetArea();
        FieldValuesBuilder builder = FieldValuesBuilder.init()
          .set(Series.NAME, Labels.get(defaultSeries))
          .set(Series.IS_AUTOMATIC, budgetArea.isAutomatic())
          .set(Series.BUDGET_AREA, budgetArea.getId())
          .set(Series.TARGET_ACCOUNT, budgetArea == BudgetArea.TRANSFER ? null : Account.MAIN_SUMMARY_ACCOUNT_ID)
          .set(Series.PROFILE_TYPE, ProfileType.EVERY_MONTH.getId())
          .set(Series.IS_INITIAL, Boolean.TRUE);

        Glob series = repository.create(Series.TYPE, builder.toArray());
        seriesToKeys.put(defaultSeries, series.getKey());
      }
      else {
        repository.create(SubSeries.TYPE,
                          value(SubSeries.NAME, Labels.get(defaultSeries)),
                          value(SubSeries.SERIES, seriesToKeys.get(parent).get(Series.ID)));
      }
    }

    SignpostStatus.setPeriodicitySeriesKey(seriesToKeys.get(DefaultSeries.ELECTRICITY), repository);
    SignpostStatus.setAmountSeriesKey(seriesToKeys.get(DefaultSeries.GROCERIES), repository);
  }
}

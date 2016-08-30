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

    createEntry(DefaultSeries.INCOME, ProfileType.EVERY_MONTH);

    createEntry(DefaultSeries.RENT, ProfileType.EVERY_MONTH);

    SignpostStatus.setPeriodicitySeriesKey(
      createEntry(DefaultSeries.ELECTRICITY, ProfileType.EVERY_MONTH), repository
    );
    createEntry(DefaultSeries.GAS, ProfileType.EVERY_MONTH);
    createEntry(DefaultSeries.WATER, ProfileType.EVERY_MONTH);
    createEntry(DefaultSeries.CAR_CREDIT, ProfileType.EVERY_MONTH);
    createEntry(DefaultSeries.CAR_INSURANCE, ProfileType.EVERY_MONTH);
    createEntry(DefaultSeries.INCOME_TAXES, ProfileType.EVERY_MONTH);
    createEntry(DefaultSeries.CELL_PHONE, ProfileType.EVERY_MONTH);
    createEntry(DefaultSeries.INTERNET, ProfileType.EVERY_MONTH);
    createEntry(DefaultSeries.FIXED_PHONE, ProfileType.EVERY_MONTH);

    SignpostStatus.setAmountSeriesKey(
      createEntry(DefaultSeries.GROCERIES, ProfileType.EVERY_MONTH), repository
    );
    createEntry(DefaultSeries.HEALTH, ProfileType.EVERY_MONTH, DefaultSeries.PHYSICIAN, DefaultSeries.PHARMACY, DefaultSeries.REIMBURSEMENTS);
    createEntry(DefaultSeries.LEISURES, ProfileType.EVERY_MONTH);
    createEntry(DefaultSeries.CLOTHING, ProfileType.EVERY_MONTH);
    createEntry(DefaultSeries.BEAUTY, ProfileType.EVERY_MONTH);
    createEntry(DefaultSeries.FUEL, ProfileType.EVERY_MONTH);
    createEntry(DefaultSeries.CASH, ProfileType.EVERY_MONTH);
    createEntry(DefaultSeries.BANK_FEES, ProfileType.EVERY_MONTH);
    createEntry(DefaultSeries.RESTAURANT, ProfileType.EVERY_MONTH);
    createEntry(DefaultSeries.MISC, ProfileType.EVERY_MONTH);
  }

  private Key createEntry(DefaultSeries defaultSeries,
                          ProfileType profileType,
                          DefaultSeries... subSeriesList) {

    BudgetArea budgetArea = defaultSeries.getBudgetArea();

    FieldValuesBuilder builder = FieldValuesBuilder.init()
      .set(Series.NAME, Labels.get(defaultSeries))
      .set(Series.IS_AUTOMATIC, budgetArea.isAutomatic())
      .set(Series.BUDGET_AREA, budgetArea.getId())
      .set(Series.TARGET_ACCOUNT, budgetArea == BudgetArea.TRANSFER ? null : Account.MAIN_SUMMARY_ACCOUNT_ID)
      .set(Series.PROFILE_TYPE, profileType.getId())
      .set(Series.IS_INITIAL, Boolean.TRUE);

    Glob series = repository.create(Series.TYPE, builder.toArray());

    for (DefaultSeries subSeries : subSeriesList) {
      String subSeriesName = Labels.get(defaultSeries, subSeries);
      repository.create(SubSeries.TYPE,
                        value(SubSeries.NAME, subSeriesName),
                        value(SubSeries.SERIES, series.get(Series.ID)));
    }

    return series.getKey();
  }
}

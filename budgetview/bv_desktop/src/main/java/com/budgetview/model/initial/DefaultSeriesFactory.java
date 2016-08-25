package com.budgetview.model.initial;

import com.budgetview.model.*;
import com.budgetview.shared.model.DefaultSeries;
import com.budgetview.utils.Lang;
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

    createEntry(BudgetArea.INCOME, DefaultSeries.INCOME, ProfileType.EVERY_MONTH);

    createEntry(BudgetArea.RECURRING, DefaultSeries.RENT, ProfileType.EVERY_MONTH);

    SignpostStatus.setPeriodicitySeriesKey(
      createEntry(BudgetArea.RECURRING, DefaultSeries.ELECTRICITY, ProfileType.EVERY_MONTH), repository
    );
    createEntry(BudgetArea.RECURRING, DefaultSeries.GAS, ProfileType.EVERY_MONTH);
    createEntry(BudgetArea.RECURRING, DefaultSeries.WATER, ProfileType.EVERY_MONTH);
    createEntry(BudgetArea.RECURRING, DefaultSeries.CAR_CREDIT, ProfileType.EVERY_MONTH);
    createEntry(BudgetArea.RECURRING, DefaultSeries.CAR_INSURANCE, ProfileType.EVERY_MONTH);
    createEntry(BudgetArea.RECURRING, DefaultSeries.INCOME_TAXES, ProfileType.EVERY_MONTH);
    createEntry(BudgetArea.RECURRING, DefaultSeries.CELL_PHONE, ProfileType.EVERY_MONTH);
    createEntry(BudgetArea.RECURRING, DefaultSeries.INTERNET, ProfileType.EVERY_MONTH);
    createEntry(BudgetArea.RECURRING, DefaultSeries.FIXED_PHONE, ProfileType.EVERY_MONTH);

    SignpostStatus.setAmountSeriesKey(
      createEntry(BudgetArea.VARIABLE, DefaultSeries.GROCERIES, ProfileType.EVERY_MONTH), repository
    );
    createEntry(BudgetArea.VARIABLE, DefaultSeries.HEALTH, ProfileType.EVERY_MONTH, DefaultSeries.PHYSICIAN, DefaultSeries.PHARMACY, DefaultSeries.REIMBURSEMENTS);
    createEntry(BudgetArea.VARIABLE, DefaultSeries.LEISURES, ProfileType.EVERY_MONTH);
    createEntry(BudgetArea.VARIABLE, DefaultSeries.CLOTHING, ProfileType.EVERY_MONTH);
    createEntry(BudgetArea.VARIABLE, DefaultSeries.BEAUTY, ProfileType.EVERY_MONTH);
    createEntry(BudgetArea.VARIABLE, DefaultSeries.FUEL, ProfileType.EVERY_MONTH);
    createEntry(BudgetArea.VARIABLE, DefaultSeries.CASH, ProfileType.EVERY_MONTH);
    createEntry(BudgetArea.VARIABLE, DefaultSeries.BANK_FEES, ProfileType.EVERY_MONTH);
    createEntry(BudgetArea.VARIABLE, DefaultSeries.RESTAURANT, ProfileType.EVERY_MONTH);
    createEntry(BudgetArea.VARIABLE, DefaultSeries.MISC, ProfileType.EVERY_MONTH);
  }

  private Key createEntry(BudgetArea budgetArea, DefaultSeries nameKey,
                          ProfileType profileType,
                          DefaultSeries... subSeriesList) {

    FieldValuesBuilder builder = FieldValuesBuilder.init()
      .set(Series.NAME, getName(budgetArea, nameKey))
      .set(Series.IS_AUTOMATIC, budgetArea.isAutomatic())
      .set(Series.BUDGET_AREA, budgetArea.getId())
      .set(Series.TARGET_ACCOUNT, budgetArea == BudgetArea.TRANSFER ? null : Account.MAIN_SUMMARY_ACCOUNT_ID)
      .set(Series.PROFILE_TYPE, profileType.getId())
      .set(Series.IS_INITIAL, Boolean.TRUE);

    Glob series = repository.create(Series.TYPE, builder.toArray());

    for (DefaultSeries subSeries : subSeriesList) {
      String subSeriesName = getName(budgetArea, nameKey, subSeries);
      repository.create(SubSeries.TYPE,
                        value(SubSeries.NAME, subSeriesName),
                        value(SubSeries.SERIES, series.get(Series.ID)));
    }

    return series.getKey();
  }

  private String getName(BudgetArea budgetArea, DefaultSeries nameKey, DefaultSeries subSeriesKey) {
    return Lang.get("defaultSeries." + budgetArea.getName() + "." + nameKey.getName() + "." + subSeriesKey.getName());
  }

  private String getName(BudgetArea budgetArea, DefaultSeries nameKey) {
    return Lang.get("defaultSeries." + budgetArea.getName() + "." + nameKey.getName());
  }

}

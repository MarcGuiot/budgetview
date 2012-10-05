package org.designup.picsou.model.initial;

import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
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

    createEntry(BudgetArea.INCOME, "income1", ProfileType.EVERY_MONTH);
    createEntry(BudgetArea.INCOME, "income2", ProfileType.EVERY_MONTH);

    createEntry(BudgetArea.RECURRING, "rent", ProfileType.EVERY_MONTH);

    SignpostStatus.setPeriodicitySeriesKey(
      createEntry(BudgetArea.RECURRING, "electricity", ProfileType.EVERY_MONTH), repository
    );
    createEntry(BudgetArea.RECURRING, "gas", ProfileType.EVERY_MONTH);
    createEntry(BudgetArea.RECURRING, "water", ProfileType.EVERY_MONTH);
    createEntry(BudgetArea.RECURRING, "carCredit", ProfileType.EVERY_MONTH);
    createEntry(BudgetArea.RECURRING, "carInsurance", ProfileType.EVERY_MONTH);
    createEntry(BudgetArea.RECURRING, "incomeTaxes", ProfileType.EVERY_MONTH);
    createEntry(BudgetArea.RECURRING, "cellPhone1", ProfileType.EVERY_MONTH);
    createEntry(BudgetArea.RECURRING, "cellPhone2", ProfileType.EVERY_MONTH);
    createEntry(BudgetArea.RECURRING, "internet", ProfileType.EVERY_MONTH);
    createEntry(BudgetArea.RECURRING, "fixedPhone", ProfileType.EVERY_MONTH);

    SignpostStatus.setAmountSeriesKey(
      createEntry(BudgetArea.VARIABLE, "groceries", ProfileType.EVERY_MONTH), repository
    );
    createEntry(BudgetArea.VARIABLE, "health", ProfileType.EVERY_MONTH, "physician", "pharmacy", "reimbursements");
    createEntry(BudgetArea.VARIABLE, "leisures", ProfileType.EVERY_MONTH);
    createEntry(BudgetArea.VARIABLE, "clothing", ProfileType.EVERY_MONTH);
    createEntry(BudgetArea.VARIABLE, "beauty", ProfileType.EVERY_MONTH);
    createEntry(BudgetArea.VARIABLE, "fuel", ProfileType.EVERY_MONTH);
    createEntry(BudgetArea.VARIABLE, "cash", ProfileType.EVERY_MONTH);
    createEntry(BudgetArea.VARIABLE, "bankFees", ProfileType.EVERY_MONTH);
    createEntry(BudgetArea.VARIABLE, "misc", ProfileType.EVERY_MONTH);
  }

  private Key createEntry(BudgetArea budgetArea, String nameKey,
                          ProfileType profileType,
                          String... subSeries) {

    FieldValuesBuilder builder = FieldValuesBuilder.init()
      .set(Series.NAME, getName(budgetArea, nameKey))
      .set(Series.IS_AUTOMATIC, budgetArea.isAutomatic())
      .set(Series.BUDGET_AREA, budgetArea.getId())
      .set(Series.PROFILE_TYPE, profileType.getId())
      .set(Series.IS_INITIAL, Boolean.TRUE);

    Glob series = repository.create(Series.TYPE, builder.toArray());

    for (String subSeriesKey : subSeries) {
      String subSeriesName = getName(budgetArea, nameKey, subSeriesKey);
      repository.create(SubSeries.TYPE,
                        value(SubSeries.NAME, subSeriesName),
                        value(SubSeries.SERIES, series.get(Series.ID)));
    }

    return series.getKey();
  }

  private String getName(BudgetArea budgetArea, String nameKey, String subSeriesKey) {
    return Lang.get("defaultSeries." + budgetArea.getName() + "." + nameKey + "." + subSeriesKey);
  }

  private String getName(BudgetArea budgetArea, String nameKey) {
    return Lang.get("defaultSeries." + budgetArea.getName() + "." + nameKey);
  }

}

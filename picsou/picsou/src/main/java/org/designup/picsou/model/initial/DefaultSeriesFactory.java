package org.designup.picsou.model.initial;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.ProfileType;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SubSeries;
import org.designup.picsou.utils.Lang;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.FieldValuesBuilder;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;

public class DefaultSeriesFactory {

  public static boolean AUTO_CREATE_DEFAULT_SERIES = true;
  
  private GlobRepository repository;

  public static void run(GlobRepository repository) {
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
  }

  private void createUserSeries() {

    createEntry(BudgetArea.INCOME, ProfileType.EVERY_MONTH, "income1", true);
    createEntry(BudgetArea.INCOME, ProfileType.EVERY_MONTH, "income2", true);

    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "rent", false);
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "electricity", true);
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "gas", true);
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "water", true);
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "carCredit", true);
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "carInsurance", true);
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "incomeTaxes", true);
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "cellPhone1", true);
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "cellPhone2", true);
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "internet", true);
    createEntry(BudgetArea.RECURRING, ProfileType.EVERY_MONTH, "fixedPhone", false);

    createEntry(BudgetArea.ENVELOPES, ProfileType.EVERY_MONTH, "groceries", true);
    createEntry(BudgetArea.ENVELOPES, ProfileType.EVERY_MONTH, "health", true, "physician", "pharmacy", "reimbursements");
    createEntry(BudgetArea.ENVELOPES, ProfileType.EVERY_MONTH, "leisures", true);
    createEntry(BudgetArea.ENVELOPES, ProfileType.EVERY_MONTH, "clothing", true);
    createEntry(BudgetArea.ENVELOPES, ProfileType.EVERY_MONTH, "beauty", true);
    createEntry(BudgetArea.ENVELOPES, ProfileType.EVERY_MONTH, "fuel", true);
    createEntry(BudgetArea.ENVELOPES, ProfileType.EVERY_MONTH, "cash", true);
    createEntry(BudgetArea.ENVELOPES, ProfileType.EVERY_MONTH, "bankFees", true);
    createEntry(BudgetArea.ENVELOPES, ProfileType.EVERY_MONTH, "misc", true);
  }

  private void createEntry(BudgetArea budgetArea,
                           ProfileType profileType,
                           String nameKey,
                           boolean selected,
                           String... subSeries) {

    FieldValuesBuilder builder = FieldValuesBuilder.init()
      .set(Series.NAME, getName(budgetArea, nameKey))
      .set(Series.BUDGET_AREA, budgetArea.getId())
      .set(Series.PROFILE_TYPE, profileType.getId());

    Glob series = repository.create(Series.TYPE, builder.toArray());

    for (String subSeriesKey : subSeries) {
      String subSeriesName = getName(budgetArea, nameKey, subSeriesKey);
      repository.create(SubSeries.TYPE,
                        value(SubSeries.NAME, subSeriesName),
                        value(SubSeries.SERIES, series.get(Series.ID)));
    }
  }

  private String getName(BudgetArea budgetArea, String nameKey, String subSeriesKey) {
    return Lang.get("defaultSeries." + budgetArea.getName() + "." + nameKey + "." + subSeriesKey);
  }

  private String getName(BudgetArea budgetArea, String nameKey) {
    return Lang.get("defaultSeries." + budgetArea.getName() + "." + nameKey);
  }

}

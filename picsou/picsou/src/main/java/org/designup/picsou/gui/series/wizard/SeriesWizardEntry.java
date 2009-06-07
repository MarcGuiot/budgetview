package org.designup.picsou.gui.series.wizard;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.ProfileType;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SubSeries;
import org.designup.picsou.utils.Lang;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.FieldValuesBuilder;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;

public class SeriesWizardEntry {
  private BudgetArea budgetArea;
  private String name;
  private ProfileType profileType;
  private String seriesKey;
  private String[] subSeries;
  private boolean selected;

  public SeriesWizardEntry(BudgetArea budgetArea, ProfileType profileType, String seriesKey,
                           String[] subSeriesKeys,
                           GlobRepository repository) {
    this.budgetArea = budgetArea;
    this.profileType = profileType;
    this.seriesKey = seriesKey;
    this.name = Lang.get("seriesWizard." + budgetArea.getName() + "." + seriesKey);
    this.subSeries = subSeriesKeys;
    this.selected = budgetArea == BudgetArea.ENVELOPES;
  }

  public String getName() {
    return name;
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  public boolean isSelected() {
    return selected;
  }

  public void createSeries(GlobRepository repository) {
    if (!selected) {
      return;
    }

    FieldValuesBuilder builder = FieldValuesBuilder.init()
      .set(Series.NAME, name)
      .set(Series.BUDGET_AREA, budgetArea.getId())
      .set(Series.PROFILE_TYPE, profileType.getId());

    Glob series = repository.create(Series.TYPE, builder.toArray());

    for (String subSeriesKey : subSeries) {
      String subSeriesName = Lang.get("seriesWizard." + budgetArea.getName() + "." + seriesKey + "." + subSeriesKey);
      repository.create(SubSeries.TYPE,
                        value(SubSeries.NAME, subSeriesName),
                        value(SubSeries.SERIES, series.get(Series.ID)));
    }
  }
}

package org.designup.picsou.gui.series.view;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesGroup;
import org.designup.picsou.model.SubSeries;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.format.utils.AbstractGlobStringifier;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidParameter;

public class SeriesWrapperStringifier extends AbstractGlobStringifier {
  private GlobStringifier seriesStringifier;
  private final GlobStringifier seriesGroupStringifier;
  private GlobStringifier subSeriesStringifier;
  private GlobStringifier budgetAreaStringifier;
  private GlobRepository parentRepository;

  public SeriesWrapperStringifier(GlobRepository parentRepository, Directory directory) {
    this.parentRepository = parentRepository;

    DescriptionService descriptionService = directory.get(DescriptionService.class);
    seriesStringifier = descriptionService.getStringifier(Series.TYPE);
    seriesGroupStringifier = descriptionService.getStringifier(SeriesGroup.TYPE);
    subSeriesStringifier = descriptionService.getStringifier(SubSeries.TYPE);
    budgetAreaStringifier = descriptionService.getStringifier(BudgetArea.TYPE);
  }

  public String toString(Glob wrapper, GlobRepository repository) {
    switch (SeriesWrapperType.get(wrapper)) {

      case BUDGET_AREA:
        Glob budgetArea = parentRepository.find(Key.create(BudgetArea.TYPE, wrapper.get(SeriesWrapper.ITEM_ID)));
        if (budgetArea == null) {
          return "";
        }
        return budgetAreaStringifier.toString(budgetArea, repository);

      case SERIES:
        Glob series = parentRepository.find(Key.create(Series.TYPE, wrapper.get(SeriesWrapper.ITEM_ID)));
        if (series == null) {
          return "";
        }
        return seriesStringifier.toString(series, repository);

      case SERIES_GROUP:
        Glob seriesGroup = parentRepository.find(Key.create(SeriesGroup.TYPE, wrapper.get(SeriesWrapper.ITEM_ID)));
        if (seriesGroup == null) {
          return "";
        }
        return seriesGroupStringifier.toString(seriesGroup, repository);

      case SUB_SERIES:
        Glob subSeries = parentRepository.find(Key.create(SubSeries.TYPE, wrapper.get(SeriesWrapper.ITEM_ID)));
        if (subSeries == null) {
          return "";
        }
        return subSeriesStringifier.toString(subSeries, repository);

      case SUMMARY:
        Integer id = wrapper.get(SeriesWrapper.ID);
        if (id.equals(SeriesWrapper.BALANCE_SUMMARY_ID)) {
          return Lang.get("seriesWrapper.balanceSummary");
        }
        if (id.equals(SeriesWrapper.MAIN_POSITION_SUMMARY_ID)) {
          return Lang.get("seriesWrapper.mainSummary");
        }
        if (id.equals(SeriesWrapper.SAVINGS_POSITION_SUMMARY_ID)) {
          return Lang.get("seriesWrapper.savingsSummary");
        }

      default:
        throw new InvalidParameter("Unexpected case: " + wrapper);
    }

  }
}

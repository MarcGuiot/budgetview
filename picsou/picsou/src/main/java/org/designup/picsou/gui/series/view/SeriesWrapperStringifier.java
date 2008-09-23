package org.designup.picsou.gui.series.view;

import org.globsframework.model.format.utils.AbstractGlobStringifier;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.utils.Lang;

public class SeriesWrapperStringifier extends AbstractGlobStringifier {
  private GlobStringifier seriesStringifier;
  private GlobStringifier budgetAreaStringifier;
  private GlobRepository parentRepository;

  public SeriesWrapperStringifier(GlobRepository parentRepository, Directory directory) {
    this.parentRepository = parentRepository;

    DescriptionService descriptionService = directory.get(DescriptionService.class);
    seriesStringifier = descriptionService.getStringifier(Series.TYPE);
    budgetAreaStringifier = descriptionService.getStringifier(BudgetArea.TYPE);
  }

  public String toString(Glob wrapper, GlobRepository repository) {
    if (Boolean.TRUE.equals(wrapper.get(SeriesWrapper.IS_BUDGET_AREA))) {
      Glob budgetArea = parentRepository.find(Key.create(BudgetArea.TYPE, wrapper.get(SeriesWrapper.ITEM_ID)));
      if (budgetArea == null) return "";
      return budgetAreaStringifier.toString(budgetArea, repository);
    }
    else {
      Glob series = parentRepository.find(Key.create(Series.TYPE, wrapper.get(SeriesWrapper.ITEM_ID)));
      if (series == null) return "";
      return seriesStringifier.toString(series, repository);
    }
  }
}

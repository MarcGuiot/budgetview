package org.designup.picsou.gui.accounts.utils;

import org.designup.picsou.gui.utils.Matchers;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.views.GlobComboView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Collections;

public class SeriesFilteringCombo {
  private GlobComboView combo;

  public SeriesFilteringCombo(GlobRepository repository, Directory directory,
                              GlobComboView.GlobSelectionHandler handler) {
    combo =
      GlobComboView.init(Series.TYPE, repository, directory)
        .setShowEmptyOption(true)
        .setEmptyOptionLabel(Lang.get("transactionView.series.filter.all"));
    combo.setSelectionHandler(handler);

    final Matchers.SeriesFirstEndDateFilter matcher = Matchers.userSeriesActiveInPeriod();
    combo.setFilter(matcher);
    directory.get(SelectionService.class).addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        matcher.filterDates(selection.getAll(Month.TYPE).getValueSet(Month.ID));
        combo.setFilter(matcher);
      }
    }, Month.TYPE, Series.TYPE);
  }

  public GlobMatcher getCurrentSeriesFilter() {
    Glob currentSelection = combo.getCurrentSelection();
    if ((currentSelection == null) || !currentSelection.exists()) {
      return GlobMatchers.ALL;
    }
    Integer seriesId = currentSelection.get(Series.ID);
    return Matchers.transactionsForSeries(Collections.singleton(seriesId));
  }

  public JComboBox getComponent() {
    return combo.getComponent();
  }

  public void reset() {
    combo.selectFirst();
  }
}

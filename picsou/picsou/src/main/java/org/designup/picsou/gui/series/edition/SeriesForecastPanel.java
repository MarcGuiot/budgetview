package org.designup.picsou.gui.series.edition;

import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.model.Series;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.editors.GlobCheckBoxView;
import org.globsframework.gui.editors.GlobLinkComboEditor;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Set;

public class SeriesForecastPanel {
  private static final int DEFAULT_DAY = 15;
  private GlobRepository repository;
  private Directory directory;
  private JPanel panel;
  private GlobCheckBoxView forceCheckbox;
  private GlobLinkComboEditor dayCombo;
  private Key currentSeriesKey;

  public SeriesForecastPanel(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    createPanel();
  }

  private void createPanel() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(SeriesEditionDialog.class,
                                                      "/layout/series/seriesForecastPanel.splits",
                                                      repository, directory);

    forceCheckbox = builder.addCheckBox("forceSingleOperationCheckbox", Series.FORCE_SINGLE_OPERATION);
    dayCombo =
      builder
        .addComboEditor("forceSingleOperationDayCombo", Series.FORCE_SINGLE_OPERATION_DAY)
        .setShowEmptyOption(false);

    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (currentSeriesKey != null && changeSet.containsChanges(currentSeriesKey)) {
          update();
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        if (changedTypes.contains(Series.TYPE)) {
          update();
        }
      }
    });

    panel = builder.load();

    update();
  }

  private void update() {
    Glob series = repository.find(currentSeriesKey);
    dayCombo.setEnabled(series != null && series.isTrue(Series.FORCE_SINGLE_OPERATION));
    if (series != null && series.get(Series.FORCE_SINGLE_OPERATION_DAY) == null) {
      repository.update(currentSeriesKey, Series.FORCE_SINGLE_OPERATION_DAY, DEFAULT_DAY);
    }
  }

  public JPanel getPanel() {
    return panel;
  }

  public void setCurrentSeries(Glob currentSeries) {
    if ((currentSeries == null) || !repository.contains(currentSeries.getKey())) {
      currentSeriesKey = null;
      update();
      return;
    }
    currentSeriesKey = currentSeries.getKey();
    forceCheckbox.forceSelection(currentSeriesKey);
    dayCombo.forceSelection(currentSeriesKey);
    update();
  }
}

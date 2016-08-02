package com.budgetview.desktop.series.edition;

import com.budgetview.desktop.series.SeriesEditionDialog;
import com.budgetview.model.Series;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.editors.GlobLinkComboEditor;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class SeriesForecastPanel {
  private static final int DEFAULT_DAY = 15;
  private GlobRepository repository;
  private Directory directory;
  private List<ForecastMode> modes;
  private JPanel panel;
  private GlobLinkComboEditor forecastDayCombo;
  private Key currentSeriesKey;
  private JComboBox forecastModeCombo;

  public SeriesForecastPanel(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    this.modes = Arrays.asList(
      new ForecastMode(false, Lang.get("seriesEdition.forecast.mode.auto")),
      new ForecastMode(true, Lang.get("seriesEdition.forecast.mode.single")));

    createPanel();
  }

  private void createPanel() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(SeriesEditionDialog.class,
                                                      "/layout/series/seriesForecastPanel.splits",
                                                      repository, directory);

    forecastModeCombo = new JComboBox(modes.toArray());
    forecastModeCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        ForecastMode mode = (ForecastMode)forecastModeCombo.getSelectedItem();
        mode.apply();
      }
    });
    builder.add("forecastModeCombo", forecastModeCombo);
    forecastDayCombo =
      builder
        .addComboEditor("forecastDayCombo", Series.FORCE_SINGLE_OPERATION_DAY)
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
    boolean showDayCombo = series != null && series.isTrue(Series.FORCE_SINGLE_OPERATION);
    forecastDayCombo.setEnabled(showDayCombo);
    forecastDayCombo.setVisible(showDayCombo);
    if (series != null && series.get(Series.FORCE_SINGLE_OPERATION_DAY) == null) {
      repository.update(currentSeriesKey, Series.FORCE_SINGLE_OPERATION_DAY, DEFAULT_DAY);
    }
    updateMode(series);
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
    updateMode(currentSeries);
    forecastDayCombo.forceSelection(currentSeriesKey);
    update();
  }

  public void updateMode(Glob series) {
    boolean forceSingleDay = series != null && series.isTrue(Series.FORCE_SINGLE_OPERATION);
    forecastModeCombo.setSelectedIndex(forceSingleDay ? 1 : 0);
  }

  private class ForecastMode {
    private boolean forceSingleDay;
    private String label;

    public ForecastMode(boolean forceSingleDay, String label) {
      this.forceSingleDay = forceSingleDay;
      this.label = label;
    }

    public String toString() {
      return label;
    }

    protected void apply() {
      if (currentSeriesKey == null) {
        return;
      }
      repository.update(currentSeriesKey, Series.FORCE_SINGLE_OPERATION, forceSingleDay);
    }
  }
}

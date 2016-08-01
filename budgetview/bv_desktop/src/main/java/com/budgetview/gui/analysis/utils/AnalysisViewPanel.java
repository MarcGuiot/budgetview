package com.budgetview.gui.analysis.utils;

import com.budgetview.gui.analysis.SeriesChartsColors;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.util.SortedSet;

public abstract class AnalysisViewPanel {
  private String name;
  private String splitsFile;
  protected GlobRepository repository;
  protected Directory parentDirectory;
  protected Directory directory;
  protected SeriesChartsColors seriesChartsColors;

  protected AnalysisViewPanel(String name, String splitsFile, GlobRepository repository, Directory parentDirectory, Directory directory, SeriesChartsColors seriesChartsColors) {
    this.name = name;
    this.splitsFile = splitsFile;
    this.repository = repository;
    this.parentDirectory = parentDirectory;
    this.directory = directory;
    this.seriesChartsColors = seriesChartsColors;
  }

  public void load(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), splitsFile, repository, directory);
    registerComponents(builder);
    parentBuilder.add(name, builder);
  }

  protected abstract void registerComponents(GlobsPanelBuilder builder);

  public abstract void monthSelected(Integer referenceMonthId, SortedSet<Integer> monthIds);

  public abstract void reset();
}

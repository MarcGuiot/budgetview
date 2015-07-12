package org.designup.picsou.gui.analysis;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.analysis.budget.BudgetAnalysisView;
import org.designup.picsou.gui.analysis.evolution.EvolutionAnalysisView;
import org.designup.picsou.gui.analysis.table.TableAnalysisView;
import org.designup.picsou.gui.analysis.utils.AnalysisViewPanel;
import org.designup.picsou.gui.series.view.SeriesWrapper;
import org.designup.picsou.model.*;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.utils.GlobSelectionBuilder;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

public class AnalysisView extends View {

  private Directory parentDirectory;
  private SelectionService parentSelectionService;
  private Integer referenceMonthId;
  private List<AnalysisViewPanel> viewPanels;
  private CardHandler cards;

  public AnalysisView(GlobRepository repository, Directory directory) {
    super(repository, createLocalDirectory(directory));
    this.parentDirectory = directory;
    this.parentSelectionService = directory.get(SelectionService.class);
  }

  private static Directory createLocalDirectory(Directory parentDirectory) {
    Directory localDirectory = new DefaultDirectory(parentDirectory);
    SelectionService localSelectionService = new SelectionService();
    localDirectory.add(localSelectionService);
    return localDirectory;
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    parentBuilder.add("analysisView", createLocalPanel());
  }

  private GlobsPanelBuilder createLocalPanel() {

    SeriesChartsColors seriesChartsColors = new SeriesChartsColors(repository, directory);
    viewPanels = new ArrayList<AnalysisViewPanel>();
    viewPanels.add(new BudgetAnalysisView("budgetAnalysis", repository, parentDirectory, directory, seriesChartsColors));
    viewPanels.add(new EvolutionAnalysisView("evolutionAnalysis", repository, parentDirectory, directory, seriesChartsColors));
    viewPanels.add(new TableAnalysisView("tableAnalysis", repository, parentDirectory, directory, seriesChartsColors));

    parentSelectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        SortedSet<Integer> monthIds = selection.getAll(Month.TYPE).getSortedSet(Month.ID);
        if (!monthIds.isEmpty()) {
          referenceMonthId = monthIds.iterator().next();
          for (AnalysisViewPanel panel : viewPanels) {
            panel.monthSelected(referenceMonthId, monthIds);
          }
        }
      }
    }, Month.TYPE);

    parentSelectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        GlobSelectionBuilder builder = GlobSelectionBuilder.init();
        for (Glob series : selection.getAll(Series.TYPE)) {
          builder.add(SeriesWrapper.getWrapperForSeries(series.get(Series.ID), repository));
        }
        for (Glob subSeries : selection.getAll(SubSeries.TYPE)) {
          builder.add(SeriesWrapper.getWrapperForSubSeries(subSeries.get(SubSeries.ID), repository));
        }
        for (Glob group : selection.getAll(SeriesGroup.TYPE)) {
          builder.add(SeriesWrapper.getWrapperForSeriesGroup(group.get(SeriesGroup.ID), repository));
        }
        selectionService.select(builder.get());
      }
    }, Series.TYPE, SubSeries.TYPE, SeriesGroup.TYPE);

    parentSelectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        updateViewType();
      }
    }, AnalysisViewType.TYPE);

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/analysis/analysisView.splits",
                                                      repository, directory);
    cards = builder.addCardHandler("cards");
    for (AnalysisViewPanel panel : viewPanels) {
      panel.load(builder);
    }

    return builder;
  }

  public void reset() {
    for (AnalysisViewPanel viewPanel : viewPanels) {
      viewPanel.reset();
    }
    parentSelectionService.select(AnalysisViewType.BUDGET.getGlob());
    referenceMonthId = null;
  }

  private void updateViewType() {
    GlobList selection = parentSelectionService.getSelection(AnalysisViewType.TYPE);
    if (selection.size() != 1) {
      cards.show("budget");
      return;
    }
    switch (AnalysisViewType.get(selection.getFirst())) {
      case BUDGET:
        cards.show("budget");
        break;
      case EVOLUTION:
        cards.show("evolution");
        break;
      case TABLE:
        cards.show("table");
        break;
    }
  }
}


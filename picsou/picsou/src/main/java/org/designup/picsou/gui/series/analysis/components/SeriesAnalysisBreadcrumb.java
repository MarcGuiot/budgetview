package org.designup.picsou.gui.series.analysis.components;

import org.designup.picsou.gui.series.view.SeriesWrapper;
import org.designup.picsou.gui.series.view.SeriesWrapperType;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.TypeChangeSetListener;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.globsframework.utils.exceptions.UnexpectedValue;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class SeriesAnalysisBreadcrumb implements GlobSelectionListener {
  private GlobRepository repository;
  private JEditorPane editor;
  private SelectionService selectionService;

  public SeriesAnalysisBreadcrumb(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.editor = GuiUtils.createReadOnlyHtmlComponent();
    editor.addHyperlinkListener(new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent event) {
        if (!HyperlinkEvent.EventType.ACTIVATED.equals(event.getEventType())) {
          return;
        }

        String href = event.getDescription().trim();
        processClick(href);
      }
    });
    selectionService = directory.get(SelectionService.class);
    selectionService.addListener(this, SeriesWrapper.TYPE);
    repository.addChangeListener(new TypeChangeSetListener(Series.TYPE, UserPreferences.TYPE) {
      public void update(GlobRepository repository) {
        SeriesAnalysisBreadcrumb.this.update();
      }
    });
    update();
  }

  private void processClick(String event) {
    try {
      int wrapperId = Integer.parseInt(event);
      Glob wrapper = repository.find(Key.create(SeriesWrapper.TYPE, wrapperId));
      if (wrapper != null) {
        selectionService.select(wrapper);
      }
    }
    catch (NumberFormatException e) {
      throw new RuntimeException(e);
    }
  }

  public void selectionUpdated(GlobSelection selection) {
    update();
  }

  private void update() {
    GlobList wrappers = selectionService.getSelection(SeriesWrapper.TYPE);
    if (wrappers.size() > 1) {
      editor.setText(Lang.get("seriesAnalysisBreadcrumb.multi"));
      return;
    }
    if (wrappers.isEmpty()) {
      editor.setText(getBudgetLabel());
      return;
    }

    editor.setText(getHtml(wrappers.getFirst()));
  }

  private String getHtml(Glob wrapper) {
    BreadcrumbBuilder builder = new BreadcrumbBuilder();
    switch (SeriesWrapperType.get(wrapper)) {
      case SUMMARY:
        builder.addBudgetLabel();
        break;
      case BUDGET_AREA:
        builder.addBudgetLink();
        builder.addSeparator();
        builder.addBudgetAreaName(wrapper);
        break;
      case SERIES:
        builder.addBudgetLink();
        builder.addSeparator();
        builder.addParentBudgetAreaLink(wrapper);
        builder.addSeparator();
        builder.addSeriesName(wrapper);
        break;
      case SUB_SERIES:
        builder.addBudgetLink();
        builder.addSeparator();
        builder.addParentBudgetAreaLink(wrapper);
        builder.addSeparator();
        builder.addSeriesLink(wrapper);
        break;
    }
    return builder.toString();
  }

  private String getBudgetLabel() {
    switch (AnalysisViewType.get(repository)) {
      case CHARTS:
      case BOTH:
        return Lang.get("seriesAnalysisBreadcrumb.summary.charts");
      case TABLE:
        return Lang.get("seriesAnalysisBreadcrumb.summary.table");
      default:
        throw new UnexpectedValue(AnalysisViewType.get(repository));
    }
  }

  public JEditorPane getEditor() {
    return editor;
  }

  private class BreadcrumbBuilder {
    private StringBuilder builder = new StringBuilder();

    private void addBudgetLabel() {
      builder.append(getBudgetLabel());
    }

    private void addSeriesName(Glob seriesWrapper) {
      Glob series = SeriesWrapper.getSeries(seriesWrapper, repository);
      addBold(series.get(Series.NAME));
    }

    private void addSeriesLink(Glob subSeriesWrapper) {
      Glob subSeries = SeriesWrapper.getSubSeries(subSeriesWrapper, repository);
      Glob series = repository.findLinkTarget(subSeries, SubSeries.SERIES);
      addLink(subSeriesWrapper.get(SeriesWrapper.PARENT), series.get(Series.NAME));
    }

    private void addBudgetLink() {
      addLink(SeriesWrapper.BALANCE_SUMMARY_ID, Lang.get("seriesAnalysisBreadcrumb.top"));
    }

    private void addParentBudgetAreaLink(Glob wrapper) {
      BudgetArea budgetArea;

      SeriesWrapperType wrapperType = SeriesWrapperType.get(wrapper);
      switch (wrapperType) {
        case SERIES: {
          Glob series = SeriesWrapper.getSeries(wrapper, repository);
          budgetArea = Series.getBudgetArea(series);
          break;
        }
        case SUB_SERIES: {
          Glob subSeries = SeriesWrapper.getSubSeries(wrapper, repository);
          Glob series = repository.findLinkTarget(subSeries, SubSeries.SERIES);
          budgetArea = Series.getBudgetArea(series);
          break;
        }
        default:
          throw new InvalidParameter("Unexpected type " + wrapperType + " for " + wrapper);
      }

      Glob wrapperForBudgetArea = SeriesWrapper.getWrapperForBudgetArea(budgetArea, repository);
      addLink(wrapperForBudgetArea.get(SeriesWrapper.ID), budgetArea.getLabel());
    }

    private void addBudgetAreaName(Glob wrapperForBudgetArea) {
      BudgetArea budgetArea = SeriesWrapper.getBudgetArea(wrapperForBudgetArea);
      addBold(budgetArea.getLabel());
    }

    private void addBold(String text) {
      builder
        .append("<b>")
        .append(text)
        .append("</b>");
    }

    private void addLink(Integer seriesWrapperId, String text) {
      builder
        .append("<a href='")
        .append(seriesWrapperId)
        .append("'>")
        .append(text)
        .append("</a>");
    }

    private void addSeparator() {
      builder.append(" &gt; ");
    }

    public String toString() {
      return builder.toString();
    }
  }
}

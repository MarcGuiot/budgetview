package com.budgetview.gui.analysis.budget;

import com.budgetview.gui.series.view.SeriesWrapper;
import com.budgetview.gui.series.view.SeriesWrapperType;
import com.budgetview.model.*;
import com.budgetview.utils.Lang;
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
import org.globsframework.utils.exceptions.UnexpectedValue;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class BudgetAnalysisBreadcrumb implements GlobSelectionListener {
  private GlobRepository repository;
  private JEditorPane editor;
  private SelectionService selectionService;

  public BudgetAnalysisBreadcrumb(GlobRepository repository, Directory directory) {
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
        BudgetAnalysisBreadcrumb.this.update();
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
    add(wrapper, builder, true);
    return builder.toString();
  }

  private void add(Glob wrapper, BreadcrumbBuilder builder, boolean tail) {
    switch (SeriesWrapperType.get(wrapper)) {
      case SUMMARY:
        builder.addBudgetLabel();
        break;
      case BUDGET_AREA:
        builder.addBudgetLink();
        builder.addSeparator();
        if (tail) {
          builder.addBudgetAreaName(wrapper);
        }
        else {
          builder.addBudgetAreaLink(wrapper);
        }
        break;
      case SERIES_GROUP:
        add(SeriesWrapper.getParent(wrapper, repository), builder, false);
        builder.addSeparator();
        if (tail) {
          builder.addGroupName(wrapper);
        }
        else {
          builder.addGroupLink(wrapper);
        }
        break;
      case SERIES:
        add(SeriesWrapper.getParent(wrapper, repository), builder, false);
        builder.addSeparator();
        if (tail) {
          builder.addSeriesName(wrapper);
        }
        else {
          builder.addSeriesLink(wrapper);
        }
        break;
      case SUB_SERIES:
        add(SeriesWrapper.getParent(wrapper, repository), builder, false);
        builder.addSeparator();
        builder.addSubSeriesName(wrapper);
        break;
    }
  }

  private String getBudgetLabel() {
    switch (AnalysisViewType.get(repository)) {
      case BUDGET:
      case EVOLUTION:
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

    private void addSeriesLink(Glob seriesWrapper) {
      Glob series = SeriesWrapper.getSeries(seriesWrapper, repository);
      addLink(seriesWrapper.get(SeriesWrapper.ID), series.get(Series.NAME));
    }

    private void addGroupName(Glob seriesWrapper) {
      Glob group = SeriesWrapper.getGroup(seriesWrapper, repository);
      addBold(group.get(SeriesGroup.NAME));
    }

    private void addGroupLink(Glob groupWrapper) {
      Glob group = SeriesWrapper.getGroup(groupWrapper, repository);
      addLink(groupWrapper.get(SeriesWrapper.ID), group.get(SeriesGroup.NAME));
    }

    private void addSubSeriesName(Glob seriesWrapper) {
      Glob subSeries = SeriesWrapper.getSubSeries(seriesWrapper, repository);
      addBold(subSeries.get(SubSeries.NAME));
    }

    private void addBudgetLink() {
      addLink(SeriesWrapper.BALANCE_SUMMARY_ID, Lang.get("seriesAnalysisBreadcrumb.top"));
    }

    private void addBudgetAreaName(Glob wrapperForBudgetArea) {
      BudgetArea budgetArea = SeriesWrapper.getBudgetArea(wrapperForBudgetArea);
      addBold(budgetArea.getLabel());
    }

    private void addBudgetAreaLink(Glob wrapperForBudgetArea) {
      BudgetArea budgetArea = SeriesWrapper.getBudgetArea(wrapperForBudgetArea);
      addLink(wrapperForBudgetArea.get(SeriesWrapper.ID), budgetArea.getLabel());
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

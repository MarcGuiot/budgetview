package com.budgetview.desktop.analysis;

import com.budgetview.desktop.budget.summary.BudgetAreaSummaryComputer;
import com.budgetview.desktop.model.AccountStat;
import com.budgetview.desktop.model.BudgetStat;
import com.budgetview.desktop.model.SavingsBudgetStat;
import com.budgetview.desktop.model.SeriesStat;
import com.budgetview.desktop.series.view.SeriesWrapper;
import com.budgetview.desktop.series.view.SeriesWrapperType;
import com.budgetview.desktop.utils.AmountColors;
import com.budgetview.model.Account;
import com.budgetview.model.BudgetArea;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.painters.FillPainter;
import org.globsframework.gui.splits.painters.GradientPainter;
import org.globsframework.gui.splits.painters.Paintable;
import org.globsframework.gui.splits.painters.Painter;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;

public class SeriesChartsColors implements ColorChangeListener {

  private GlobRepository parentRepository;

  private Color summaryText;
  private Painter summaryBg;
  private Painter summaryCurrentBg;
  private Color uncategorizedText;

  private Color budgetAreaText;
  private Painter budgetAreaBg;
  private Painter budgetAreaCurrentBg;

  private Color seriesText;
  private Color seriesErrorText;
  private Painter seriesEvenBg;
  private Painter seriesOddBg;
  private Painter seriesCurrentEvenBg;
  private Painter seriesCurrentOddBg;
  private Color subSeriesText;

  private Color selectionText;
  private Painter selectionBackground;
  private Painter plainSelectionBackground;

  private AmountColors amountColors;
  private BudgetAreaColorUpdater budgetAreaColorUpdater;

  public SeriesChartsColors(GlobRepository parentRepository, Directory directory) {
    this.parentRepository = parentRepository;
    ColorService colorService = directory.get(ColorService.class);
    colorService.addListener(this);

    amountColors = new AmountColors(directory);
    budgetAreaColorUpdater = new BudgetAreaColorUpdater(parentRepository, directory);

    selectionBackground = new GradientPainter("seriesEvolution.selection.bg.top",
                                              "seriesEvolution.selection.bg.bottom",
                                              "seriesEvolution.selection.border",
                                              colorService);
    summaryBg = new FillPainter("seriesEvolution.summary.bg", colorService);
    summaryCurrentBg = new FillPainter("seriesEvolution.summary.bg.current", colorService);
    budgetAreaBg = new GradientPainter("seriesEvolution.budgetArea.bg.top",
                                       "seriesEvolution.budgetArea.bg.bottom",
                                       "seriesEvolution.budgetArea.bg.border",
                                       colorService);
    budgetAreaCurrentBg = new GradientPainter("seriesEvolution.budgetArea.current.bg.top",
                                              "seriesEvolution.budgetArea.current.bg.bottom",
                                              "seriesEvolution.budgetArea.current.bg.border",
                                              colorService);
    seriesEvenBg = new FillPainter("seriesEvolution.series.bg.even", colorService);
    seriesOddBg = new FillPainter("seriesEvolution.series.bg.odd", colorService);
    seriesCurrentEvenBg = new FillPainter("seriesEvolution.series.bg.current.even", colorService);
    seriesCurrentOddBg = new FillPainter("seriesEvolution.series.bg.current.odd", colorService);
    plainSelectionBackground = new FillPainter("seriesEvolution.table.selected.bg", colorService);
  }

  public void colorsChanged(ColorLocator colors) {
    summaryText = colors.get("seriesEvolution.summary.text");
    budgetAreaText = colors.get("seriesEvolution.budgetArea.text");
    seriesText = colors.get("seriesEvolution.series.text.normal");
    seriesErrorText = colors.get("seriesEvolution.series.text.error");
    subSeriesText = colors.get("seriesEvolution.subSeries.text");
    selectionText = colors.get("seriesEvolution.selected.fg");
    uncategorizedText = colors.get("seriesEvolution.uncategorized");
  }

  public void setColors(Glob seriesWrapper, int row, int monthOffset, Integer referenceMonthId,
                        boolean selected, JComponent component, Paintable background) {
    if (selected) {
      if (SeriesWrapperType.BUDGET_AREA.isOfType(seriesWrapper)
          && !seriesWrapper.get(SeriesWrapper.ID).equals(SeriesWrapper.UNCATEGORIZED_ID)) {
        setColors(component, selectionText, background, selectionBackground);
      }
      else {
        setColors(component, selectionText, background, plainSelectionBackground);
      }
      return;
    }

    switch (SeriesWrapperType.get(seriesWrapper)) {

      case BUDGET_AREA:
        if (SeriesWrapper.UNCATEGORIZED_ID.equals(seriesWrapper.get(SeriesWrapper.ID))) {
          setColors(component, uncategorizedText, summaryBg, summaryCurrentBg, background, monthOffset);
        }
        else {
          setBudgetAreaColors(component, background, monthOffset, referenceMonthId, seriesWrapper);
        }
        break;

      case SERIES:
      case SERIES_GROUP:
        setSeriesColors(component, background, row, monthOffset, referenceMonthId, seriesWrapper);
        break;

      case SUB_SERIES:
        setSubSeriesColors(component, background, row, monthOffset);
        break;

      case SUMMARY:
        setSummaryColors(component, background, monthOffset, referenceMonthId, seriesWrapper);
        break;
    }
  }

  private void setSummaryColors(JComponent component, Paintable panel, int monthOffset, Integer referenceMonthId, Glob wrapper) {
    final Integer wrapperId = wrapper.get(SeriesWrapper.ID);
    setColors(component, component != null ? getSummaryForeground(wrapperId, referenceMonthId) : null,
              summaryBg, summaryCurrentBg, panel, monthOffset);
  }

  private Color getSummaryForeground(Integer wrapperId, Integer referenceMonthId) {
    if (SeriesWrapper.MAIN_POSITION_SUMMARY_ID.equals(wrapperId)) {
      Glob budgetStat = parentRepository.find(Key.create(AccountStat.ACCOUNT, Account.MAIN_SUMMARY_ACCOUNT_ID,
                                                         AccountStat.MONTH, referenceMonthId));
      if (budgetStat != null) {
        Double amount = budgetStat.get(AccountStat.END_POSITION, 0.);
        return amountColors.getTextColor(amount, summaryText);
      }
    }

    if (SeriesWrapper.SAVINGS_POSITION_SUMMARY_ID.equals(wrapperId)) {
      Glob budgetStat = SavingsBudgetStat.findSummary(referenceMonthId, parentRepository);
      if (budgetStat != null) {
        final Double position = budgetStat.get(SavingsBudgetStat.END_OF_MONTH_POSITION);
        if ((position != null) && (position < 0)) {
          return uncategorizedText;
        }
      }
    }

    return summaryText;
  }

  private void setBudgetAreaColors(JComponent component, Paintable panel, int monthOffset, Integer referenceMonthId,
                                   Glob wrapper) {
    Color foreground = component != null ? getBudgetAreaForeground(wrapper, referenceMonthId) : null;
    setColors(component, foreground, budgetAreaBg, budgetAreaCurrentBg, panel, monthOffset);
  }

  private Color getBudgetAreaForeground(Glob wrapper, Integer referenceMonthId) {
    Glob budgetStat = parentRepository.find(Key.create(BudgetStat.TYPE, referenceMonthId));
    if (budgetStat == null) {
      return budgetAreaText;
    }

    BudgetArea budgetArea = BudgetArea.get(wrapper.get(SeriesWrapper.ITEM_ID));
    budgetAreaColorUpdater.update(new GlobList(budgetStat), budgetArea);
    return budgetAreaColorUpdater.getForeground();
  }

  private void setSeriesColors(JComponent component, Paintable panel,
                               Integer row, int monthOffset, int referenceMonthId, Glob wrapper) {
    Color foreground = component != null ? getSeriesForeground(referenceMonthId, wrapper) : null;
    setSeriesColors(component, panel, row, monthOffset, foreground);
  }

  private void setSubSeriesColors(JComponent component, Paintable panel, Integer row, int monthOffset) {
    setSeriesColors(component, panel, row, monthOffset, subSeriesText);
  }

  private void setSeriesColors(JComponent component, Paintable panel, Integer row, int monthOffset, Color foreground) {
    if (row % 2 == 0) {
      setColors(component, foreground, seriesEvenBg, seriesCurrentEvenBg, panel, monthOffset);
    }
    else {
      setColors(component, foreground, seriesOddBg, seriesCurrentOddBg, panel, monthOffset);
    }
  }

  private Color getSeriesForeground(int referenceMonthId, Glob wrapper) {
    final Glob seriesStat = parentRepository.find(SeriesWrapper.createSeriesStatKey(wrapper, referenceMonthId));
    if (seriesStat == null) {
      return seriesText;
    }

    Double actual = seriesStat.get(SeriesStat.ACTUAL_AMOUNT);
    Double planned = seriesStat.get(SeriesStat.PLANNED_AMOUNT);
    if ((actual != null) && (planned != null)) {
      if ((actual < 0) && (actual < planned)) {
        return seriesErrorText;
      }
    }
    return seriesText;
  }

  private void setColors(JComponent component, Color textColor,
                         Painter bgPainter, Painter currentMonthBgPainter,
                         Paintable panel, int monthOffset) {
    if (monthOffset == 0) {
      setColors(component, textColor, panel, currentMonthBgPainter);
    }
    else {
      setColors(component, textColor, panel, bgPainter);
    }
  }

  private void setColors(JComponent component, Color componentForeground,
                         Paintable background, Painter backgroundPainter) {
    if (component != null) {
      component.setForeground(componentForeground);
    }
    background.setPainter(backgroundPainter);
  }

  private class BudgetAreaColorUpdater extends BudgetAreaSummaryComputer {

    private Color foreground;

    public BudgetAreaColorUpdater(GlobRepository repository, Directory directory) {
      super(repository, directory);
    }

    protected void clearComponents() {
    }

    protected void updateComponents(BudgetArea budgetArea) {
      if (hasErrorOverrun()) {
        foreground = errorOverrunAmountColor;
      }
      else {
        foreground = budgetAreaText;
      }
    }

    public Color getForeground() {
      return foreground;
    }
  }
}

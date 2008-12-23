package org.designup.picsou.gui.series.evolution;

import org.designup.picsou.gui.model.BalanceStat;
import org.designup.picsou.gui.model.SavingsBalanceStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.gui.series.view.SeriesWrapper;
import org.designup.picsou.gui.series.view.SeriesWrapperType;
import org.designup.picsou.gui.utils.AmountColors;
import org.designup.picsou.gui.utils.PicsouColors;
import org.designup.picsou.gui.budget.BudgetAreaSummaryComputer;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountPositionThreshold;
import org.designup.picsou.model.BudgetArea;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.painters.FillPainter;
import org.globsframework.gui.splits.painters.GradientPainter;
import org.globsframework.gui.splits.painters.Paintable;
import org.globsframework.gui.splits.painters.Painter;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.GlobList;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;

public class SeriesEvolutionColors implements ColorChangeListener {

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

  private Color selectionText;
  private Painter selectionBackground;
  private Painter plainSelectionBackground;

  private AmountColors amountColors;
  private BudgetAreaColorUpdater budgetAreaColorUpdater;

  public SeriesEvolutionColors(GlobRepository parentRepository, Directory directory) {
    this.parentRepository = parentRepository;
    ColorService colorService = directory.get(ColorService.class);
    colorService.addListener(this);

    amountColors = new AmountColors(directory);
    budgetAreaColorUpdater = new BudgetAreaColorUpdater(parentRepository, directory);

    selectionBackground = PicsouColors.createTableSelectionBackgroundPainter(colorService);
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
    plainSelectionBackground = new FillPainter(PicsouColors.TRANSACTION_SELECTED_BG, colorService);
  }

  public void colorsChanged(ColorLocator colors) {
    summaryText = colors.get("seriesEvolution.summary.text");
    budgetAreaText = colors.get("seriesEvolution.budgetArea.text");
    seriesText = colors.get("seriesEvolution.series.text.normal");
    seriesErrorText = colors.get("seriesEvolution.series.text.error");
    selectionText = colors.get(PicsouColors.CATEGORIES_SELECTED_FG);
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
        setSeriesColors(component, background, row, monthOffset, referenceMonthId, seriesWrapper.get(SeriesWrapper.ITEM_ID));
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
      Glob balanceStat = parentRepository.find(Key.create(BalanceStat.TYPE, referenceMonthId));
      if (balanceStat != null) {
        Double threshold = AccountPositionThreshold.getValue(parentRepository);
        final double diff = balanceStat.get(BalanceStat.END_OF_MONTH_ACCOUNT_POSITION) - threshold;
        return amountColors.getTextColor(diff, summaryText);
      }
    }

    if (SeriesWrapper.SAVINGS_POSITION_SUMMARY_ID.equals(wrapperId)) {
      Glob balanceStat = parentRepository.find(
        Key.create(SavingsBalanceStat.ACCOUNT, Account.SAVINGS_SUMMARY_ACCOUNT_ID,
                   SavingsBalanceStat.MONTH, referenceMonthId));
      if (balanceStat != null) {
        final Double position = balanceStat.get(SavingsBalanceStat.END_OF_MONTH_POSITION);
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
    Glob balanceStat = parentRepository.find(Key.create(BalanceStat.TYPE, referenceMonthId));
    if (balanceStat == null) {
      return budgetAreaText;
    }

    BudgetArea budgetArea = BudgetArea.get(wrapper.get(SeriesWrapper.ITEM_ID));
    budgetAreaColorUpdater.update(new GlobList(balanceStat), budgetArea);
    return budgetAreaColorUpdater.getForeground();
  }

  private void setSeriesColors(JComponent component, Paintable panel,
                               Integer row, int monthOffset, int referenceMonthId, Integer itemId) {
    Color foreground = component != null ? getSeriesForeground(referenceMonthId, itemId) : null;
    if (row % 2 == 0) {
      setColors(component, foreground, seriesEvenBg, seriesCurrentEvenBg, panel, monthOffset);
    }
    else {
      setColors(component, foreground, seriesOddBg, seriesCurrentOddBg, panel, monthOffset);
    }
  }

  private Color getSeriesForeground(int referenceMonthId, Integer itemId) {
    final Glob seriesStat = parentRepository.find(Key.create(SeriesStat.SERIES, itemId,
                                                             SeriesStat.MONTH, referenceMonthId));
    if (seriesStat == null) {
      return seriesText;
    }

    Double observed = seriesStat.get(SeriesStat.AMOUNT);
    Double planned = seriesStat.get(SeriesStat.PLANNED_AMOUNT);
    if ((observed != null) && (planned != null)) {
      if ((observed < 0) && (observed < planned)) {
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

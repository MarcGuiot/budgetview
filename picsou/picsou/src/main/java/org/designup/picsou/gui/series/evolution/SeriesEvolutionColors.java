package org.designup.picsou.gui.series.evolution;

import org.designup.picsou.gui.series.view.SeriesWrapper;
import org.designup.picsou.gui.series.view.SeriesWrapperType;
import org.designup.picsou.gui.utils.PicsouColors;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.painters.FillPainter;
import org.globsframework.gui.splits.painters.GradientPainter;
import org.globsframework.gui.splits.painters.Paintable;
import org.globsframework.gui.splits.painters.Painter;
import org.globsframework.model.Glob;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;

public class SeriesEvolutionColors implements ColorChangeListener {

  private Color summaryText;
  private Painter summaryBg;
  private Painter summaryCurrentBg;
  private Color uncategorizedText;

  private Color budgetAreaText;
  private Painter budgetAreaBg;
  private Painter budgetAreaCurrentBg;

  private Color seriesText;
  private Painter seriesEvenBg;
  private Painter seriesOddBg;
  private Painter seriesCurrentEvenBg;
  private Painter seriesCurrentOddBg;

  private Color selectionText;
  private Painter selectionBackground;
  private Painter plainSelectionBackground;

  public SeriesEvolutionColors(Directory directory) {
    ColorService colorService = directory.get(ColorService.class);
    colorService.addListener(this);
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
    seriesText = colors.get("seriesEvolution.series.text");
    selectionText = colors.get(PicsouColors.CATEGORIES_SELECTED_FG);
    uncategorizedText = colors.get("seriesEvolution.uncategorized");
  }

  public void setColors(Glob seriesWrapper, int row, int monthOffset, boolean selected,
                        JComponent component, Paintable background) {
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
        if (SeriesWrapper.ALL_ID.equals(seriesWrapper.get(SeriesWrapper.ID))) {
          setSummaryColors(component, background, monthOffset);
        }
        else if (SeriesWrapper.UNCATEGORIZED_ID.equals(seriesWrapper.get(SeriesWrapper.ID))) {
          setColors(uncategorizedText, summaryBg, summaryCurrentBg, component, background, monthOffset);
        }
        else {
          setBudgetAreaColors(component, background, monthOffset);
        }
        break;

      case SERIES:
        setSeriesColors(component, background, monthOffset, row);
        break;

      case SUMMARY:
        setSummaryColors(component, background, monthOffset);
        break;
    }
  }

  private void setSummaryColors(JComponent component, Paintable panel, int monthOffset) {
    setColors(summaryText, summaryBg, summaryCurrentBg, component, panel, monthOffset);
  }

  private void setBudgetAreaColors(JComponent component, Paintable panel, int monthOffset) {
    setColors(budgetAreaText, budgetAreaBg, budgetAreaCurrentBg, component, panel, monthOffset);
  }

  private void setSeriesColors(JComponent component, Paintable panel, int monthOffset, int row) {
    if (row % 2 == 0) {
      setColors(seriesText, seriesEvenBg, seriesCurrentEvenBg, component, panel, monthOffset);
    }
    else {
      setColors(seriesText, seriesOddBg, seriesCurrentOddBg, component, panel, monthOffset);
    }
  }

  private void setColors(Color textColor, Painter bgPainter, Painter currentMonthBgPainter,
                         JComponent component, Paintable panel, int monthOffset) {
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
}

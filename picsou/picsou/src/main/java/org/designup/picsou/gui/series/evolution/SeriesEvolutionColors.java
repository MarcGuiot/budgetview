package org.designup.picsou.gui.series.evolution;

import org.designup.picsou.gui.series.view.SeriesWrapper;
import org.designup.picsou.gui.utils.PicsouColors;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.painters.FillPainter;
import org.globsframework.gui.splits.painters.Paintable;
import org.globsframework.gui.splits.painters.Painter;
import org.globsframework.model.Glob;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;

public class SeriesEvolutionColors implements ColorChangeListener {

  private Color balanceText;
  private Painter balanceBg;
  private Painter balanceCurrentBg;

  private Color positionText;
  private Painter positionBg;
  private Painter positionCurrentBg;

  private Color budgetAreaText;
  private Painter budgetAreaBg;
  private Painter budgetAreaCurrentBg;

  private Color seriesText;
  private Painter seriesEvenBg;
  private Painter seriesOddBg;
  private Painter seriesCurrentBg;

  private Painter emptyCell;
  private Painter emptyCurrentCell;

  private Color selectionText;

  private Painter selectionBackground;

  public SeriesEvolutionColors(Directory directory) {
    ColorService colorService = directory.get(ColorService.class);
    colorService.addListener(this);
    selectionBackground = PicsouColors.createTableSelectionBackgroundPainter(colorService);
    balanceBg = new FillPainter("seriesEvolution.balance.bg", colorService);
    balanceCurrentBg = new FillPainter("seriesEvolution.balance.bg.current", colorService);
    positionBg = new FillPainter("seriesEvolution.position.bg", colorService);
    positionCurrentBg = new FillPainter("seriesEvolution.position.bg.current", colorService);
    budgetAreaBg = new FillPainter("seriesEvolution.budgetArea.bg", colorService);
    budgetAreaCurrentBg = new FillPainter("seriesEvolution.budgetArea.bg.current", colorService);
    seriesEvenBg = new FillPainter("seriesEvolution.series.bg.even", colorService);
    seriesOddBg = new FillPainter("seriesEvolution.series.bg.odd", colorService);
    seriesCurrentBg = new FillPainter("seriesEvolution.series.bg.current", colorService);
    emptyCell = new FillPainter("seriesEvolution.empty.bg", colorService);
    emptyCurrentCell = new FillPainter("seriesEvolution.empty.bg.current", colorService);
  }

  public void colorsChanged(ColorLocator colors) {
    balanceText = colors.get("seriesEvolution.balance.text");
    positionText = colors.get("seriesEvolution.position.text");
    budgetAreaText = colors.get("seriesEvolution.budgetArea.text");
    seriesText = colors.get("seriesEvolution.series.text");
    selectionText = colors.get(PicsouColors.CATEGORIES_SELECTED_FG);
  }

  public void setColors(Glob seriesWrapper, int row, int monthOffset, boolean selected,
                        JComponent component, Paintable background) {
    if (selected) {
      setColors(component, selectionText, background, selectionBackground);
    }
    else if (Boolean.TRUE.equals(seriesWrapper.get(SeriesWrapper.IS_BUDGET_AREA))) {
      if (SeriesWrapper.ALL_ID.equals(seriesWrapper.get(SeriesWrapper.ID))) {
        setBalanceColors(component, background, monthOffset);
      }
      else if (SeriesWrapper.UNCATEGORIZED_ID.equals(seriesWrapper.get(SeriesWrapper.ID))) {
        setBalanceColors(component, background, monthOffset);
      }
      else {
        setBudgetAreaColors(component, background, monthOffset);
      }
    }
    else {
      setSeriesColors(component, background, monthOffset, row);
    }
  }

  private void setBalanceColors(JComponent component, Paintable panel, int monthOffset) {
    setColors(balanceText, balanceBg, balanceCurrentBg, component, panel, monthOffset);
  }

  private void setPositionColors(JComponent component, Paintable panel, int monthOffset) {
    setColors(positionText, positionBg, positionCurrentBg, component, panel, monthOffset);
  }

  private void setBudgetAreaColors(JComponent component, Paintable panel, int monthOffset) {
    setColors(budgetAreaText, budgetAreaBg, budgetAreaCurrentBg, component, panel, monthOffset);
  }

  private void setSeriesColors(JComponent component, Paintable panel, int monthOffset, int row) {
    if (row % 2 == 0) {
      setColors(seriesText, seriesEvenBg, seriesCurrentBg, component, panel, monthOffset);
    }
    else {
      setColors(seriesText, seriesOddBg, seriesCurrentBg, component, panel, monthOffset);
    }
  }

  private void setColors(JComponent component, Color foreground,
                         Paintable background, Painter backgroundPainter) {
    if (component != null) {
      component.setForeground(foreground);
    }
    background.setPainter(backgroundPainter);
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
}

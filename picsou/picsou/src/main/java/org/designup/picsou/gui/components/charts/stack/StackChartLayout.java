package org.designup.picsou.gui.components.charts.stack;

public interface StackChartLayout {

  int barTextX(String text, boolean selected);

  int labelTextX(String text, boolean selected);

  int barX();

  int blockWidth();

  int blockX();
}

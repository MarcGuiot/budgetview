package com.budgetview.desktop.components.charts.stack;

public interface StackChartLayout {

  int barTextX(String text);

  int labelTextX(String text, boolean selected);

  int barX();

  int blockWidth();

  int blockX();
}

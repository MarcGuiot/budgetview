package com.budgetview.gui.printing.utils;

import com.budgetview.gui.printing.PrintStyle;

import java.awt.*;

public interface PageBlock {

  int getHeight();

  void print(Dimension area, Graphics2D g2, PrintStyle style);

  int getNeededHeight();
}

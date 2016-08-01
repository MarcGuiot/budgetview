package com.budgetview.gui.time.tooltip;

public interface TimeViewMouseHandler {
  void enterMonth(int monthId);

  void enterYear(int year);

  void leave();
}

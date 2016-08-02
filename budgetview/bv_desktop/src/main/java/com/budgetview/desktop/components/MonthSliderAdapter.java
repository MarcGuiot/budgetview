package com.budgetview.desktop.components;

import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;

public interface MonthSliderAdapter {

  String getText(Glob glob, GlobRepository repository);

  String getMaxText();

  int getCurrentMonth(Glob glob, GlobRepository repository);

  void setMonth(Glob glob, int selectedMonthId, GlobRepository repository);
}

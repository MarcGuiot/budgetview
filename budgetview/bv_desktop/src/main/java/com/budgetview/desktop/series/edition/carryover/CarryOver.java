package com.budgetview.desktop.series.edition.carryover;

public interface CarryOver {
  int getMonth();

  double getAvailable();

  double getNewPlanned();

  double getCarriedOver();

  double getRemainder();
}

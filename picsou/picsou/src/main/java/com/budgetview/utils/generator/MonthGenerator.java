package com.budgetview.utils.generator;

abstract class MonthGenerator {
  protected final int accountId;

  protected MonthGenerator(int accountId) {
    this.accountId = accountId;
  }

  abstract void run(Integer month, int maxMonth);
}

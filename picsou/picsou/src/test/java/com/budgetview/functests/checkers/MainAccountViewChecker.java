package com.budgetview.functests.checkers;

import org.uispec4j.Window;

public class MainAccountViewChecker extends AccountViewPanelChecker<MainAccountViewChecker> {

  public MainAccountViewChecker(Window window) {
    super(window, "mainAccounts");
  }

  public MainAccountViewChecker setAsSavings(String name, String newName) {
    edit(name)
      .setAsSavings()
      .setName(newName)
      .validate();
    return this;
  }
}

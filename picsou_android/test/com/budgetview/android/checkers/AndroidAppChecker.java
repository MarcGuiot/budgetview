package com.budgetview.android.checkers;

import org.robolectric.Robolectric;

public class AndroidAppChecker {
  public AndroidAppChecker() {
    Robolectric.application.onCreate();
  }

  public HomeChecker start() {
    return new HomeChecker();
  }
}

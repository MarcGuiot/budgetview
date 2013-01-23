package com.budgetview.android.checkers;

import com.xtremelabs.robolectric.Robolectric;

public class AndroidAppChecker {
  public AndroidAppChecker() {
    Robolectric.application.onCreate();

  }

  public HomeChecker start() {
    return new HomeChecker();
  }
}

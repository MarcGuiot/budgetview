package org.designup.picsou.functests.checkers;

import org.uispec4j.*;
import static org.uispec4j.assertion.UISpecAssert.assertThat;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

import javax.management.monitor.CounterMonitorMBean;

public class MainAccountViewChecker extends AccountViewChecker<MainAccountViewChecker> {

  public MainAccountViewChecker(Panel window) {
    super(window, "mainAccountView");
  }
}

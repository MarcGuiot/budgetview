package org.designup.picsou.functests.checkers;

import org.designup.picsou.model.ColorTheme;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowInterceptor;

import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class DevOptionsChecker extends GuiChecker {
  private Window window;

  public static DevOptionsChecker open(Trigger trigger) {
    return new DevOptionsChecker(WindowInterceptor.getModalDialog(trigger));
  }

  private DevOptionsChecker(Window window) {
    this.window = window;
  }

  public DevOptionsChecker setPeriodInMonth(int count){
    window.getComboBox("period").select(Integer.toString(count));
    assertThat(window.getComboBox("period").selectionEquals(Integer.toString(count)));
    return this;
  }

  public DevOptionsChecker setMonthBack(int count){
    window.getComboBox("monthBack").select(Integer.toString(count));
    assertThat(window.getComboBox("monthBack").selectionEquals(Integer.toString(count)));
    return this;
  }
  
  public void validate() {
    window.getButton("OK").click();
    UISpecAssert.assertFalse(window.isVisible());
  }
}

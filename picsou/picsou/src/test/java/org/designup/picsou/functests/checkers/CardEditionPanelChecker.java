package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.model.Month;
import org.designup.picsou.utils.Lang;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.*;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;
import java.awt.*;

public class CardEditionPanelChecker extends GuiChecker {
  Panel panel;

  public CardEditionPanelChecker(Panel dialog) {
    this.panel = dialog;
  }

  public CardEditionPanelChecker checkMonth(int monthId) {
    getDeferredLabel(monthId);
    return this;
  }

  public CardEditionPanelChecker checkFromBeginning() {
    panel.getTextBox("From begining");
    return this;
  }

  private TextBox getDeferredLabel(int monthId) {
    return panel.getTextBox(getLabel(monthId));
  }

  private String getLabel(int monthId) {
    if (monthId == 0) {
      return "From begining";
    }
    String month = Month.getFullMonthLabel(monthId);
    String labelForMonth;
    if (Lang.find("account.deferred.repeat.label." + Month.toMonth(monthId)) != null) {
      labelForMonth = Lang.get("account.deferred.repeat.label." + Month.toMonth(monthId), Month.toYear(monthId));
    }
    else {
      labelForMonth = Lang.get("account.deferred.repeat.label", month, Integer.toString(Month.toYear(monthId)));
    }
    return labelForMonth;
  }

  public CardEditionPanelChecker setDayFromBegining(int day) {
    setDay(day, getDeferredLabel(0));
    return this;
  }

  public CardEditionPanelChecker setDay(int monthId, int day) {
    setDay(day, getDeferredLabel(monthId));
    return this;
  }

  private void setDay(int day, TextBox box) {
    Panel container = box.getContainer("deferredPeriodPanel");
    container.getComboBox("day").select(Integer.toString(day));
  }

  public CardEditionPanelChecker changeMonth(int monthId, final int newMonth) {
    TextBox box = getDeferredLabel(monthId);
    Panel panel = box.getContainer("deferredPeriodPanel");
    WindowInterceptor.init(panel.getButton("changeDeferredMonthAction").triggerClick())
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          MonthChooserChecker monthChooser = new MonthChooserChecker(window);
          return monthChooser.triggerMonth(newMonth);
        }
      }).run();
    return this;
  }

  public CardEditionPanelChecker checkFromBeginningDay(int day) {
    checkDay(day, getDeferredLabel(0));
    return this;
  }

  public CardEditionPanelChecker checkBeginningUnchangeable() {
    TextBox box = getDeferredLabel(0);
    Panel panel = box.getContainer("deferredPeriodPanel");
    assertFalse(panel.getButton("removePeriod").isVisible());
    assertFalse(panel.getButton("changeDeferredMonthAction").isVisible());
    return this;
  }

  private void checkDay(int day, TextBox box) {
    Panel panel = box.getContainer("deferredPeriodPanel");
    assertThat(panel.getComboBox("day").selectionEquals(Integer.toString(day)));
  }

  public CardEditionPanelChecker checkDay(int monthId, int day) {
    TextBox box = getDeferredLabel(monthId);
    checkDay(day, box);
    return this;
  }

  public CardEditionPanelChecker delete(int monthId) {
    TextBox box = getDeferredLabel(monthId);
    Panel panel = box.getContainer("deferredPeriodPanel");
    panel.getButton("removePeriod").click();
    return this;
  }

  public CardEditionPanelChecker addMonth() {
    panel.getButton("createNewPeriod").click();
    return this;
  }

  public CardEditionPanelChecker checkPeriod(Integer[][] periods) {
    Component[] swingComponents = panel.getSwingComponents(JLabel.class, "From ");
    int index = 0;
    for (Integer[] period : periods) {
      if (index >= swingComponents.length) {
        Assert.fail("" + period[0] + "/" + period[1] + " unexpected");
      }
      assertThat(new TextBox((JLabel)swingComponents[index]).textEquals(getLabel(period[0])));
      checkDay(period[0], period[1]);
      index++;
    }
    if (index < swingComponents.length) {
      String message = "Actual : ";
      while (index < swingComponents.length) {
        message += ((JLabel)swingComponents[index]).getText() + "\n";
        index++;
      }
      Assert.fail(message);
    }
    return this;
  }
}
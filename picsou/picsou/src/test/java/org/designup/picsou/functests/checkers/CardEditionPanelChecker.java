package org.designup.picsou.functests.checkers;

import org.uispec4j.TextBox;
import org.uispec4j.Panel;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;
import org.uispec4j.interception.WindowInterceptor;
import org.uispec4j.interception.WindowHandler;
import org.designup.picsou.model.Month;
import org.designup.picsou.utils.Lang;

import javax.swing.*;
import java.awt.*;

import junit.framework.Assert;

public class CardEditionPanelChecker extends GuiChecker{
  Panel panel;

  public CardEditionPanelChecker(Panel dialog) {
    this.panel = dialog;
  }

  public CardEditionPanelChecker checkMonth(int monthId) {
    getDeferredLabel(monthId);
    return this;
  }

  public CardEditionPanelChecker checkFromBegining() {
    panel.getTextBox("From begining");
    return this;
  }

  private TextBox getDeferredLabel(int monthId) {
    return panel.getTextBox(getLabel(monthId));
  }

  private String getLabel(int monthId) {
    if (monthId == 0){
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
    JPanel panel = (JPanel)box.getContainer().getAwtContainer();
    Panel panelChecker = new Panel(panel);
    panelChecker.getComboBox("day").select(Integer.toString(day));
  }

  public CardEditionPanelChecker changeMonth(int monthId, final int newMonth) {
    TextBox box = getDeferredLabel(monthId);
    JPanel panel = (JPanel)box.getContainer().getAwtContainer();
    Panel panelChecker = new Panel(panel);
    WindowInterceptor.init(panelChecker.getButton("changeDeferredMonthAction").triggerClick())
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          MonthChooserChecker monthChooserChecker = new MonthChooserChecker(window);
          return monthChooserChecker.triggerMonth(newMonth);
        }
      }).run();
    return this;
  }

  public CardEditionPanelChecker checkFromBeginingDay(int day) {
    checkDay(day, getDeferredLabel(0));
    return this;
  }

  public CardEditionPanelChecker checkBeginingUnchangable() {
    TextBox box = getDeferredLabel(0);
    JPanel panel = (JPanel)box.getContainer().getAwtContainer();
    Panel panelChecker = new Panel(panel);
    assertFalse(panelChecker.getButton("removePeriod").isVisible());
    assertFalse(panelChecker.getButton("changeDeferredMonthAction").isVisible());
    return this;
  }

  private void checkDay(int day, TextBox box) {
    JPanel panel = (JPanel)box.getContainer().getAwtContainer();
    Panel panelChecker = new Panel(panel);
    assertThat(panelChecker.getComboBox("day").selectionEquals(Integer.toString(day)));
  }

  public CardEditionPanelChecker checkDay(int monthId, int day) {
    TextBox box = getDeferredLabel(monthId);
    checkDay(day, box);
    return this;
  }

  public CardEditionPanelChecker delete(int monthId) {
    TextBox box = getDeferredLabel(monthId);
    JPanel panel = (JPanel)box.getContainer().getAwtContainer();
    Panel panelChecker = new Panel(panel);
    panelChecker.getButton("removePeriod").click();
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
      if (index >= swingComponents.length){
        Assert.fail("" + period[0] + "/" + period[1] + " unexpected");
      }
      assertThat(new TextBox((JLabel)swingComponents[index]).textEquals(getLabel(period[0])));
      checkDay(period[0], period[1]);
      index++;
    }
    if (index < swingComponents.length){
      String message = "Actual : ";
      while (index < swingComponents.length){
        message += ((JLabel)swingComponents[index]).getText() + "\n";
        index++;
      }
      Assert.fail(message);
    }
    return this;
  }
}

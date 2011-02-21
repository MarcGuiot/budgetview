package org.designup.picsou.functests.checkers;

import org.uispec4j.*;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.uispec4j.assertion.UISpecAssert.*;

public class ProjectViewChecker extends ViewChecker {

  private Panel panel;

  public ProjectViewChecker(Window mainWindow) {
    super(mainWindow);
  }

  public ProjectEditionChecker create() {
    return ProjectEditionChecker.open(getPanel().getButton("createProject"));
  }

  public ProjectEditionChecker edit(String projectName) {
    return ProjectEditionChecker.open(getPanel().getButton(projectName));
  }

  public ProjectViewChecker checkNoProjectShown() {
    List<String> actualNames = getActualNames();
    if (!actualNames.isEmpty()) {
      UISpecAssert.fail("Unexpected projects shown: " + actualNames);
    }
    return this;
  }

  public ProjectViewChecker checkProjectList(String... projectNames) {
    List<String> actualNames = getActualNames();
    org.globsframework.utils.TestUtils.assertEquals(Arrays.asList(projectNames), actualNames);
    return this;
  }

  private List<String> getActualNames() {
    UIComponent[] nameButtons = getPanel().getUIComponents(Button.class, "projectName");
    List<String> actualNames = new ArrayList<String>();
    for (UIComponent nameButton : nameButtons) {
      actualNames.add(nameButton.getLabel());
    }
    return actualNames;
  }

  public ProjectViewChecker checkProject(String projectName, String period, double amount) {
    Panel projectPanel = getPanel(projectName);
    assertThat(projectPanel.getTextBox("projectPeriod").textEquals(period));
    assertThat(projectPanel.getTextBox("projectAmount").textEquals(toString(amount)));
    return this;
  }

  public void checkHintMessageDisplayed() {
    checkComponentVisible(getPanel(), JEditorPane.class, "projectHint", true);
  }

  public void checkHintMessageHidden() {
    checkComponentVisible(getPanel(), JEditorPane.class, "projectHint", false);
  }

  private Panel getPanel(String projectName) {
    UIComponent[] nameButtons = getPanel().getUIComponents(Button.class, "projectName");
    for (UIComponent nameButton : nameButtons) {
      if (nameButton.getLabel().equals(projectName)) {
        return nameButton.getContainer("projectRow");
      }
    }
    fail("Project '" + projectName + "' not found - actual names: " + getActualNames());
    return null;
  }

  private Panel getPanel() {
    if (panel == null) {
      views.selectHome();
      panel = mainWindow.getPanel("projectView");
    }
    return panel;
  }
}

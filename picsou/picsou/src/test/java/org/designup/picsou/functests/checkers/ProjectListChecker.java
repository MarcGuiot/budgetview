package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.gui.projects.components.ProjectButton;
import org.globsframework.utils.TablePrinter;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.UIComponent;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;

import java.util.Arrays;

public class ProjectListChecker extends ViewChecker {

  private Panel projectViewPanel;

  public ProjectListChecker(Window mainWindow) {
    super(mainWindow);
  }

  public void create() {
    getPanel().getButton("createProject").click();
  }

  public void checkNoCurrentProjects() {
    checkProjects("", "currentProjects");
  }

  public void checkCurrentProjects(String expected) {
    checkProjects(expected, "currentProjects");
  }

  public void dumpCurrentProjects() {
    System.out.println("ProjectListChecker.dumpCurrentProjects: \n" + dump("currentProjects"));
  }

  public void checkNoPastProjects() {
    checkProjects("", "pastProjects");
  }

  public void checkPastProjects(String expected) {
    checkProjects(expected, "pastProjects");
  }

  private void checkProjects(String expected, String repeatName) {
    TablePrinter printer = dump(repeatName);
    Assert.assertEquals(expected, printer.toString().trim());
  }

  private TablePrinter dump(String repeatName) {
    UIComponent[] blocks = getPanel().getPanel(repeatName).getUIComponents(Panel.class, "projectBlock");
    TablePrinter printer = new TablePrinter();
    for (UIComponent block : blocks) {
      Panel projectPanel = (Panel)block;
      TextBox name = projectPanel.getTextBox("name");
      ProjectButton projectButton = (ProjectButton)projectPanel.getButton("projectButton").getAwtComponent();
      printer.addRow(name.getText(),
                     projectButton.getMonth(),
                     projectButton.getPlanned(),
                     projectButton.isActive() ? "on" : "off");
    }
    return printer;
  }

  public void checkListShown() {
    UISpecAssert.assertThat(getProjectViewPanel().containsUIComponent(Panel.class, "projectListView"));
  }

  public void checkEditionShown() {
    UISpecAssert.assertThat(getProjectViewPanel().containsUIComponent(Panel.class, "projectEditionView"));
  }

  public void select(String projectName) {
    getPanel().getTextBox(projectName)
      .getContainer("projectBlock")
      .getButton("projectButton")
      .click();
    UISpecAssert.assertThat(getProjectViewPanel().containsUIComponent(Panel.class, "projectEditionView"));
  }

  public void checkNoProjectShown() {
    checkListShown();
    UIComponent[] blocks = getPanel().getUIComponents(Panel.class, "projectBlock");
    if (blocks.length > 0) {
      Assert.fail(blocks.length + "projects unexpectedly shown: " + Arrays.toString(blocks));
    }
  }

  private Panel getPanel() {
    return getProjectViewPanel().getPanel("projectListView");
  }
  private Panel getProjectViewPanel() {
    if (projectViewPanel == null) {
      views.selectHome();
      projectViewPanel = mainWindow.getPanel("projectView");
    }
    return projectViewPanel;
  }
}

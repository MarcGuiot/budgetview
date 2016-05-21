package com.budgetview.functests.checkers;

import com.budgetview.functests.checkers.components.PopupChecker;
import com.budgetview.gui.projects.components.ProjectButton;
import junit.framework.Assert;
import org.globsframework.utils.TablePrinter;
import org.uispec4j.*;
import org.uispec4j.interception.PopupMenuInterceptor;

import javax.swing.*;

import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class ProjectListChecker extends ViewChecker {

  private Panel projectViewPanel;

  public ProjectListChecker(Window mainWindow) {
    super(mainWindow);
  }

  public void checkCreationPageShown() {
    checkComponentVisible(getProjectViewPanel(), JPanel.class, "projectCreationView", true);
  }

  public void checkShown() {
    views.selectHome();
    checkComponentVisible(mainWindow, JPanel.class, "projectView", true);
  }

  public void checkHidden() {
    views.selectHome();
    checkComponentVisible(mainWindow, JPanel.class, "projectView", false);
  }

  public ProjectListChecker checkListPageShown() {
    checkComponentVisible(getProjectViewPanel(), JPanel.class, "projectListView", true);
    return this;
  }

  public void create() {
    Panel projectView = getProjectViewPanel();
    assertThat(projectView.isVisible());
    projectView.getButton("createProject").click();
  }

  public void checkNoCurrentProjects() {
    checkProjects("", "currentProjects");
  }

  public void checkCurrentProjects(String expected) {
    checkProjects(expected, "currentProjects");
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

  private TablePrinter dump(String containerName) {
    UIComponent[] blocks = getPanel().getPanel(containerName).getUIComponents(Panel.class, "projectBlock");
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

  public void checkEditionShown() {
    assertThat(getProjectViewPanel().containsUIComponent(Panel.class, "projectEditionView"));
  }

  public void select(String projectName) {
    getPanel().getTextBox(projectName)
      .getContainer("projectBlock")
      .getButton("projectButton")
      .click();
    assertThat(getProjectViewPanel().containsUIComponent(Panel.class, "projectEditionView"));
  }

  public void delete(final String projectName) {
    views.selectProjects();
    PopupChecker popup = new PopupChecker() {
      protected MenuItem openMenu() {
        Button button = getPanel().getTextBox(projectName)
          .getContainer("projectBlock")
          .getButton("projectButton");
        return PopupMenuInterceptor.run(button.triggerRightClick());
      }
    };
    popup.click("Delete");
  }

  public void checkNoProjectShown() {
    checkListPageShown();
    checkNoCurrentProjects();
    checkNoPastProjects();
  }

  private Panel getPanel() {
    return getProjectViewPanel().getPanel("projectListView");
  }

  private Panel getProjectViewPanel() {
    if (projectViewPanel == null) {
      views.selectProjects();
      projectViewPanel = mainWindow.getPanel("projectView");
    }
    return projectViewPanel;
  }

  public ProjectListChecker checkCurrentProjectsSectionHidden() {
    checkComponentVisible(getPanel(), JPanel.class, "currentProjects", false);
    checkComponentVisible(getPanel(), JLabel.class, "currentProjectsSectionTitle", false);
    return this;
  }

  public ProjectListChecker checkCurrentProjectsSectionShown() {
    checkComponentVisible(getPanel(), JPanel.class, "currentProjects", true);
    checkComponentVisible(getPanel(), JLabel.class, "currentProjectsSectionTitle", true);
    return this;
  }

  public ProjectListChecker checkPastProjectsSectionHidden() {
    checkComponentVisible(getPanel(), JLabel.class, "pastProjectsSectionTitle", false);
    checkComponentVisible(getPanel(), JPanel.class, "pastProjects", false);
    return this;
  }

  public ProjectListChecker checkPastProjectsSectionCollapsed() {
    checkComponentVisible(getPanel(), JLabel.class, "pastProjectsSectionTitle", true);
    checkComponentVisible(getPanel(), JPanel.class, "pastProjectsPanel", false);
    return this;
  }

  public ProjectListChecker checkPastProjectsSectionExpanded() {
    checkComponentVisible(getPanel(), JLabel.class, "pastProjectsSectionTitle", true);
    checkComponentVisible(getPanel(), JPanel.class, "pastProjectsPanel", true);
    return this;
  }

  public ProjectListChecker expandPastProjectsSection() {
    Button toggle = getPanel().getButton("togglePastProjects");
    assertThat(toggle.textEquals("Show"));
    toggle.click();
    return this;
  }

  public ProjectListChecker collapsePastProjectsSection() {
    Button toggle = getPanel().getButton("togglePastProjects");
    assertThat(toggle.textEquals("Hide"));
    toggle.click();
    return this;
  }
}

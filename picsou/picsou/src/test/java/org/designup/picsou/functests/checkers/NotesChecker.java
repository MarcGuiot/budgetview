package org.designup.picsou.functests.checkers;

import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.*;
import org.uispec4j.finder.ComponentMatchers;

import javax.swing.*;

public class NotesChecker extends GuiChecker {
  private Window window;

  public NotesChecker(Window mainWindow) {
    this.window = mainWindow;
  }

  public NotesChecker checkText(String text) {
    UISpecAssert.assertThat(getNotesArea().textEquals(text));
    return this;
  }

  public NotesChecker setText(String text) {
    getNotesArea().setText(text);
    return this;
  }

  private TextBox getNotesArea() {
    return window.getInputTextBox("notesEditor");
  }

  public NotesChecker checkNoHelpMessageDisplayed() {
    assertFalse(getPanel().containsComponent(ComponentMatchers.innerNameIdentity("noData")));
    assertFalse(getPanel().containsComponent(ComponentMatchers.innerNameIdentity("noSeries")));
    return this;
  }

  private Panel getPanel() {
    return window.getPanel("notesView");
  }

  public NotesChecker checkNoDataMessage() {
    return checkMessage("You must import your financial operations", "noDataMessage", "noData");
  }

  public NotesChecker checkNoSeriesMessage() {
    return checkMessage("Use the series wizard:", "noSeriesMessage", "noSeries");
  }

  public void categorize() {
    getPanel().getButton("categorize").click();
  }

  public ImportChecker openImport() {
    return ImportChecker.open(getPanel().getButton("import").triggerClick());
  }

  public HelpChecker openImportHelp() {
    return HelpChecker.open(window.getTextBox("noDataMessage").triggerClickOnHyperlink("import"));
  }

  public SeriesWizardChecker openSeriesWizard() {
    return SeriesWizardChecker.open(getPanel().getButton("openSeriesWizard").triggerClick());
  }

  public void checkSeriesWizardButtonVisible(boolean visible) {
    if (visible) {
      Button button = getPanel().getButton("openSeriesWizard");
      assertThat(and(button.isVisible(), button.isEnabled()));
    }
    else {
      checkComponentVisible(getPanel(), JButton.class, "openSeriesWizard", false);
    }
  }

  private NotesChecker checkMessage(String text, String textBoxName, final String panelName) {
    TextBox textBox = window.getPanel(panelName).getTextBox(textBoxName);
    UISpecAssert.assertThat(textBox.textEquals(text));
    return this;
  }

}

package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.functests.checkers.components.GaugeChecker;
import org.designup.picsou.functests.checkers.components.PopupButton;
import org.designup.picsou.gui.description.Formatting;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

import static org.uispec4j.assertion.UISpecAssert.*;

public class ProjectEditionChecker extends ViewChecker {

  private Panel panel;

  public ProjectEditionChecker(Window mainWindow) {
    super(mainWindow);
  }

  public ProjectEditionChecker checkTitle(String text) {
    assertThat(getPanel().getTextBox("title").textEquals(text));
    return this;
  }

  public ProjectEditionChecker setName(String name) {
    getPanel().getTextBox("projectName").setText(name);
    return this;
  }

  public ProjectEditionChecker checkName(String text) {
    assertThat(getPanel().getButton("projectNameButton").textEquals(text));
    return this;
  }

  public ProjectEditionChecker checkProjectGaugeHidden() {
    assertFalse(getPanel().getPanel("gauge").isVisible());
    assertFalse(getPanel().getPanel("gaugePanel").isVisible());
    return this;
  }

  public ProjectEditionChecker checkProjectGauge(double actual, double planned) {

    assertThat(getPanel().getTextBox("totalActual").textEquals(Formatting.toString(actual)));
    assertThat(getPanel().getTextBox("totalPlanned").textEquals(Formatting.toString(planned)));

    GaugeChecker gauge = new GaugeChecker(getPanel(), "gauge");
    gauge.checkActualValue(actual);
    gauge.checkTargetValue(planned);
    return this;
  }

  public ProjectEditionChecker checkItem(int index, String label, String month, double actual, double planned) {
    ProjectItemViewChecker itemPanel = view(index);
    itemPanel.checkValues(label, month, actual, planned);
    return this;
  }

  public ProjectEditionChecker checkItemCount(int count) {
    Component[] panels = getPanel().getSwingComponents(JPanel.class, "projectItemPanel");
    if (panels.length != count) {
      Assert.fail("Unexpected item count: expected " + count + " but was " + panels.length +
                  " - labels: " + Arrays.asList(getItemLabels()));
    }
    return this;
  }

  public ProjectEditionChecker checkItems(String expected) {
    Assert.assertEquals(expected.trim(), getContent());
    return this;
  }

  public ProjectEditionChecker setItem(int index, String name, int monthId, double amount) {
    edit(index)
      .setLabel(name)
      .setMonth(monthId)
      .setAmount(amount)
      .validate();
    return this;
  }

  public ProjectEditionChecker deleteItem(int index) {
    view(index).delete();
    return this;
  }

  public ProjectEditionChecker addItem() {
    getPanel().getButton("addItem").click();
    return this;
  }

  public ProjectEditionChecker addItem(int index, String label, int month, double amount) {
    addItem();
    edit(index)
      .setLabel(label)
      .setMonth(month)
      .setAmount(amount)
      .validate();
    return this;
  }

  public ProjectEditionChecker checkItemGauge(int index, double actual, double planned) {
    view(index).checkGauge(actual, planned);
    return this;
  }

  private String getContent() {
    checkNoEditedItems();
    StringBuilder builder = new StringBuilder();
    Component[] panels = getPanel().getSwingComponents(JPanel.class, "projectItemPanel");
    for (int i = 0; i < panels.length; i++) {
      view(i).write(builder);
      builder.append("\n");
    }
    return builder.toString().trim();
  }

  private void checkNoEditedItems() {
    java.util.List<String> labels = new ArrayList<String>();
    Component[] editorPanels = getPanel().getSwingComponents(JPanel.class, "projectItemEditionPanel");
    for (Component editorPanel : editorPanels) {
      Panel panel1 = new Panel((JPanel)editorPanel);
      labels.add(panel1.getTextBox("nameField").getText());
    }
    if (labels.size() > 0) {
      Assert.fail("Unexpected edited items: " + labels);
    }
  }

  private String[] getItemLabels() {
    java.util.List<String> labels = new ArrayList<String>();
    for (Component jPanel : getPanel().getSwingComponents(JPanel.class, "projectItemViewPanel")) {
      Panel viewPanel = new Panel((JPanel)jPanel);
      labels.add(viewPanel.getButton("itemButton").getLabel());
    }
    for (Component jPanel : getPanel().getSwingComponents(JPanel.class, "projectItemEditionPanel")) {
      Panel editorPanel = new Panel((JPanel)jPanel);
      labels.add(editorPanel.getTextBox("nameField").getText());
    }
    return labels.toArray(new String[labels.size()]);
  }

  public ProjectEditionChecker checkProjectNameMessage(String expectedMessage) {
    Panel editor = getPanel().getPanel("projectNameEditor");
    TextBox nameField = editor.getTextBox("projectNameField");
    assertThat(nameField.isVisible());
    editor.getButton("Validate").click();
    checkTipVisible(getPanel(), nameField, expectedMessage);
    assertThat(editor.getButton("Validate").isVisible());
    return this;
  }

  public ProjectEditionChecker checkNoErrorTipDisplayed() {
    super.checkNoTipVisible(getPanel());
    return this;
  }

  public void delete() throws Exception {
    getDelete().run();
    System.out.println("ProjectEditionChecker.delete: TODO: check unselect");
  }

  public void deleteWithConfirmation(String title, String message) {
    ConfirmationDialogChecker.open(getDelete())
      .checkTitle(title)
      .checkMessageContains(message)
      .validate("Delete project");
    System.out.println("ProjectEditionChecker.deleteWithConfirmation: TODO: check unselect");
  }

  public void openDeleteAndNavigate() {
    ConfirmationDialogChecker.open(getDelete())
      .clickOnHyperlink("see these operations")
      .checkHidden();
    System.out.println("ProjectEditionChecker.deleteWithConfirmation: TODO: check unselect");
  }

  private Trigger getDelete() {
    final org.uispec4j.Button itemButton = getPanel().getButton("nameButton");
    PopupButton button = new PopupButton(itemButton);
    return button.triggerClick("Delete");
  }

  private Panel getPanel() {
    if (panel == null) {
      panel = mainWindow.getPanel("projectEditionView");
    }
    return panel;
  }

  public ProjectItemEditionChecker edit(int index) {
    return new ProjectItemEditionChecker(getItemPanel(index));
  }

  public ProjectItemEditionChecker toggleAndEdit(int index) {
    view(index).modify();
    return edit(index);
  }

  public ProjectItemViewChecker view(int index) {
    return new ProjectItemViewChecker(getItemPanel(index));
  }

  private Panel getItemPanel(int index) {
    Component[] panels = getPanel().getSwingComponents(JPanel.class, "projectItemPanel");
    return new Panel((JPanel)panels[index]);
  }
}

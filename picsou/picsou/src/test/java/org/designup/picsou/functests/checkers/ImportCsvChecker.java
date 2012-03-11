package org.designup.picsou.functests.checkers;

import org.uispec4j.ComboBox;
import org.uispec4j.TextBox;
import org.uispec4j.Window;

import javax.swing.*;
import java.awt.*;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class ImportCsvChecker extends GuiChecker {
  private ImportDialogChecker parent;
  private Window window;

  public ImportCsvChecker(ImportDialogChecker parent, Window window) {
    this.parent = parent;
    this.window = window;
  }

  public ImportCsvChecker checkContains(String... items) {
    for (String item : items) {
      assertThat(window.containsLabel(item));
    }
    return this;
  }

  private int getIndex(Container panel, Component component) {
    for (int i = 0; i < panel.getComponentCount(); i++) {
      if (component == panel.getComponent(i)) {
        return i;
      }
    }
    return -1;
  }

  public ImportCsvChecker setAsUserDate(String name) {
    return select(name, "User date");
  }

  public ImportCsvChecker checkIsUserDate(String name) {
    ComboBox box = getTypeSelectionCombo(name);
    assertThat(box.selectionEquals("User date"));
    return this;
  }

  public ImportCsvChecker setAsBankDate(String name) {
    ComboBox box = getTypeSelectionCombo(name);
    assertThat(box.contentEquals("Do not import", "User date", "Bank date"));
    return select(name, "Bank date");
  }

  public ImportCsvChecker checkIsBankDate(String name) {
    ComboBox box = getTypeSelectionCombo(name);
    assertThat(box.selectionEquals("Bank date"));
    return this;
  }

  public ImportCsvChecker setAsLabel(String name) {
    ComboBox box = getTypeSelectionCombo(name);
    assertThat(box.contentEquals("Do not import", "Label", "Note", "Envelope name", "Sub-envelope name"));
    return select(name, "Label");
  }

  public ImportCsvChecker checkIsLabel(String name) {
    ComboBox box = getTypeSelectionCombo(name);
    assertThat(box.selectionEquals("Label"));
    return this;
  }

  public ImportCsvChecker setAsNote(String name) {
   return select(name, "note");
  }

  public ImportCsvChecker checkIsNote(String name) {
    ComboBox box = getTypeSelectionCombo(name);
    assertThat(box.contentEquals("Do not import", "Label", "Note", "Envelope name", "Sub-envelope name"));
    assertThat(box.selectionEquals("Note"));
    return this;
  }

  public ImportCsvChecker setAsIgnore(String name) {
    return select(name, "Do not import");
  }

  public ImportCsvChecker setAsEnvelope(String name) {
    ComboBox box = getTypeSelectionCombo(name);
    assertThat(box.contentEquals("Do not import", "Label", "Note", "Envelope name", "Sub-envelope name"));
    return select(name, "Envelope name");
  }

  public ImportCsvChecker checkIsEnvelope(String name) {
    ComboBox box = getTypeSelectionCombo(name);
    assertThat(box.selectionEquals("Envelope name"));
    return this;
  }

  public ImportCsvChecker setAsSubEnvelope(String name) {
    ComboBox box = getTypeSelectionCombo(name);
    assertThat(box.contentEquals("Do not import", "Label", "Note", "Envelope name", "Sub-envelope name"));
    return select(name, "Sub-envelope name");
  }

  public ImportCsvChecker setAsAmount(String name) {
    ComboBox box = getTypeSelectionCombo(name);
    assertThat(box.contentEquals("Do not import", "Amount", "Debit", "Credit"));
    return select(name, "Amount");
  }

  public ImportCsvChecker checkIsAmount(String name) {
    ComboBox box = getTypeSelectionCombo(name);
    assertThat(box.selectionEquals("Amount"));
    return this;
  }

  public ImportCsvChecker setAsDebit(String name) {
    ComboBox box = getTypeSelectionCombo(name);
    assertThat(box.contentEquals("Do not import", "Amount", "Debit", "Credit"));
    return select(name, "Debit");
  }

  public ImportCsvChecker checkIsDebit(String name) {
    ComboBox box = getTypeSelectionCombo(name);
    assertThat(box.selectionEquals("Debit"));
    return this;
  }

  public ImportCsvChecker setAsCredit(String name) {
    ComboBox box = getTypeSelectionCombo(name);
    assertThat(box.contentEquals("Do not import", "Amount", "Debit", "Credit"));
    return select(name, "Credit");
  }

  public ImportCsvChecker checkIsCredit(String name) {
    ComboBox box = getTypeSelectionCombo(name);
    assertThat(box.selectionEquals("Credit"));
    return this;
  }

  private ImportCsvChecker select(String name, String value) {
    ComboBox box = getTypeSelectionCombo(name);
    box.select(value);
    return this;
  }

  private ComboBox getTypeSelectionCombo(String name) {
    TextBox component = window.getTextBox(name);
    Container associationPanel = component.getContainer("association").getAwtComponent();
    Container container = associationPanel.getParent();
    int index = getIndex(container, associationPanel);
    Component component1 = container.getComponent(index + 1);
    return new ComboBox((JComboBox)component1);
  }

  public ImportDialogChecker validate() {
    window.getButton("ok").click();
    assertFalse(window.isVisible());
    return parent;
  }

  public void cancel() {
    window.getButton("cancel").click();
    assertFalse(window.isVisible());
  }
}

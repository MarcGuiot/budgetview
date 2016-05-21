package com.budgetview.functests.checkers;

import org.uispec4j.ComboBox;
import org.uispec4j.TextBox;
import org.uispec4j.Window;

import javax.swing.*;
import java.awt.*;

import static org.uispec4j.assertion.UISpecAssert.*;

public class CsvImporterChecker extends GuiChecker {
  private ImportDialogChecker parent;
  private Window window;

  public CsvImporterChecker(ImportDialogChecker parent, Window window) {
    this.parent = parent;
    this.window = window;
  }

  public CsvImporterChecker checkContains(String... items) {
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
  
  public CsvImporterChecker checkFieldsError(String message) {
    TextBox messageField = window.getTextBox("message");
    assertThat(messageField.textEquals(message));
    assertFalse(window.getButton("ok").isEnabled());
    return this;
  }

  public CsvImporterChecker checkFieldsComplete(String message) {
    TextBox messageField = window.getTextBox("message");
    assertThat(messageField.textEquals(message));
    assertThat(window.getButton("ok").isEnabled());
    return this;
  }

  public CsvImporterChecker setAsUserDate(String name) {
    return select(name, "User date");
  }

  public CsvImporterChecker checkIsUserDate(String name) {
    ComboBox box = getTypeSelectionCombo(name);
    assertThat(box.selectionEquals("User date"));
    return this;
  }

  public CsvImporterChecker setAsBankDate(String name) {
    ComboBox box = getTypeSelectionCombo(name);
    assertThat(box.contentEquals("Do not import", "User date", "Bank date"));
    return select(name, "Bank date");
  }

  public CsvImporterChecker checkIsBankDate(String name) {
    ComboBox box = getTypeSelectionCombo(name);
    assertThat(box.selectionEquals("Bank date"));
    return this;
  }

  public CsvImporterChecker setAsLabel(String name) {
    ComboBox box = getTypeSelectionCombo(name);
    assertThat(box.contentEquals("Do not import", "Label", "Note", "Envelope name", "Sub-envelope name"));
    return select(name, "Label");
  }

  public CsvImporterChecker checkIsLabel(String name) {
    ComboBox box = getTypeSelectionCombo(name);
    assertThat(box.selectionEquals("Label"));
    return this;
  }

  public CsvImporterChecker setAsNote(String name) {
   return select(name, "note");
  }

  public CsvImporterChecker checkIsNote(String name) {
    ComboBox box = getTypeSelectionCombo(name);
    assertThat(box.contentEquals("Do not import", "Label", "Note", "Envelope name", "Sub-envelope name"));
    assertThat(box.selectionEquals("Note"));
    return this;
  }

  public CsvImporterChecker setAsIgnore(String name) {
    return select(name, "Do not import");
  }

  public CsvImporterChecker setAsEnvelope(String name) {
    ComboBox box = getTypeSelectionCombo(name);
    assertThat(box.contentEquals("Do not import", "Label", "Note", "Envelope name", "Sub-envelope name"));
    return select(name, "Envelope name");
  }

  public CsvImporterChecker checkIsEnvelope(String name) {
    ComboBox box = getTypeSelectionCombo(name);
    assertThat(box.selectionEquals("Envelope name"));
    return this;
  }

  public CsvImporterChecker setAsSubEnvelope(String name) {
    ComboBox box = getTypeSelectionCombo(name);
    assertThat(box.contentEquals("Do not import", "Label", "Note", "Envelope name", "Sub-envelope name"));
    return select(name, "Sub-envelope name");
  }

  public CsvImporterChecker setAsAmount(String name) {
    ComboBox box = getTypeSelectionCombo(name);
    assertThat(box.contentEquals("Do not import", "Amount", "Debit", "Credit"));
    return select(name, "Amount");
  }

  public CsvImporterChecker checkIsAmount(String name) {
    ComboBox box = getTypeSelectionCombo(name);
    assertThat(box.selectionEquals("Amount"));
    return this;
  }

  public CsvImporterChecker setAsDebit(String name) {
    ComboBox box = getTypeSelectionCombo(name);
    assertThat(box.contentEquals("Do not import", "Amount", "Debit", "Credit"));
    return select(name, "Debit");
  }

  public CsvImporterChecker checkIsDebit(String name) {
    ComboBox box = getTypeSelectionCombo(name);
    assertThat(box.selectionEquals("Debit"));
    return this;
  }

  public CsvImporterChecker setAsCredit(String name) {
    ComboBox box = getTypeSelectionCombo(name);
    assertThat(box.contentEquals("Do not import", "Amount", "Debit", "Credit"));
    return select(name, "Credit");
  }

  public CsvImporterChecker checkIsCredit(String name) {
    ComboBox box = getTypeSelectionCombo(name);
    assertThat(box.selectionEquals("Credit"));
    return this;
  }

  private CsvImporterChecker select(String name, String value) {
    ComboBox box = getTypeSelectionCombo(name);
    box.select(value);
    return this;
  }

  public CsvImporterChecker checkAvailableTypes(String name, String... values) {
    assertThat(getTypeSelectionCombo(name).contentEquals(values));
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

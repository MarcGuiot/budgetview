package org.designup.picsou.functests.checkers;

import org.uispec4j.CheckBox;
import org.uispec4j.ComboBox;
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

  public ImportCsvChecker checkContains(String... lines) {
    for (String line : lines) {
      assertThat(window.containsLabel(line));
    }
    return this;
  }

  private int getIndex(JPanel panel, Component component) {
    for (int i = 0; i < panel.getComponentCount(); i++) {
      if (component == panel.getComponent(i)) {
        return i;
      }
    }
    return -1;
  }


  public ImportCsvChecker setAsOperationUserDate(String name) {
    return select(name, "User date");
  }

  private ImportCsvChecker select(String name, String value) {
    JComponent component = window.getTextBox(name).getAwtComponent();
    JPanel panel = (JPanel)component.getParent();
    int index = getIndex(panel, component);
    Component component1 = panel.getComponent(index + 2);
    ComboBox box = new ComboBox((JComboBox)component1);
    box.select(value);
    return this;
  }

  public ImportCsvChecker setAsOperationBankDate(String name) {
    return select(name, "Bank date");
  }

  public ImportCsvChecker setAsLabel(String name) {
    return select(name, "label");
  }

  public ImportCsvChecker setAsIgnore(String name) {
    return select(name, "Do not import");
  }

  public ImportCsvChecker setAsEnvelop(String name) {
    return select(name, "envoppe name");
  }

  public ImportCsvChecker setAsSubEnvelop(String name) {
    return select(name, "sub envoppe name");
  }

  public ImportCsvChecker setAsAmount(String name) {
    return select(name, "amount");
  }

  public ImportCsvChecker setAsDebit(String name) {
    return select(name, "debit");
  }

  public ImportCsvChecker setAsCredit(String name) {
    return select(name, "credit");
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

  public ImportCsvChecker checkSeparator(char sep) {
    if (sep == '\t'){
      assertThat(window.getRadioButton("tab").isSelected());
    }
    if (sep == ':'){
      assertThat(window.getRadioButton(" : ").isSelected());
    }
    if (sep == ';'){
      assertThat(window.getRadioButton(" ; ").isSelected());
    }
    if (sep == ','){
      assertThat(window.getRadioButton(" , ").isSelected());
    }
    return this;
  }

  public ImportCsvChecker setSeparator(char sep) {
    if (sep == '\t'){
      window.getRadioButton("tab").click();
    }
    if (sep == ':'){
      window.getRadioButton(" : ").click();
    }
    if (sep == ';'){
      window.getRadioButton(" ; ").click();
    }
    if (sep == ','){
      window.getRadioButton(" , ").click();
    }
    return this;
  }

  public ImportCsvChecker checkLine(String line) {
    window.getTextBox(line);
    return this;
  }
}

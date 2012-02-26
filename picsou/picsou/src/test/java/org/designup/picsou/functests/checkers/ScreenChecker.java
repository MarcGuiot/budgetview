package org.designup.picsou.functests.checkers;

import org.designup.picsou.gui.plaf.WavePanelUI;
import org.globsframework.gui.splits.color.Colors;
import org.globsframework.gui.splits.components.StyledPanelUI;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.Window;
import org.uispec4j.utils.ColorUtils;

import javax.swing.*;

import static org.uispec4j.assertion.UISpecAssert.assertTrue;

public class ScreenChecker {
  private Panel mainWindow;
  private TextBox sectionText;
  private TextBox periodText;

  public ScreenChecker(Window mainWindow) {
    this.mainWindow = mainWindow;
  }

  public void checkContent(String section) {
    assertTrue(getSectionText().textEquals(section));
    assertTrue(getPeriodText().textIsEmpty());
  }

  public void checkContent(String section, String period) {
    assertTrue(getSectionText().textEquals(section));
    assertTrue(getPeriodText().textEquals(period));
  }

  public TextBox getSectionText() {
    if (sectionText == null) {
      sectionText = mainWindow.getTextBox("sectionTitle");
    }
    return sectionText;
  }

  public TextBox getPeriodText() {
    if (periodText == null) {
      periodText = mainWindow.getTextBox("periodTitle");
    }
    return periodText;
  }

  public void checkBackgroundColorIsStandard() {
    checkBackgroundTopColor("6ea2c4");
  }

  public void checkBackgroundColorIsClassic() {
    checkBackgroundTopColor("1565CB");
  }

  private void checkBackgroundTopColor(String color) {
    JPanel panel = 
      (JPanel)mainWindow.getPanel("backgroundPanel").getAwtComponent();
    WavePanelUI ui = (WavePanelUI)panel.getUI();
    ColorUtils.assertEquals(Colors.toColor(color), ui.getTopColor());
  }
}

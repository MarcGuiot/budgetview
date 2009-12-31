package org.globsframework.gui.utils;

import junit.framework.TestCase;
import org.uispec4j.Button;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

import javax.swing.*;

public class ShowHideButtonTest extends TestCase {

  private Icon showIcon = new ImageIcon();
  private Icon hideIcon = new ImageIcon();

  public void testShowHide() throws Exception {
    JPanel panel = new JPanel();
    ShowHideButton showHideButton = new ShowHideButton(panel, "show", "hide");
    showHideButton.setIcons(showIcon, hideIcon);
    Button button = new Button(showHideButton);

    assertTrue(panel.isVisible());
    assertThat(button.textEquals("hide"));
    assertThat(button.iconEquals(hideIcon));

    button.click();
    assertFalse(panel.isVisible());
    assertThat(button.textEquals("show"));
    assertThat(button.iconEquals(showIcon));

    button.click();
    assertTrue(panel.isVisible());
    assertThat(button.textEquals("hide"));
    assertThat(button.iconEquals(hideIcon));
  }

  public void testForceVisible() throws Exception {
    JPanel panel = new JPanel();
    panel.setVisible(false);

    ShowHideButton showHideButton = new ShowHideButton(panel, "show", "hide");
    Button button = new Button(showHideButton);
    assertThat(button.textEquals("show"));
    assertThat(button.isEnabled());

    showHideButton.forceVisible();
    assertFalse(panel.isVisible());
    UISpecAssert.assertFalse(button.isEnabled());
  }
}

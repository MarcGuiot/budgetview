package org.globsframework.gui.splits.components;

import org.globsframework.gui.splits.layout.Anchor;
import org.globsframework.gui.splits.layout.Fill;
import org.globsframework.gui.splits.layout.GridBagBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.utils.Strings;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

public class HyperlinkButtonUI extends BasicButtonUI {
  private int textWidth;
  private int fontHeight;
  private int descent;
  private boolean underline = true;
  private Color rolloverColor = Color.BLUE.brighter();
  private Color disabledColor = Color.GRAY;
  private PropertyChangeListener autoHideListener;
  private PropertyChangeListener fontMetricsUpdater;

  public HyperlinkButtonUI() {
  }

  public void installUI(JComponent c) {
    super.installUI(c);
    AbstractButton button = (AbstractButton)c;
    button.setRolloverEnabled(true);
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    button.setForeground(Color.BLUE);
    button.setOpaque(false);
    initFontMetrics(button);
    updateVisibility(button);
  }

  protected void installListeners(final AbstractButton button) {
    super.installListeners(button);
    autoHideListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        updateVisibility(button);
      }
    };
    button.addPropertyChangeListener("enabled", autoHideListener);

    fontMetricsUpdater = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        initFontMetrics(button);
      }
    };
    button.addPropertyChangeListener("font", fontMetricsUpdater);
    button.addPropertyChangeListener("text", fontMetricsUpdater);
  }

  private void updateVisibility(AbstractButton button) {
    button.setVisible(button.isEnabled());
  }

  protected void uninstallListeners(AbstractButton button) {
    super.uninstallListeners(button);
    button.removePropertyChangeListener(autoHideListener);
    button.removePropertyChangeListener(fontMetricsUpdater);
  }

  public void setUnderline(boolean underline) {
    this.underline = underline;
  }

  public void setRolloverColor(Color rolloverColor) {
    this.rolloverColor = rolloverColor;
  }

  public void setDisabledColor(Color disabledColor) {
    this.disabledColor = disabledColor;
  }

  public void paint(Graphics g, JComponent c) {

    AbstractButton button = (AbstractButton)c;

    Graphics2D d = (Graphics2D)g;
    d.setFont(button.getFont());
    d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    if (button.isOpaque()) {
      d.setColor(button.getParent().getBackground());
      d.clearRect(0, 0, button.getWidth(), button.getHeight());
    }

    String text = button.getText();
    if (Strings.isNullOrEmpty(text)) {
      return;
    }

    int x1 = (button.getWidth() - textWidth) / 2;
    int y1 = (button.getHeight() + fontHeight) / 2 - descent;
    if (button.getModel().isRollover() || button.getModel().isArmed()) {
      d.setColor(rolloverColor);
    }
    else if (!button.isEnabled()) {
      d.setColor(disabledColor);
    }
    else {
      d.setColor(button.getForeground());
    }

    d.drawString(text, x1, y1);
    if (underline || button.getModel().isRollover()) {
      d.drawLine(x1, y1 + 1, x1 + textWidth, y1 + 1);
    }
  }

  private void initFontMetrics(AbstractButton button) {
    FontMetrics fontMetrics = button.getFontMetrics(button.getFont());
    textWidth = fontMetrics.stringWidth(button.getText() == null ? "" : button.getText());
    fontHeight = fontMetrics.getHeight();
    descent = fontMetrics.getDescent();
    button.setSize(textWidth, fontHeight);
    Dimension dimension = new Dimension(textWidth, fontHeight);
    button.setPreferredSize(dimension);
    button.setMaximumSize(dimension);
    button.setMinimumSize(dimension);
  }

  public static void main(String[] args) {
    final AbstractAction action = new AbstractAction("toto") {
      public void actionPerformed(ActionEvent e) {
        System.out.println("HyperlinkButtonUI.actionPerformed: ");
      }
    };
    final JButton button = new JButton(action);
    button.setText("hello");
    button.setUI(new HyperlinkButtonUI());

    JPanel panel =
      GridBagBuilder.init()
      .add(button, 0, 0, 1, 1, 1, 1, Fill.NONE, Anchor.CENTER)
        .add(new JButton(new AbstractAction("disable") {
          public void actionPerformed(ActionEvent e) {
            final boolean newState = !button.isEnabled();
            action.setEnabled(newState);
            putValue(Action.NAME, newState ? "disable" : "enable");
          }
        }),0, 1, 1, 1, 1, 1, Fill.NONE, Anchor.CENTER)
        .getPanel();
    panel.setOpaque(true);
    panel.setBackground(Color.CYAN);
    GuiUtils.show(panel);
  }
}
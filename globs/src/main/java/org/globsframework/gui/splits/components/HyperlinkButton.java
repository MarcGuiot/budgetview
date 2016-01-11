package org.globsframework.gui.splits.components;

import org.globsframework.gui.splits.layout.Anchor;
import org.globsframework.gui.splits.layout.Fill;
import org.globsframework.gui.splits.layout.GridBagBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.utils.Strings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class HyperlinkButton extends JButton {
  private int textWidth;
  private int fontHeight;
  private int descent;
  private boolean underline = true;
  private Color rolloverColor = Color.BLUE.brighter();
  private Color disabledColor = Color.GRAY;
  private boolean autoHide = true;

  public HyperlinkButton() {
    init();
  }

  public HyperlinkButton(Action action) {
    super(action);
    init();
  }

  private void init() {
    setRolloverEnabled(true);
    setEnabledCursor(true);
    setForeground(Color.BLUE);
  }

  public void setAutoHide(boolean autoHide) {
    this.autoHide = autoHide;
  }

  public void setEnabledCursor(boolean enabled) {
      setCursor(Cursor.getPredefinedCursor(enabled ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
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

  public void setEnabled(boolean b) {
    super.setEnabled(b);
    setVisible(b || !autoHide);
  }

  public void paint(Graphics g) {
    Graphics2D d = (Graphics2D)g;
    d.setFont(getFont());
    d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    if (isOpaque()) {
      d.setColor(getParent().getBackground());
      d.clearRect(0, 0, getWidth(), getHeight());
    }

    String text = getText();
    if (Strings.isNullOrEmpty(text)) {
      return;
    }

    int x1 = (getWidth() - textWidth) / 2;
    int y1 = (getHeight() + fontHeight) / 2 - descent;
    if (getModel().isRollover() || getModel().isArmed()) {
      d.setColor(rolloverColor);
    }
    else if (!getModel().isEnabled()) {
      d.setColor(disabledColor);
    }
    else {
      d.setColor(getForeground());
    }

    d.drawString(text, x1, y1);
    if (underline || getModel().isRollover()) {
      d.drawLine(x1, y1 + 1, x1 + textWidth, y1 + 1);
    }
  }

  public void setFont(Font font) {
    super.setFont(font);
    initFontMetrics();
  }

  public void setText(String text) {
    super.setText(text);
    initFontMetrics();
  }

  private void initFontMetrics() {
    FontMetrics fontMetrics = getFontMetrics(getFont());
    textWidth = fontMetrics.stringWidth(getText() == null ? "" : getText());
    fontHeight = fontMetrics.getHeight();
    descent = fontMetrics.getDescent();
    setSize(textWidth, fontHeight);
    Dimension dimension = new Dimension(textWidth, fontHeight);
    setPreferredSize(dimension);
    setMaximumSize(dimension);
    setMinimumSize(dimension);
  }

  public static void main(String[] args) {
    HyperlinkButton button = new HyperlinkButton(new AbstractAction("toto") {
      public void actionPerformed(ActionEvent e) {
        System.out.println("HyperlinkButton.actionPerformed");
      }
    });
    button.setText("hello");

    JPanel panel = GridBagBuilder.init().add(button, 0, 0, 1, 1, 1, 1, Fill.NONE, Anchor.CENTER).getPanel();
    panel.setOpaque(true);
    panel.setBackground(Color.CYAN);
    GuiUtils.showCentered(panel);
  }
}

package org.globsframework.gui.splits.components;

import org.globsframework.gui.splits.layout.Anchor;
import org.globsframework.gui.splits.layout.Fill;
import org.globsframework.gui.splits.layout.GridBagBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class HyperlinkButton extends JButton {
  private int textWidth;
  private int fontHeight;
  private Color rolloverColor = Color.BLUE;
  private Color disabledColor = Color.GRAY;
  private int descent;

  public HyperlinkButton() {
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    setForeground(Color.CYAN);
  }

  public HyperlinkButton(Action action) {
    super(action);
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    setForeground(Color.CYAN);
  }

  public void setRolloverColor(Color rolloverColor) {
    this.rolloverColor = rolloverColor;
  }

  public void setDisabledColor(Color disabledColor) {
    this.disabledColor = disabledColor;
  }

  public void setEnabled(boolean b) {
    super.setEnabled(b);
    setVisible(b);
  }

  public void paint(Graphics g) {
    Graphics2D d = (Graphics2D)g;
    d.setFont(getFont());
    if (isOpaque()) {
      d.setColor(getParent().getBackground());
      d.clearRect(0, 0, getWidth(), getHeight());
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
    String str = getText();
    if (str != null) {
      d.drawString(str, x1, y1);
      d.drawLine(x1, y1 + 1, x1 + textWidth, y1 + 1);
    }
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
    HyperlinkButton component1 = new HyperlinkButton(new AbstractAction("toto") {

      public void actionPerformed(ActionEvent e) {
        System.out.println("HyperlinkButton.actionPerformed");
      }
    });
    component1.setText("hello");
    JPanel panel = GridBagBuilder.init().add(component1, 0, 0, 1, 1, 1, 1, Fill.NONE, Anchor.CENTER).getPanel();
    panel.setOpaque(true);
    panel.setBackground(Color.CYAN);
    GuiUtils.show(panel);
  }
}

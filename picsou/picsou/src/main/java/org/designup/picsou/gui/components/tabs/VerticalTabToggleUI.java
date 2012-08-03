package org.designup.picsou.gui.components.tabs;

import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import javax.swing.plaf.basic.BasicToggleButtonUI;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class VerticalTabToggleUI extends BasicToggleButtonUI {

  private Color rolloverTextColor = Color.RED;
  private Color disabledTextColor = Color.GRAY;

  private Color bgColor;
  private Color borderColor;
  private Font boldFont;

  protected void installDefaults(final AbstractButton button) {
    super.installDefaults(button);
    button.setRolloverEnabled(true);
    button.setOpaque(false);
    button.setBorderPainted(false);
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    final Font defaultFont = button.getFont();
    boldFont = defaultFont.deriveFont(defaultFont.getStyle() ^ Font.BOLD);

    button.getModel().addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        button.setFont(button.isSelected() ? boldFont : defaultFont);
      }
    });
  }

  protected void paintButtonPressed(Graphics g, AbstractButton button) {
    button.setOpaque(false);

    Graphics2D g2 = (Graphics2D)g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    Dimension size = button.getSize();
    int width = size.width;
    int height = size.height - 1;

    g2.setColor(bgColor);
    g2.fillRect(0, 0, width, height);

    g2.setColor(borderColor);
    g2.drawLine(0, 0, width, 0);
    g2.drawLine(0, 0, 0, height);
    g2.drawLine(0, height, width, height);
  }

  protected void paintText(Graphics g, JComponent component, Rectangle textRect, String text) {
    AbstractButton button = (AbstractButton)component;
    ButtonModel model = button.getModel();
    FontMetrics fm = g.getFontMetrics(boldFont);
    int mnemonicIndex = button.getDisplayedMnemonicIndex();
    if (!button.isEnabled()) {
      g.setColor(disabledTextColor);
    }
    else if (model.isRollover()) {
      g.setColor(rolloverTextColor);
    }
    else {
      g.setColor(component.getForeground());
    }

    g.setFont(boldFont);
    g.drawString(text,
                 textRect.x + getTextShiftOffset(),
                 textRect.y + (fm.getMaxAscent() - fm.getMaxDescent()) + getTextShiftOffset() + 2);

    if (mnemonicIndex > 0) {
      GuiUtils.drawUnderlineCharAt(g, text, mnemonicIndex,
                                   textRect.x + getTextShiftOffset(),
                                   textRect.y + (fm.getMaxAscent() - fm.getMaxDescent()) + getTextShiftOffset());
    }
  }

  public void setBgColor(Color bgColor) {
    this.bgColor = bgColor;
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }

  public void setRolloverTextColor(Color rolloverTextColor) {
    this.rolloverTextColor = rolloverTextColor;
  }

  public void setDisabledTextColor(Color disabledTextColor) {
    this.disabledTextColor = disabledTextColor;
  }
}


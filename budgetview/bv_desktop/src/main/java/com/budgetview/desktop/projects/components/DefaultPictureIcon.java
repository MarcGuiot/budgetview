package com.budgetview.desktop.projects.components;

import com.budgetview.utils.Lang;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;

public class DefaultPictureIcon implements Icon, Disposable, ColorChangeListener {

  private final int width;
  private final int height;
  private final ColorService colorService;

  private Color backgroundColor = Color.LIGHT_GRAY;
  private Color borderColor = Color.DARK_GRAY;
  private Color textColor;

  private Font font;
  private final FontMetrics fontMetrics;
  private final String text;

  public DefaultPictureIcon(Dimension dimension, Directory directory) {
    this.width = dimension.width;
    this.height = dimension.height;
    this.colorService = directory.get(ColorService.class);
    this.colorService.addListener(this);
    JLabel label = new JLabel();
    font = label.getFont().deriveFont(11.0f);
    fontMetrics = label.getFontMetrics(font);
    text = Lang.get("projectView.defaultIcon.text");
  }

  public int getIconWidth() {
    return width;
  }

  public int getIconHeight() {
    return height;
  }

  public void colorsChanged(ColorLocator colorLocator) {
    backgroundColor = colorLocator.get("projectView.defaultPicture.bg");
    borderColor = colorLocator.get("projectView.defaultPicture.border");
    textColor = colorLocator.get("projectView.defaultPicture.text");
  }

  public void paintIcon(Component c, Graphics g, int x, int y) {
    int drawingWidth = width - 1;
    int drawingHeight = height - 1;

    g.setColor(backgroundColor);
    g.fillRect(x, y, drawingWidth, drawingHeight);
    g.setColor(borderColor);
    g.drawRect(x, y, drawingWidth, drawingHeight);

    g.setFont(font);
    g.setColor(textColor);
    g.drawString(text,
                 x + drawingWidth / 2 - fontMetrics.stringWidth(text) / 2,
                 y + drawingHeight / 2 - fontMetrics.getAscent() / 2);
  }

  public void dispose() {
    colorService.removeListener(this);
  }
}

package org.designup.picsou.gui.components.images;

import org.designup.picsou.gui.utils.Gui;
import org.globsframework.gui.splits.components.EmptyIcon;
import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;

public class FileStorageService {
  public Icon getIcon(String path, Dimension maxSize) {

    ImageIcon imageIcon = new ImageIcon(path);
    if ((maxSize.width == 0) || (maxSize.height == 0)) {
      return new EmptyIcon(10,10);
    }
    float xScale = (float)maxSize.width / (float)imageIcon.getIconWidth();
    float yScale = (float)maxSize.height / (float)imageIcon.getIconHeight();
    float minScale = Math.min(xScale, yScale);
    if (minScale < 1.0) {
      imageIcon.setImage(imageIcon.getImage().getScaledInstance((int)(imageIcon.getIconWidth() * minScale),
                                                                (int)(imageIcon.getIconHeight() * minScale),
                                                                Image.SCALE_FAST));
    }

    return imageIcon;
  }
}

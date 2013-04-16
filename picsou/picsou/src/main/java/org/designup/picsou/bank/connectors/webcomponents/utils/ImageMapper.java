package org.designup.picsou.bank.connectors.webcomponents.utils;

import org.designup.picsou.bank.connectors.webcomponents.WebImage;
import org.designup.picsou.bank.connectors.webcomponents.WebImageMap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class ImageMapper {

  private JLabel mappedLabel;
  private MouseAdapter lastMouseAdapter;

  public interface Listener {
    void imageClicked();
  }

  private List<Listener> listeners = new ArrayList<Listener>();

 public ImageMapper install(final WebImageMap imageMap, JLabel label) throws WebParsingError {
    reset();
    this.mappedLabel = label;
    WebImage image = imageMap.getImage();
    Icon icon = image.asIcon();
    if (icon == null) {
      throw new WebParsingError(imageMap, "Could not load image for imageMap " + imageMap.getName());
    }
    label.setIcon(icon);
    label.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
    lastMouseAdapter = new MouseAdapter() {
      public void mouseClicked(MouseEvent mouseEvent) {
        try {
          imageMap.click(mouseEvent.getX(), mouseEvent.getY());
          notifyListeners();
        }
        catch (WebCommandFailed e) {
          throw new RuntimeException(e);
        }
      }
    };
    label.addMouseListener(lastMouseAdapter);
    return this;
  }

  public void addListener(Listener listener) {
    listeners.add(listener);
  }

  private void notifyListeners() {
    for (Listener listener : listeners) {
      listener.imageClicked();
    }
  }

  private void reset() {
    if (mappedLabel != null) {
      mappedLabel.removeMouseListener(lastMouseAdapter);
    }
    listeners.clear();
  }
}

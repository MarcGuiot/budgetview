package org.globsframework.gui.splits;

import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.color.ColorServiceEditor;
import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class SplitsEditor {

  public static void showInFrame(final SplitsBuilder builder, Container container) {
    ColorService colorService = builder.getContext().getColorService();
    colorService.autoUpdate(container);

    JFrame frame = new JFrame("Splits Editor");
    ColorServiceEditor colorEditor = new ColorServiceEditor(colorService);

    frame.getContentPane().add(new JButton(new AbstractAction("Reload") {
      public void actionPerformed(ActionEvent e) {
        builder.load();
      }
    }));

    frame.getContentPane().add(colorEditor.getPanel());
    GuiUtils.showCentered(frame);
  }
}

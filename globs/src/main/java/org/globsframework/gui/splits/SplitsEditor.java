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

    JFrame frame =
      SplitsBuilder.init(builder.getContext().getColorService(),
                                 builder.getContext().getIconLocator())
        .setSource(SplitsEditor.class, "/splits/splitsEditor.splits")
        .add("reload", new AbstractAction("Reload") {
          public void actionPerformed(ActionEvent e) {
            builder.load();
          }
        })
        .add("colorEditor", new ColorServiceEditor(colorService).getBuilder())
        .load();

    GuiUtils.showCentered(frame);
  }
}

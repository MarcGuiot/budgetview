package org.globsframework.gui.splits;

import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.color.editor.ColorServiceEditor;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;

public class SplitsEditor {

  public static final String EDITOR_ENABLED_PROPERTY = "splits.editor.enabled";

  public static void show(final SplitsBuilder builder, Container container) {

    if (!"true".equalsIgnoreCase(System.getProperty(EDITOR_ENABLED_PROPERTY))) {
      return;
    }

    ColorService colorService = builder.getContext().getService(ColorService.class);
    colorService.autoUpdate(container);

    JFrame splitsEditorFrame =
      SplitsBuilder.init(builder.getDirectory())
        .setSource(SplitsEditor.class, "/splits/splitsEditor.splits")
        .add("colorEditor", new ColorServiceEditor(colorService).getBuilder())
        .load();
    GuiUtils.showCentered(splitsEditorFrame);
  }

  public static void show(Window window, Directory directory) {
    ColorService colorService = directory.get(ColorService.class);
    colorService.autoUpdate(window);

    JFrame frame =
      SplitsBuilder.init(directory)
        .setSource(SplitsEditor.class, "/splits/splitsEditor.splits")
        .add("colorEditor", new ColorServiceEditor(colorService).getBuilder())
        .load();

    GuiUtils.showCentered(frame);    
  }
}

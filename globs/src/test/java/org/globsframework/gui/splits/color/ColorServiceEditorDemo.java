package org.globsframework.gui.splits.color;

import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.SplitsEditor;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.directory.DefaultDirectory;

import javax.swing.*;
import java.awt.*;

public class ColorServiceEditorDemo {
  public static void main(String[] args) {
    ColorService colorService = new ColorService();
    colorService.set("form.bg", Color.RED);
    colorService.set("label.fg", Color.BLACK);

    Directory directory = new DefaultDirectory();
    directory.add(colorService);
    SplitsBuilder builder = new SplitsBuilder(directory);

    JFrame frame =
      builder.setSource(
        "<splits>" +
        "  <frame background='form.bg'>" +
        "    <row margin='15'>" +
        "      <label text='Name' foreground='label.fg' marginRight='20'/>" +
        "      <textField/>" +
        "    </row>" +
        "  </frame>" +
        "</splits>")
        .load();
    frame.pack();
    frame.setVisible(true);

    SplitsEditor.show(builder, frame);
  }
}

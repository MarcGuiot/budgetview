package org.globsframework.gui.splits.color;

import org.globsframework.gui.splits.IconLocator;
import org.globsframework.gui.splits.SplitsBuilder;

import javax.swing.*;
import java.awt.*;
import java.io.StringReader;

public class ColorServiceEditorDemo {
  public static void main(String[] args) {
    ColorService colorService = new ColorService();
    colorService.set("form.bg", Color.RED);
    colorService.set("label.fg", Color.BLACK);

    SplitsBuilder builder = new SplitsBuilder(colorService, IconLocator.NULL);

    JFrame frame =
      (JFrame)builder.parse(new StringReader(
        "<splits>" +
        "  <frame background='form.bg'>" +
        "    <row margin='15'>" +
        "      <label text='Name' foreground='label.fg' marginRight='20'/>" +
        "      <textField/>" +
        "    </row>" +
        "  </frame>" +
        "</splits>"));
    frame.pack();
    frame.setVisible(true);

    ColorServiceEditor.showInFrame(colorService, frame);
  }
}

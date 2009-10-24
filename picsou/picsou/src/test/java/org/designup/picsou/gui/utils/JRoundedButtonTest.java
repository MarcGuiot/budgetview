package org.designup.picsou.gui.utils;

import org.designup.picsou.gui.components.JRoundedButton;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

public class JRoundedButtonTest {
  public static void main(String[] args) throws IOException {

    Directory directory = new DefaultDirectory();
    ColorService colorService = ApplicationColors.registerColorService(directory);
    directory.add(Gui.IMAGE_LOCATOR);

    final JFrame frame = new JFrame();
    JPanel panel = new JPanel();
    panel.add(JRoundedButton.createCircle(new AbstractAction("", Gui.IMAGE_LOCATOR.get("button_collapse.png")) {
      public void actionPerformed(ActionEvent actionEvent) {
      }
    }, colorService));
    frame.getContentPane().add(panel);
    frame.pack();
    frame.setSize(300, 200);

    frame.setVisible(true);
  }
}

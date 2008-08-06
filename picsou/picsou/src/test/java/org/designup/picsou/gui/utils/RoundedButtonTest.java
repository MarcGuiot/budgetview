package org.designup.picsou.gui.utils;

import org.globsframework.gui.splits.IconLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.io.IOException;

public class RoundedButtonTest {
  public static void main(String[] args) throws IOException {

    Directory directory = new DefaultDirectory();
    ColorService colorService = PicsouColors.registerColorService(directory);
    IconLocator iconLocator = Gui.ICON_LOCATOR;

    final JFrame frame = new JFrame();
//    JPanel panel = new JGradientPanel(directory.get(ColorService.class),
//                                      Color.DARK_GRAY,
//                                      Color.LIGHT_GRAY);
//    panel.add(RoundedButton.createRoundedRectangle(new AbstractAction("", iconLocator.get("last.png")) {
//      public void actionPerformed(ActionEvent actionEvent) {
//      }
//    }, colorService));
//    frame.getContentPane().add(panel);
//    frame.pack();
//    frame.setSize(300, 200);

//    ColorServiceEditor.showInFrame(directory.get(ColorService.class), frame);

    frame.setVisible(true);
  }
}

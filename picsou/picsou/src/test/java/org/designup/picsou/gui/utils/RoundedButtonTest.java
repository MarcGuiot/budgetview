package org.designup.picsou.gui.utils;

import org.crossbowlabs.globs.utils.directory.DefaultDirectory;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.splits.IconLocator;
import org.crossbowlabs.splits.color.ColorService;
import org.crossbowlabs.splits.color.ColorServiceEditor;
import org.designup.picsou.gui.components.JGradientPanel;
import org.designup.picsou.gui.components.RoundedButton;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

public class RoundedButtonTest {
  public static void main(String[] args) throws IOException {

    Directory directory = new DefaultDirectory();
    ColorService colorService = PicsouColors.registerColorService(directory);
    IconLocator iconLocator = Gui.ICON_LOCATOR;

    final JFrame frame = new JFrame();
    JPanel panel = new JGradientPanel(directory.get(ColorService.class),
                                      PicsouColors.FRAME_BG_TOP,
                                      PicsouColors.FRAME_BG_BOTTOM);
    panel.add(RoundedButton.createRoundedRectangle(new AbstractAction("", iconLocator.get("last.png")) {
      public void actionPerformed(ActionEvent actionEvent) {
      }
    }, colorService));
    frame.getContentPane().add(panel);
    frame.pack();
    frame.setSize(300, 200);

    ColorServiceEditor.showInFrame(directory.get(ColorService.class), frame);

    frame.setVisible(true);
  }
}

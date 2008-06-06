package org.designup.picsou.gui;

import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.splits.SplitsBuilder;
import org.crossbowlabs.splits.color.ColorService;
import org.designup.picsou.gui.components.JWavePanel;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.utils.PicsouColors;
import org.designup.picsou.utils.Lang;

import javax.swing.*;

public class NewAccountPanel {
  private JPanel panel;

  public static void show(GlobRepository repository, Directory directory, MainWindow mainWindow) {
    NewAccountPanel panel = new NewAccountPanel(repository, directory, mainWindow);
    mainWindow.setPanel(panel.panel);
  }

  private NewAccountPanel(final GlobRepository repository, final Directory directory, final MainWindow mainWindow) {

    ColorService colorService = PicsouColors.createColorService();
    SplitsBuilder builder = new SplitsBuilder(colorService, Gui.ICON_LOCATOR, Lang.TEXT_LOCATOR);
    builder.add("wave", new JWavePanel(colorService));
    ImportPanel importPanel = new ImportPanel(repository, directory) {
      protected void complete() {
        MainPanel.show(repository, directory, mainWindow);
      }


    };
    builder.add("content", importPanel.getPanel());
    panel = (JPanel)builder.parse(getClass(), "/layout/newAccountPanel.splits");
  }
}

package org.designup.picsou.gui.preferences.dev;

import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.model.UserPreferences;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class DevOptionsDialog {
  private PicsouDialog dialog;
  private GlobsPanelBuilder builder;

  public DevOptionsDialog(Window parent, final GlobRepository repository, Directory directory) {
    builder = new GlobsPanelBuilder(DevOptionsDialog.class,
                                    "/layout/general/devOptionsDialog.splits",
                                    repository, directory);

    builder.addComboEditor("period", UserPreferences.KEY,
                           UserPreferences.PERIOD_COUNT_FOR_PLANNED,
                           new int[]{4, 5, 6, 7, 10});

    builder.addComboEditor("monthBack", UserPreferences.KEY,
                           UserPreferences.MONTH_FOR_PLANNED,
                           new int[]{1, 2, 3});

    dialog = PicsouDialog.create(parent, directory);
    dialog.addPanelWithButton((JPanel)builder.load(), new OkAction());
  }

  public void show() {
    dialog.pack();
    dialog.showCentered();
    builder.dispose();
  }

  private class OkAction extends AbstractAction {
    public OkAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      dialog.setVisible(false);
    }
  }
}

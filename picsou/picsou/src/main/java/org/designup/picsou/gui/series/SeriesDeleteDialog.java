package org.designup.picsou.gui.series;

import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class SeriesDeleteDialog {
  private PicsouDialog dialog;
  private boolean ok;

  public SeriesDeleteDialog(GlobRepository repository, Directory directory, Window parent) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(SeriesEditionDialog.class,
                                                      "/layout/seriesDeletionDialog.splits",
                                                      repository, directory);

    dialog = PicsouDialog.createWithButtons(parent, directory, builder.<JPanel>load(),
                                            new AbstractAction(Lang.get("ok")) {
                                              public void actionPerformed(ActionEvent e) {
                                                ok = true;
                                                dialog.setVisible(false);
                                              }
                                            },
                                            new AbstractAction(Lang.get("cancel")) {
                                              public void actionPerformed(ActionEvent e) {
                                                ok = false;
                                                dialog.setVisible(false);
                                              }
                                            });
  }

  public boolean show() {
    ok = false;
    dialog.pack();
    GuiUtils.showCentered(dialog);
    return ok;
  }
}

package org.designup.picsou.gui.series;

import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
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
                                                      "/layout/seriesDeleteDialog.splits",
                                                      repository, directory);
    dialog = PicsouDialog.createWithButtons(parent, builder.<JPanel>load(),
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
                                            }, directory);
  }

  public boolean show() {
    ok = false;
    dialog.setVisible(true);
    return ok;
  }
}

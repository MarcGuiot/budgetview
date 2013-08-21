package org.designup.picsou.gui.preferences.panes;

import org.designup.picsou.gui.components.dialogs.CancelAction;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.preferences.PreferencesDialog;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class OverwriteConfirmationDialog {

  private final PicsouDialog dialog;
  private Boolean overwrite = false;
  private JRadioButton overwriteRadio;
  private final GlobsPanelBuilder builder;

  public OverwriteConfirmationDialog(Window owner, GlobRepository repository, Directory directory) {
    builder = new GlobsPanelBuilder(PreferencesDialog.class,
                                                      "/layout/general/preferences/overwriteConfirmationDialog.splits",
                                                      repository, directory);

    dialog = PicsouDialog.create(owner, true, directory);

    ButtonGroup group = new ButtonGroup();
    JRadioButton useRadio = new JRadioButton(Lang.get("data.path.transferMode.use"));
    builder.add("useRadio", useRadio);
    group.add(useRadio);
    overwriteRadio = new JRadioButton(Lang.get("data.path.transferMode.overwrite"));
    builder.add("overwriteRadio", overwriteRadio);
    group.add(overwriteRadio);
    overwriteRadio.doClick();

    dialog.addPanelWithButtons(builder.<JPanel>load(), new OkAction(), new CancelAction());
    dialog.pack();
  }

  public void show() {
    dialog.showCentered();
    builder.dispose();
  }

  public boolean wasCancelled() {
    return overwrite == null;
  }

  public boolean overwriteSelected() {
    return Boolean.TRUE.equals(overwrite);
  }

  private class OkAction extends AbstractAction {
    private OkAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      overwrite = overwriteRadio.isSelected();
      dialog.setVisible(false);
    }
  }

  private class CancelAction extends AbstractAction {
    private CancelAction() {
      super(Lang.get("cancel"));
    }

    public void actionPerformed(ActionEvent e) {
      overwrite = null;
      dialog.setVisible(false);
    }
  }
}

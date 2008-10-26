package org.designup.picsou.gui.license;

import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LicenceExpirationDialog {
  private PicsouDialog dialog;
  private JLabel message;

  public LicenceExpirationDialog(Window parent, GlobRepository repository, Directory directory) {

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/licenseExpirationDialog.splits",
                                                      repository, directory);
    message = new JLabel();
    builder.add("expirationMessage", message);
    dialog = PicsouDialog.createWithButton(parent, builder.<JPanel>load(), new ValidateAction(), directory);
    dialog.setTitle(Lang.get("license.expiration.title"));
    message.setText(Lang.get("license.expiration.message"));
    dialog.pack();
  }

  public void show() {
    GuiUtils.center(dialog);
    dialog.setVisible(true);
  }


  private class ValidateAction extends AbstractAction {
    private ValidateAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      dialog.setVisible(false);
    }
  }
}

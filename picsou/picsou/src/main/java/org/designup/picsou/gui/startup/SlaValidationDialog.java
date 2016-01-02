package org.designup.picsou.gui.startup;

import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SlaValidationDialog {
  private PicsouDialog dialog;
  private boolean accepted = false;
  private JLabel errorMessage;
  private JCheckBox checkBox;
  private SplitsBuilder builder;

  public static boolean termsAccepted(Window owner, Directory directory) {
    SlaValidationDialog dialog = new SlaValidationDialog(owner, directory);
    return dialog.show();
  }

  private SlaValidationDialog(Window owner, Directory directory) {

    builder = SplitsBuilder.init(directory).setSource(getClass(), "/layout/general/slaValidationDialog.splits");

    errorMessage = builder.add("errorMessage", new JLabel()).getComponent();
    errorMessage.setVisible(false);

    checkBox = builder.add("checkBox", new JCheckBox()).getComponent();
    checkBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        errorMessage.setVisible(false);
      }
    });

    builder.add("editor", Gui.createHtmlEditor(Lang.getDocFile("sla.html")));

    dialog = PicsouDialog.create(this, owner, true, directory);

    dialog.addPanelWithButtons(builder.<JPanel>load(), new OkAction(), new CancelAction());
    dialog.pack();
  }

  public boolean show() {
    dialog.showCentered();
    builder.dispose();
    return accepted;
  }

  private class OkAction extends AbstractAction {
    public OkAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      if (!checkBox.isSelected()) {
        errorMessage.setVisible(true);
        return;
      }

      accepted = true;
      dialog.setVisible(false);
    }
  }

  private class CancelAction extends AbstractAction {
    public CancelAction() {
      super(Lang.get("cancel"));
    }

    public void actionPerformed(ActionEvent e) {
      accepted = false;
      dialog.setVisible(false);
    }
  }

}

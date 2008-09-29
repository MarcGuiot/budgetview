package org.designup.picsou.gui.help;

import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class HelpDialog {
  private PicsouDialog dialog;
  private JEditorPane editor;
  private JLabel title;

  public HelpDialog(GlobRepository repository, Directory directory) {
    createDialog(repository, directory);
  }

  private void createDialog(GlobRepository repository, Directory directory) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/helpDialog.splits",
                                                      repository, directory);
    title = builder.add("title", new JLabel());
    editor = builder.add("editor", new JEditorPane());

    JPanel panel = builder.load();

    dialog = PicsouDialog.createWithButton(directory.get(JFrame.class), false, panel, new CloseAction(),
                                           directory);
    dialog.pack();
  }

  public void show(String ref) {
    title.setText(Lang.get("help." + ref));
    editor.setText(Lang.getFile(ref + ".html"));
    GuiUtils.showCentered(dialog);
  }

  private class CloseAction extends AbstractAction {
    public CloseAction() {
      super(Lang.get("close"));
    }

    public void actionPerformed(ActionEvent e) {
      dialog.setVisible(false);
    }
  }
}

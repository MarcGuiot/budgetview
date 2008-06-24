package org.designup.picsou.gui.actions;

import org.designup.picsou.gui.components.DialogOwner;
import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.startup.ImportPanel;
import org.designup.picsou.gui.startup.OpenRequestManager;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Collections;
import java.util.List;

public class ImportFileAction extends AbstractAction {

  private Directory directory;
  private GlobRepository repository;

  public ImportFileAction(final GlobRepository repository, final Directory directory) {
    super(Lang.get("import"));
    this.repository = repository;
    this.directory = directory;
    OpenRequestManager openRequestManager = directory.get(OpenRequestManager.class);
    openRequestManager.pushCallback(new OpenRequestManager.Callback() {
      public void openFiles(List<File> files) {
        SwingUtilities.invokeLater(new OpenRunnable(files, directory, repository));
      }
    });
  }

  public void actionPerformed(ActionEvent event) {
    OpenRunnable runnable = new OpenRunnable(Collections.EMPTY_LIST, directory, repository);
    runnable.run();
  }

  private static class OpenRunnable implements Runnable {
    private JFrame frame;
    private PicsouDialog dialog;
    private ImportPanel panel;

    public OpenRunnable(List<File> files, Directory directory, GlobRepository repository) {
      frame = directory.get(JFrame.class);
      panel = new ImportPanel(Lang.get("import.step1.close"), files, new DialogOwner() {
        public Window getOwner() {
          return dialog;
        }
      }, repository, directory) {
        protected void complete() {
          dialog.setVisible(false);
        }
      };
    }

    public void run() {
      dialog = PicsouDialog.create(frame, Lang.get("import"));
      dialog.setContentPane(panel.getPanel());
      dialog.pack();
      GuiUtils.showCentered(dialog);
    }
  }
}

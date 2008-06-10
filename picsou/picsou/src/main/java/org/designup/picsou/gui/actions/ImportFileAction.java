package org.designup.picsou.gui.actions;

import org.crossbowlabs.globs.model.*;
import org.crossbowlabs.globs.model.utils.LocalGlobRepository;
import org.crossbowlabs.globs.model.utils.LocalGlobRepositoryBuilder;
import static org.crossbowlabs.globs.model.FieldValue.value;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.globs.gui.SelectionService;
import org.crossbowlabs.splits.utils.GuiUtils;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.ImportPanel;
import org.designup.picsou.model.*;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ImportFileAction extends AbstractAction {

  private Directory directory;
  private GlobRepository repository;

  public ImportFileAction(GlobRepository repository, Directory directory) {
    super(Lang.get("import"));
    this.repository = repository;
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent event) {
    JFrame frame = directory.get(JFrame.class);
    final PicsouDialog dialog = PicsouDialog.create(frame, Lang.get("import"));

    ImportPanel panel = new ImportPanel(dialog, repository, directory) {
      protected void complete() {
        dialog.setVisible(false);
      }
    };
    dialog.setContentPane(panel.getPanel());
    dialog.pack();
    GuiUtils.showCentered(dialog);
  }
}

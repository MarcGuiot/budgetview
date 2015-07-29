package org.designup.picsou.gui.signpost.sections;

import org.designup.picsou.gui.actions.ImportFileAction;
import org.designup.picsou.model.Transaction;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class ImportSectionPanel extends SignpostSectionPanel {
  public ImportSectionPanel(GlobRepository repository, Directory directory) {
    super(SignpostSection.IMPORT, repository, directory);
  }

  protected boolean isCompleted(GlobRepository repository) {
    return repository.contains(Transaction.TYPE);
  }

  protected AbstractAction getAction(Directory directory) {
    return ImportFileAction.initForMenu(SignpostSection.IMPORT.getLabel(), repository, directory);
  }
}

package org.designup.picsou.gui.signpost.sections;

import org.designup.picsou.model.SignpostSectionType;
import org.designup.picsou.model.Transaction;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class ImportSectionPanel extends SignpostSectionPanel {
  public ImportSectionPanel(GlobRepository repository, Directory directory) {
    super(SignpostSection.IMPORT, repository, directory);
  }

  protected boolean isCompleted(GlobRepository repository) {
    return repository.contains(Transaction.TYPE);
  }
}

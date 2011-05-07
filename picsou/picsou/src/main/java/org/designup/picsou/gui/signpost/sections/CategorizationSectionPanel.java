package org.designup.picsou.gui.signpost.sections;

import org.designup.picsou.model.SignpostStatus;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class CategorizationSectionPanel extends SignpostSectionPanel {
  public CategorizationSectionPanel(GlobRepository repository, Directory directory) {
    super(SignpostSection.CATEGORIZATION, repository, directory);
  }

  protected boolean isCompleted(GlobRepository repository) {
    return SignpostStatus.isCompleted(SignpostStatus.GOTO_BUDGET_SHOWN, repository);
  }
}

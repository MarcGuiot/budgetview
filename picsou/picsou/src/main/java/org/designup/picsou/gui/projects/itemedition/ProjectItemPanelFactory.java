package org.designup.picsou.gui.projects.itemedition;

import org.designup.picsou.gui.projects.ProjectItemPanel;
import org.designup.picsou.model.ProjectItemType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidParameter;

public class ProjectItemPanelFactory {

  private final GlobRepository repository;
  private final Directory directory;

  public ProjectItemPanelFactory(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
  }

  public ProjectItemPanel create(Glob item) {
    switch (ProjectItemType.get(item)) {
      case EXPENSE:
        return new ProjectItemExpensePanel(item, repository, directory);
      case TRANSFER:
        return new ProjectItemTransferPanel(item, repository, directory);
      default:
        throw new InvalidParameter("Unexpected project item type for " + item);
    }
  }
}

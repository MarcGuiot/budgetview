package org.designup.picsou.gui.projects.actions;

import org.designup.picsou.model.Project;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.actions.SingleSelectionAction;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

public class SortProjectItemsAction extends SingleSelectionAction {
  public SortProjectItemsAction(GlobRepository repository, Directory directory) {
    super(Lang.get("projectEdition.sortItems"), Project.TYPE, GlobMatchers.ALL, repository, directory);
  }

  protected void process(Glob project, GlobRepository repository, Directory directory) {
    repository.startChangeSet();
    try {
      Project.sortItems(project, repository);
    }
    finally {
      repository.completeChangeSet();
    }
  }
}

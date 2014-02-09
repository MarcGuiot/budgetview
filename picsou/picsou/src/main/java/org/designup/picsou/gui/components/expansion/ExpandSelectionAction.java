package org.designup.picsou.gui.components.expansion;

import org.designup.picsou.gui.series.view.SeriesWrapper;
import org.globsframework.gui.actions.SingleSelectionAction;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class ExpandSelectionAction extends SingleSelectionAction {
  private TableExpansionModel expansionModel;

  public ExpandSelectionAction(TableExpansionModel expansionModel, GlobRepository repository, Directory directory) {
    super("", SeriesWrapper.TYPE, repository, directory);
    this.expansionModel = expansionModel;
  }

  protected void process(Glob wrapper, GlobRepository repository, Directory directory) {
    if (!expansionModel.isExpanded(wrapper)) {
      expansionModel.toggleExpansion(wrapper, false);
    }
  }
}

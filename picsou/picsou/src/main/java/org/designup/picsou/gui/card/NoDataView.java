package org.designup.picsou.gui.card;

import org.designup.picsou.gui.View;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class NoDataView extends View {
  protected NoDataView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder builder) {
  }
}

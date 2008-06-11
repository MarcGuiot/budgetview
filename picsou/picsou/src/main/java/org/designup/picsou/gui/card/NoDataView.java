package org.designup.picsou.gui.card;

import org.crossbowlabs.globs.gui.GlobsPanelBuilder;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.designup.picsou.gui.View;

public class NoDataView extends View {
  protected NoDataView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder builder) {
  }
}

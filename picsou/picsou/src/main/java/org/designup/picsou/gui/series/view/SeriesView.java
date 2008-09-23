package org.designup.picsou.gui.series.view;

import org.designup.picsou.gui.View;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;
import org.globsframework.model.impl.DefaultGlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.directory.DefaultDirectory;

public class SeriesView extends View  {
  private static DefaultDirectory localDirectory;
  public static final int LABEL_COLUMN_INDEX = 1;

  protected SeriesView(GlobRepository repository, Directory directory) {
    super(repository, createLocalDirectory(directory));
    GlobRepository localRepository = GlobRepositoryBuilder.createEmpty();

  }

  private static Directory createLocalDirectory(Directory directory) {
    localDirectory = new DefaultDirectory();
    localDirectory.add(SelectionService.class);
    return localDirectory;
  }

  public void registerComponents(GlobsPanelBuilder builder) {
  }
}

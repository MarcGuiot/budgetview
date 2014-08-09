package org.designup.picsou.gui.analysis;

import org.designup.picsou.gui.View;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class AnalysisSelector extends View {

  public AnalysisSelector(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/analysis/analysisSelector.splits",
                                                      repository, directory);
    parentBuilder.add("analysisSelector", builder);
  }
}

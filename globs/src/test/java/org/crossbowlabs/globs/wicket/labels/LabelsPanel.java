package org.crossbowlabs.globs.wicket.labels;

import org.crossbowlabs.globs.metamodel.DummyObject;
import org.crossbowlabs.globs.model.Key;
import org.crossbowlabs.globs.wicket.GlobRepositoryLoader;
import wicket.markup.html.panel.Panel;

public class LabelsPanel extends Panel {

  public LabelsPanel(String componentId, Key key, GlobRepositoryLoader repositoryLoader) {
    super(componentId);
    GlobLabels.init(this, key, repositoryLoader)
          .add(DummyObject.NAME)
          .add(DummyObject.VALUE)
          .add(DummyObject.DATE)
          .add(DummyObject.LINK);
  }
}

package org.globsframework.wicket.labels;

import org.apache.wicket.markup.html.panel.Panel;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.model.Key;
import org.globsframework.wicket.GlobRepositoryLoader;

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

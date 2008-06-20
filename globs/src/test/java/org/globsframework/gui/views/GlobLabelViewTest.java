package org.globsframework.gui.views;

import org.globsframework.metamodel.DummyObject;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifier;
import org.uispec4j.TextBox;

public class GlobLabelViewTest extends GlobTextViewTestCase {
  protected TextBox init(GlobRepository repository, boolean autoHide, GlobListStringifier stringifier) {
    GlobLabelView view =
      GlobLabelView.init(DummyObject.TYPE, repository, directory, stringifier)
      .setAutoHide(autoHide);
    return new TextBox(view.getComponent());
  }
}

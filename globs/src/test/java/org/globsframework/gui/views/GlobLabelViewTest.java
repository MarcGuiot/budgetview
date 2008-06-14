package org.globsframework.gui.views;

import org.globsframework.metamodel.DummyObject;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifier;
import org.uispec4j.TextBox;

public class GlobLabelViewTest extends GlobTextViewTestCase {
  protected TextBox init(final GlobRepository repository) {
    GlobLabelView view =
      GlobLabelView.init(DummyObject.TYPE, repository, directory,
                         new GlobListStringifier() {
                           public String toString(GlobList selected) {
                             return repository.getAll(DummyObject.TYPE).toString() + " / " + selected.toString();
                           }
                         });
    return new TextBox(view.getComponent());
  }
}

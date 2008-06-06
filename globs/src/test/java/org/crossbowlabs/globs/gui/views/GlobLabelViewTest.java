package org.crossbowlabs.globs.gui.views;

import org.crossbowlabs.globs.metamodel.DummyObject;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.format.GlobListStringifier;
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

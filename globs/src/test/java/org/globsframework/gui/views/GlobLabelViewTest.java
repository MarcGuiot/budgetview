package org.globsframework.gui.views;

import org.globsframework.metamodel.DummyObject;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.utils.GlobListMatcher;
import org.uispec4j.TextBox;

public class GlobLabelViewTest extends GlobTextViewTestCase {
  protected TextBox init(GlobRepository repository, boolean autoHide,
                         GlobListStringifier stringifier, GlobListMatcher matcher) {
    GlobLabelView view =
      GlobLabelView.init(DummyObject.TYPE, repository, directory, stringifier)
        .setAutoHideMatcher(matcher)
        .setAutoHideIfEmpty(autoHide);
    return new TextBox(view.getComponent());
  }
}

package org.globsframework.gui.views;

import org.globsframework.metamodel.DummyObject;
import org.globsframework.metamodel.Field;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Glob;
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

  protected TextBox init(GlobRepository repository, Field field) {
    GlobLabelView view = GlobLabelView.init(field, repository, directory);
    return new TextBox(view.getComponent());
  }

  protected TextBox init(GlobRepository repository, Glob glob) {
    GlobLabelView view =
      GlobLabelView.init(DummyObject.TYPE, repository, directory, stringifier)
        .forceSelection(glob);
    return new TextBox(view.getComponent());
  }
}

package org.globsframework.gui.views;

import org.globsframework.metamodel.DummyObject;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.utils.GlobListMatcher;
import org.uispec4j.TextBox;

public class GlobMultiLineTextViewTest extends GlobTextViewTestCase {
  protected TextBox init(GlobRepository repository, boolean autoHide,
                         GlobListStringifier stringifier, GlobListMatcher matcher) {
    GlobMultiLineTextView view =
      GlobMultiLineTextView.init(DummyObject.TYPE, repository, directory, stringifier)
        .setAutoHideMatcher(matcher)
        .setAutoHideIfEmpty(autoHide);
    return new TextBox(view.getComponent());
  }

  public void testComponentIsNotEditable() throws Exception {
    TextBox textBox = init(repository, true);
    assertFalse(textBox.isEditable());
  }
}
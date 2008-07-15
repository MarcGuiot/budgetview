package org.globsframework.gui.views;

import org.globsframework.metamodel.DummyObject;
import org.globsframework.metamodel.Field;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifier;
import org.uispec4j.TextBox;

public class GlobLabelViewTest extends GlobTextViewTestCase {

  protected GlobLabelView initView(GlobRepository repository, GlobListStringifier stringifier) {
    return GlobLabelView.init(DummyObject.TYPE, repository, directory, stringifier);
  }

  protected GlobLabelView initView(GlobRepository repository, Field field) {
    return GlobLabelView.init(field, repository, directory);
  }

  protected TextBox createTextBox(AbstractGlobTextView view) {
    return new TextBox(((GlobLabelView)view).getComponent());
  }
}

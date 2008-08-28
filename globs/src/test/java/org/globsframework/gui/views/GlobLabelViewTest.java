package org.globsframework.gui.views;

import org.globsframework.metamodel.DummyObject;
import org.globsframework.metamodel.Field;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifier;

public class GlobLabelViewTest extends GlobTextViewTestCase {

  protected GlobLabelView initView(GlobRepository repository, GlobListStringifier stringifier) {
    return GlobLabelView.init(DummyObject.TYPE, repository, directory, stringifier);
  }

  protected GlobLabelView initView(GlobRepository repository, Field field) {
    return GlobLabelView.init(field, repository, directory);
  }

  protected TextComponent createComponent(AbstractGlobTextView view) {
    return new TextBoxComponent(((GlobLabelView)view).getComponent());
  }
}

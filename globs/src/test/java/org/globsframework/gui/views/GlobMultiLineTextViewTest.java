package org.globsframework.gui.views;

import org.globsframework.metamodel.DummyObject;
import org.globsframework.metamodel.Field;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifier;

public class GlobMultiLineTextViewTest extends GlobTextViewTestCase {

  protected GlobMultiLineTextView initView(GlobRepository repository, GlobListStringifier stringifier) {
    return GlobMultiLineTextView.init(DummyObject.TYPE, repository, directory, stringifier);
  }

  protected GlobMultiLineTextView initView(GlobRepository repository, Field field) {
    return GlobMultiLineTextView.init(field, repository, directory);
  }

  protected TextComponent createComponent(AbstractGlobTextView view) {
    return new TextBoxComponent(((GlobMultiLineTextView)view).getComponent());
  }

  public void testComponentIsNotEditable() throws Exception {
    TextComponent textBox = initWithAutoHide(repository);
    assertFalse(textBox.isEditable());
  }
}
package org.globsframework.gui.views;

import org.globsframework.metamodel.DummyObject;
import org.globsframework.metamodel.Field;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifier;
import org.uispec4j.TextBox;

public class GlobMultiLineTextViewTest extends GlobTextViewTestCase {

  protected GlobMultiLineTextView initView(GlobRepository repository, GlobListStringifier stringifier) {
    return GlobMultiLineTextView.init(DummyObject.TYPE, repository, directory, stringifier);
  }

  protected GlobMultiLineTextView initView(GlobRepository repository, Field field) {
    return GlobMultiLineTextView.init(field, repository, directory);
  }

  protected TextBox createTextBox(AbstractGlobTextView view) {
    return new TextBox(((GlobMultiLineTextView)view).getComponent());
  }

  public void testComponentIsNotEditable() throws Exception {
    TextBox textBox = initWithAutoHide(repository);
    assertFalse(textBox.isEditable());
  }
}
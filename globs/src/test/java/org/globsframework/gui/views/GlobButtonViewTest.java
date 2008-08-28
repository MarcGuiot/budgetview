package org.globsframework.gui.views;

import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.DummyObject;
import org.uispec4j.Button;

import javax.swing.*;

public class GlobButtonViewTest extends GlobTextViewTestCase {
  protected GlobButtonView initView(GlobRepository repository, GlobListStringifier stringifier) {
    return GlobButtonView.init(DummyObject.TYPE, repository, directory, stringifier);
  }

  protected GlobButtonView initView(GlobRepository repository, Field field) {
    return GlobButtonView.init(field, repository, directory);
  }

  protected TextComponent createComponent(AbstractGlobTextView view) {
    JButton jButton = ((GlobButtonView)view).getComponent();
    return new ButtonComponent(new Button(jButton));
  }
}

package org.globsframework.gui.views;

import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.DummyGlobListFunctor;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.gui.utils.GlobSelectionBuilder;
import org.uispec4j.Button;

import javax.swing.*;

public class GlobButtonViewTest extends GlobTextAndIconViewTestCase {

  private DummyGlobListFunctor callback = new DummyGlobListFunctor();

  protected GlobButtonView initView(GlobRepository repository, GlobListStringifier stringifier) {
    return GlobButtonView.init(DummyObject.TYPE, repository, directory, stringifier, callback);
  }

  protected GlobButtonView initView(GlobRepository repository, Field field) {
    return GlobButtonView.init(field, repository, directory, callback);
  }

  protected TextComponent createComponent(AbstractGlobTextView view) {
    JButton jButton = ((GlobButtonView)view).getComponent();
    return new ButtonComponent(new Button(jButton));
  }

  public void testExecution() throws Exception {
    GlobButtonView view = GlobButtonView.init(DummyObject.TYPE, repository, directory, callback);
    Button button = new Button(view.getComponent());

    callback.checkNothingReceived();

    button.click();
    callback.checkEmpty();
    
    selectionService.select(glob1);
    button.click();
    callback.checkReceived(glob1);
  }

  public void testCallbackReceivesAFilteredList() throws Exception {
    GlobButtonView view =
      GlobButtonView.init(DummyObject.TYPE, repository, directory, callback)
      .setFilter(GlobMatchers.keyEquals(glob1.getKey()));

    Button button = new Button(view.getComponent());
    
    selectionService.select(GlobSelectionBuilder.init().add(glob1).add(glob2).get());
    button.click();
    callback.checkReceived(glob1);
  }
}

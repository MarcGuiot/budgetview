package org.designup.picsou.gui.budget.components;

import org.designup.picsou.gui.components.PopupGlobFunctor;
import org.designup.picsou.gui.series.utils.SeriesPopupFactory;
import org.designup.picsou.model.Series;
import org.globsframework.gui.ComponentHolder;
import org.globsframework.gui.utils.DisposablePopupMenuFactory;
import org.globsframework.gui.utils.PopupMenuFactory;
import org.globsframework.gui.views.GlobButtonView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobListFunctor;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class NameLabelPopupButton implements ComponentHolder {

  private GlobButtonView buttonView;
  private DisposablePopupMenuFactory popupFactory;

  public NameLabelPopupButton(Key key, DisposablePopupMenuFactory popupMenuFactory, GlobRepository repository, Directory directory) {
    this.popupFactory = popupMenuFactory;
    PopupGlobFunctor functor = new PopupGlobFunctor(popupFactory);
    buttonView = GlobButtonView.init(key.getGlobType(), repository, directory, functor)
      .forceSelection(key);
    functor.setComponent(buttonView.getComponent());
  }

  public JButton getComponent() {
    return buttonView.getComponent();
  }

  public ComponentHolder setName(String name) {
    return buttonView.setName(name);
  }

  public void dispose() {
    buttonView.dispose();
    popupFactory.dispose();
    popupFactory = null;
  }
}

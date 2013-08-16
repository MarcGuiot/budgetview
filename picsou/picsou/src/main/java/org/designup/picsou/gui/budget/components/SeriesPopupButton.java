package org.designup.picsou.gui.budget.components;

import org.designup.picsou.gui.components.PopupGlobFunctor;
import org.designup.picsou.gui.series.utils.SeriesPopupFactory;
import org.designup.picsou.model.Series;
import org.globsframework.gui.ComponentHolder;
import org.globsframework.gui.views.GlobButtonView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobListFunctor;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class SeriesPopupButton implements ComponentHolder {

  private GlobButtonView buttonView;
  private SeriesPopupFactory popupFactory;

  public SeriesPopupButton(Glob series, GlobListFunctor editSeriesFunctor, GlobRepository repository, Directory directory) {
    popupFactory = new SeriesPopupFactory(series, editSeriesFunctor, repository, directory);
    PopupGlobFunctor functor = new PopupGlobFunctor(popupFactory);
    buttonView = GlobButtonView.init(Series.TYPE, repository, directory, functor)
      .forceSelection(series.getKey());
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

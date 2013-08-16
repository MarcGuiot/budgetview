package org.designup.picsou.gui.components;

import org.designup.picsou.gui.series.utils.SeriesPopupFactory;
import org.globsframework.gui.utils.PopupMenuFactory;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobListFunctor;
import org.globsframework.utils.exceptions.InvalidState;

import javax.swing.*;

public class PopupGlobFunctor implements GlobListFunctor {

  private PopupMenuFactory factory;
  private JComponent component;

  public PopupGlobFunctor(final JPopupMenu menu) {
    this.factory = new PopupMenuFactory() {
      public JPopupMenu createPopup() {
        return menu;
      }
    };
  }

  public PopupGlobFunctor(PopupMenuFactory popupFactory) {
    this.factory = popupFactory;
  }

  public void setComponent(JComponent component) {
    this.component = component;
  }

  public void run(GlobList list, GlobRepository repository) {
    if (component == null) {
      throw new InvalidState("A component must be set for the popup to be shown - use setComponent(JComponent)");
    }
    JPopupMenu menu = factory.createPopup();
    if (!menu.isShowing()) {
      menu.show(component, 0, component.getHeight());
    }
  }
}

package org.globsframework.gui.utils;

import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;

import javax.swing.*;

public class GlobBooleanVisibilityUpdater extends AbstractGlobBooleanUpdater {

  private JComponent component;

  public static GlobBooleanVisibilityUpdater init(Key key, BooleanField field, JComponent component, GlobRepository repository) {
    GlobBooleanVisibilityUpdater updater = new GlobBooleanVisibilityUpdater(key, field, component, repository);
    updater.setKey(key);
    updater.update();
    return updater;
  }

  public GlobBooleanVisibilityUpdater(Key key, BooleanField field, JComponent component, GlobRepository repository) {
    super(field, repository);
    this.component = component;
  }

  protected void doUpdate(boolean value) {
    component.setVisible(value);
  }

  public void dispose() {
    super.dispose();
  }
}

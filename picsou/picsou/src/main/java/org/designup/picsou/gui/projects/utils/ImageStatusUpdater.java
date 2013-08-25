package org.designup.picsou.gui.projects.utils;

import org.designup.picsou.gui.components.images.GlobImageLabelView;
import org.globsframework.gui.utils.AbstractGlobBooleanUpdater;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;

public class ImageStatusUpdater extends AbstractGlobBooleanUpdater {
  private GlobImageLabelView view;

  public ImageStatusUpdater(BooleanField field, GlobImageLabelView view, GlobRepository repository) {
    this(null, field, view, repository);
  }

  public ImageStatusUpdater(Key key, BooleanField field, GlobImageLabelView view, GlobRepository repository) {
    super(field, repository);
    this.view = view;
    if (key != null) {
      setKey(key);
    }
  }

  protected void doUpdate(boolean enabled) {
    view.setEnabled(enabled);
  }
}

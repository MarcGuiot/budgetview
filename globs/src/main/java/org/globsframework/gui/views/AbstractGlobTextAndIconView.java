package org.globsframework.gui.views;

import org.globsframework.metamodel.GlobType;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public abstract class AbstractGlobTextAndIconView <T extends AbstractGlobTextAndIconView>
  extends AbstractGlobTextView<T> {

  private GlobListIconifier iconifier;

  protected AbstractGlobTextAndIconView(GlobType type,
                                        GlobRepository repository, 
                                        Directory directory, 
                                        GlobListStringifier stringifier) {
    super(type, repository, directory, stringifier);
  }
  
  public T setIconifier(GlobListIconifier iconifier) {
    this.iconifier = iconifier;
    return (T)this;
  }

  protected void postTextUpdate(GlobList filteredSelection, GlobRepository repository) {
    if (iconifier != null) {
      Icon icon = iconifier.getIcon(filteredSelection, repository);
      doUpdate(icon);
    }
  }

  protected abstract void doUpdate(Icon icon);
}

package org.designup.picsou.gui.time.selectable;

import org.globsframework.model.Glob;

import java.util.Collection;

public interface Selectable extends ChainedSelectableElement {

  enum Visibility {
    PARTIALLY,
    FULLY,
    NOT_VISIBLE
  }

  void select();

  void unSelect();

  void inverseSelect();

  String getCommonParent();

  void getSelectedGlobs(Collection<Glob> selected);

  Visibility isVisible();
}

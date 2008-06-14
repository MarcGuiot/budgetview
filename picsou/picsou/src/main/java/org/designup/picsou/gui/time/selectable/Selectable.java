package org.designup.picsou.gui.time.selectable;

import org.globsframework.model.Glob;

import java.util.Collection;

public interface Selectable extends ChainedSelectableElement {

  void select();

  void unSelect();

  void inverseSelect();

  String getCommonParent();

  void getObject(Collection<Glob> selected);

}

package org.globsframework.wicket.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.model.GlobList;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.wicket.GlobPage;
import wicket.Component;
import wicket.model.AbstractReadOnlyModel;

public class GlobListModel extends AbstractReadOnlyModel {
  private GlobType type;
  private GlobMatcher matcher;

  public GlobListModel(GlobType type, GlobMatcher matcher) {
    this.type = type;
    this.matcher = matcher;
  }

  public void setType(GlobType type) {
    this.type = type;
  }

  public void setMatcher(GlobMatcher matcher) {
    this.matcher = matcher;
  }

  public GlobType getType() {
    return type;
  }

  public GlobList getObject(Component component) {
    GlobPage page = (GlobPage)component.getPage();
    GlobList list = page.getRepository().getAll(type, matcher);
    list.sort(page.getDescriptionService().getStringifier(type).getComparator(page.getRepository()));
    return filter(list);
  }

  protected GlobList filter(GlobList list) {
    return list;
  }

  public String toString() {
    return type.getName() + " / " + matcher;
  }
}

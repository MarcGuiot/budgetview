package org.crossbowlabs.globs.wicket.model;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.utils.GlobMatcher;
import org.crossbowlabs.globs.wicket.GlobPage;
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
    return filter(list, page.getRepository());
  }

  protected GlobList filter(GlobList list, GlobRepository repository) {
    return list;
  }

  public String toString() {
    return type.getName() + " / " + matcher;
  }
}

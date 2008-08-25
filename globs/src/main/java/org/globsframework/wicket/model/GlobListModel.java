package org.globsframework.wicket.model;

import org.apache.wicket.model.AbstractReadOnlyModel;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.directory.Directory;

public class GlobListModel extends AbstractReadOnlyModel {
  private GlobType type;
  private GlobMatcher matcher;
  private GlobRepository repository;
  private Directory directory;

  public GlobListModel(GlobType type, GlobMatcher matcher, GlobRepository repository, Directory directory) {
    this.type = type;
    this.matcher = matcher;
    this.repository = repository;
    this.directory = directory;
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

  public GlobList getObject() {
    GlobList list = repository.getAll(type, matcher);
    list.sort(directory.get(DescriptionService.class).getStringifier(type).getComparator(repository));
    return filter(list);
  }

  protected GlobList filter(GlobList list) {
    return list;
  }

  public String toString() {
    return type.getName() + " / " + matcher;
  }
}

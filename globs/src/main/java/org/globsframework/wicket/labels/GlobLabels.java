package org.globsframework.wicket.labels;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.Link;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.model.Key;
import org.globsframework.wicket.GlobRepositoryLoader;
import org.globsframework.wicket.model.GlobFieldStringifierModel;
import org.globsframework.wicket.model.GlobLinkStringifierModel;

public class GlobLabels {
  private final MarkupContainer parent;
  private final Key key;
  private final GlobRepositoryLoader repositoryLoader;

  public static GlobLabels init(MarkupContainer parent, Key key, GlobRepositoryLoader repositoryLoader) {
    return new GlobLabels(parent, key, repositoryLoader);
  }

  private GlobLabels(MarkupContainer parent, Key key, GlobRepositoryLoader repositoryLoader) {
    this.parent = parent;
    this.key = key;
    this.repositoryLoader = repositoryLoader;
  }

  public GlobLabels add(Field field) {
    parent.add(new Label(field.getName(), new GlobFieldStringifierModel(key, field, repositoryLoader)));
    return this;
  }

  public GlobLabels add(LinkField field) {
    return add((Link)field);
  }

  public GlobLabels add(Link link) {
    parent.add(new Label(link.getName(), new GlobLinkStringifierModel(key, link, repositoryLoader)));
    return this;
  }
}

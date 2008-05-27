package org.crossbowlabs.globs.wicket.labels;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.Link;
import org.crossbowlabs.globs.metamodel.fields.LinkField;
import org.crossbowlabs.globs.model.Key;
import org.crossbowlabs.globs.wicket.GlobRepositoryLoader;
import org.crossbowlabs.globs.wicket.model.GlobFieldStringifierModel;
import org.crossbowlabs.globs.wicket.model.GlobLinkStringifierModel;
import wicket.MarkupContainer;
import wicket.markup.html.basic.Label;

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

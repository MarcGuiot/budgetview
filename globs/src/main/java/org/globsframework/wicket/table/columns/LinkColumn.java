package org.globsframework.wicket.table.columns;

import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.MutableFieldValues;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.wicket.component.LinkButtonPanel;
import wicket.Component;
import wicket.markup.html.link.Link;

public abstract class LinkColumn extends AbstractGlobTableColumn {
  private final String linkText;
  private final Link link;

  public LinkColumn(String title, String linkText, Link link) {
    super(title);
    this.linkText = linkText;
    this.link = link;
  }

  public Component getComponent(String id,
                                String tableId,
                                Key key,
                                MutableFieldValues fieldValues,
                                int rowIndex,
                                Component row,
                                GlobRepository repository,
                                DescriptionService descriptionService) {
    return new LinkButtonPanel(id, linkText, link);
  }
}

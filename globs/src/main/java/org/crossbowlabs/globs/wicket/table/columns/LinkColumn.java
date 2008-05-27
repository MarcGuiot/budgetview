package org.crossbowlabs.globs.wicket.table.columns;

import wicket.Component;
import wicket.markup.html.link.Link;
import org.crossbowlabs.globs.model.Key;
import org.crossbowlabs.globs.model.MutableFieldValues;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.wicket.component.LinkButtonPanel;

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

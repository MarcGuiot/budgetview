package org.crossbowlabs.globs.wicket.table.columns;

import org.crossbowlabs.globs.metamodel.Link;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.Key;
import org.crossbowlabs.globs.model.MutableFieldValues;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.model.format.GlobStringifier;
import wicket.Component;
import wicket.markup.html.basic.Label;

public class GlobLinkColumn extends AbstractGlobTableColumn {
  private final Link link;

  public GlobLinkColumn(Link link, DescriptionService service) {
    super(service.getLabel(link));
    this.link = link;
  }

  public Component getComponent(String id,
                                String tableId,
                                Key key,
                                MutableFieldValues linkValues,
                                int rowIndex,
                                Component row,
                                GlobRepository repository,
                                DescriptionService descriptionService) {
    GlobStringifier stringifier = descriptionService.getStringifier(link);
    Glob glob = repository.get(key);
    String stringValue = stringifier.toString(glob, repository);
    return new Label(id, stringValue);
  }
}

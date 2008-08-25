package org.globsframework.wicket.table.columns;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.globsframework.metamodel.Link;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.MutableFieldValues;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;

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

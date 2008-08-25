package org.globsframework.wicket.table.columns;

import org.apache.wicket.Component;
import org.globsframework.metamodel.Link;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.MutableFieldValues;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.wicket.editors.LinkEditorPanelFactory;

public class GlobLinkEditorColumn extends AbstractGlobTableColumn {
  private final Link link;

  public GlobLinkEditorColumn(Link link, DescriptionService service) {
    super(service.getLabel(link));
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
    String componentId = link.getName() + "_" + rowIndex;
    return LinkEditorPanelFactory.getPanel(id, componentId, link, fieldValues, repository,
                                           descriptionService);
  }
}

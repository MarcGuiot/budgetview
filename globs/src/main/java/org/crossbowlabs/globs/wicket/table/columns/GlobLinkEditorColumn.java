package org.crossbowlabs.globs.wicket.table.columns;

import org.crossbowlabs.globs.metamodel.Link;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.Key;
import org.crossbowlabs.globs.model.MutableFieldValues;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.wicket.editors.LinkEditorPanelFactory;
import wicket.Component;

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

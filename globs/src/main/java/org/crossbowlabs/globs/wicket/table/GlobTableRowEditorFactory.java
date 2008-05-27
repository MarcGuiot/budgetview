package org.crossbowlabs.globs.wicket.table;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.Key;
import org.crossbowlabs.globs.model.MutableFieldValues;
import org.crossbowlabs.globs.model.format.DescriptionService;
import wicket.Component;
import wicket.markup.html.WebMarkupContainer;
import java.io.Serializable;

public interface GlobTableRowEditorFactory extends Serializable {

  GlobTableRowEditor getEditor(String switcherId,
                               String editorId,
                               Key key,
                               MutableFieldValues fieldValues,
                               Component tr,
                               int rowIndex,
                               GlobRepository repository,
                               DescriptionService descriptionService);

  GlobTableRowEditorFactory NULL = new GlobTableRowEditorFactory() {
    public GlobTableRowEditor getEditor(String switcherId,
                                        final String editorId,
                                        Key key,
                                        MutableFieldValues fieldValues,
                                        Component tr,
                                        int rowIndex,
                                        GlobRepository repository,
                                        DescriptionService descriptionService) {
      WebMarkupContainer editionPanel = new WebMarkupContainer(editorId);
      editionPanel.setVisible(false);
      return new GlobTableRowEditor(null, editionPanel);
    }
  };
}

package org.globsframework.wicket.table;

import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.MutableFieldValues;
import org.globsframework.model.format.DescriptionService;
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

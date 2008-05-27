package org.crossbowlabs.globs.wicket.editors;

import org.crossbowlabs.globs.wicket.editors.converters.DateConverter;
import org.crossbowlabs.globs.wicket.model.FieldValueModel;
import wicket.extensions.markup.html.datepicker.DatePicker;
import wicket.extensions.markup.html.form.DateTextField;
import wicket.markup.html.panel.Panel;
import wicket.model.Model;
import wicket.util.convert.IConverter;

public class DateEditorPanel extends Panel {

  public DateEditorPanel(String parentId,
                         FieldValueModel model,
                         final String componentId,
                         String label) {
    super(parentId);

    DateTextField field = new DateTextField("editor", model) {
      public String getInputName() {
        return componentId;
      }

      public IConverter getConverter() {
        return new DateConverter();
      }
    };
    field.setLabel(new Model(label));
    add(field);
    add(new DatePicker("dateFieldPicker", field));
  }

}

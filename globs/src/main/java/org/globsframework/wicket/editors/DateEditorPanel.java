package org.globsframework.wicket.editors;

import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.IConverter;
import org.globsframework.wicket.editors.converters.DateConverter;
import org.globsframework.wicket.model.FieldValueModel;

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

      public IConverter getConverter(Class type) {
        return new DateConverter();
      }

    };
    field.setLabel(new Model(label));
    add(field);

//    add(new DatePicker("dateFieldPicker", field));
    DatePicker picker = new DatePicker();
    picker.bind(field);
    add(picker);
  }

}

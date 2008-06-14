package org.globsframework.wicket.editors;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.annotations.MultiLineText;
import org.globsframework.metamodel.fields.*;
import org.globsframework.model.MutableFieldValues;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.utils.Ref;
import org.globsframework.utils.exceptions.NotSupported;
import org.globsframework.wicket.editors.converters.DoubleConverter;
import org.globsframework.wicket.form.GlobFormFeedbackBorder;
import org.globsframework.wicket.model.FieldValueModel;
import wicket.Component;
import wicket.markup.html.form.TextArea;
import wicket.markup.html.form.TextField;
import wicket.markup.html.form.validation.StringValidator;
import wicket.markup.html.panel.Panel;
import wicket.model.IModel;
import wicket.model.Model;
import wicket.util.convert.IConverter;

public class FieldEditorPanelFactory {
  private FieldEditorPanelFactory() {
  }

  public static Component getPanel(String panelId,
                                   String componentId,
                                   Field field,
                                   MutableFieldValues values,
                                   DescriptionService descriptionService) {
    FieldValueModel model = new FieldValueModel(field, values);
    Ref<Panel> panelRef = new Ref<Panel>();
    String label = descriptionService.getLabel(field);
    field.safeVisit(new FactoryVisitor(GlobFormFeedbackBorder.CHILD_ID, componentId, panelRef, model, label));

    Panel panel = panelRef.get();
    panel.setRenderBodyOnly(true);

    GlobFormFeedbackBorder feedbackBorder = new GlobFormFeedbackBorder(panelId);
    feedbackBorder.add(panel);

    return feedbackBorder;
  }

  private static class FactoryVisitor implements FieldVisitor {
    private final String parentId;
    private final String componentId;
    private final Ref<Panel> panel;
    private final FieldValueModel model;
    private final String label;

    private FactoryVisitor(String parentId,
                           String componentId,
                           Ref<Panel> panel,
                           FieldValueModel model,
                           String label) {
      this.parentId = parentId;
      this.componentId = componentId;
      this.panel = panel;
      this.model = model;
      this.label = label;
    }

    public void visitInteger(IntegerField field) throws Exception {
      panel.set(new TextEditorPanel(parentId, new NamedTextField(componentId, model, label, Integer.class),
                                    field.isRequired()));
    }

    public void visitDate(DateField field) throws Exception {
      panel.set(new DateEditorPanel(parentId, model, componentId, label));
    }

    public void visitDouble(DoubleField field) throws Exception {
      NamedTextField textField = new DecimalTextField(componentId, model, label);
      panel.set(new TextEditorPanel(parentId, textField, field.isRequired()));
    }

    public void visitString(StringField field) throws Exception {
      if (field.hasAnnotation(MultiLineText.class)) {
        panel.set(new MultiLineTextEditorPanel(parentId, new NamedTextArea(componentId, model, label),
                                               field.getMaxSize(), field.isRequired()));
      }
      else {
        NamedTextField textField = new NamedTextField(componentId, model, label, String.class);
        textField.add(StringValidator.maximumLength(field.getMaxSize()));
        panel.set(new TextEditorPanel(parentId, textField, field.isRequired()));
      }
    }

    public void visitBoolean(BooleanField field) throws Exception {
      panel.set(new BooleanEditorPanel(parentId, model, componentId, label));
    }

    public void visitLong(LongField field) throws Exception {
      throw new NotSupported(field.getName());
    }

    public void visitTimeStamp(TimeStampField field) throws Exception {
      throw new NotSupported(field.getName());
    }

    public void visitLink(LinkField field) throws Exception {
      throw new NotSupported("Use " + LinkEditorPanelFactory.class.getSimpleName()
                             + " for link " + field.getName());
    }

    public void visitBlob(BlobField field) throws Exception {
      throw new NotSupported(field.getName());
    }
  }

  static class NamedTextField extends TextField {
    private String componentId;

    public NamedTextField(String componentId, IModel model, String label, Class typeClass) {
      super(TextEditorPanel.ID, model, typeClass);
      this.componentId = componentId;
      setLabel(new Model(label));
    }

    public String getInputName() {
      return componentId;
    }
  }

  private static class DecimalTextField extends NamedTextField {
    public DecimalTextField(String componentId, IModel model, String label) {
      super(componentId, model, label, Double.class);
    }

    public IConverter getConverter() {
      return new DoubleConverter();
    }
  }

  static class NamedTextArea extends TextArea {
    private String componentId;

    public NamedTextArea(String componentId, IModel model, String label) {
      super(TextEditorPanel.ID, model);
      this.componentId = componentId;
      setLabel(new Model(label));
    }

    public String getInputName() {
      return componentId;
    }
  }
}

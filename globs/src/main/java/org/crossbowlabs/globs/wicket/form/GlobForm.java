package org.crossbowlabs.globs.wicket.form;

import java.util.ArrayList;
import java.util.List;
import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.Link;
import org.crossbowlabs.globs.model.FieldValues;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.Key;
import org.crossbowlabs.globs.model.MutableFieldValues;
import org.crossbowlabs.globs.model.utils.DefaultFieldValues;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.utils.exceptions.InvalidParameter;
import org.crossbowlabs.globs.utils.exceptions.ItemAlreadyExists;
import org.crossbowlabs.globs.utils.exceptions.MissingInfo;
import org.crossbowlabs.globs.wicket.GlobPage;
import org.crossbowlabs.globs.wicket.editors.FieldEditorPanelFactory;
import org.crossbowlabs.globs.wicket.editors.LinkEditorPanelFactory;
import wicket.AttributeModifier;
import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.markup.html.AjaxFallbackLink;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.Form;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.markup.html.panel.Panel;
import wicket.model.Model;

public class GlobForm extends Panel {
  private GlobType type;
  private Key key;
  private List fields = new ArrayList();
  private FieldValues defaultFieldValues;
  private MutableFieldValues values;
  private GlobFormCancelAction cancelAction;

  GlobForm(String id,
           GlobType type,
           Key key,
           List fields,
           MutableFieldValues values,
           FieldValues defaultFieldValues,
           GlobFormCancelAction cancelAction) {
    super(id);

    this.type = type;
    this.key = key;
    this.fields = fields;
    this.values = values;
    this.defaultFieldValues = defaultFieldValues;
    this.cancelAction = cancelAction;
    add(new InnerForm(id));
    add(new AttributeModifier("id", true, new Model(id)));
    setRenderBodyOnly(true);
  }

  private class InnerForm extends Form {
    private GlobForm.RowList rowList;

    private InnerForm(String parentId) {
      super("form");
      this.rowList = new RowList(parentId);
      add(rowList);
      add(new CancelButton());
      add(new AttributeModifier("id", true, new Model(parentId)));
    }

    protected void onSubmit() {
      GlobRepository repository = ((GlobPage)getPage()).getRepository();
      try {

        for (Field field : defaultFieldValues.getMap().keySet()) {
          if (!values.contains(field)) {
            values.setValue(field, defaultFieldValues.getMap().get(field));
          }
        }
        if (key == null) {
          repository.create(type, values.toArray());
          values = new DefaultFieldValues();
          rowList.removeAll();
        }
        else {
          repository.update(key, values.toArray());
        }
      }
      catch (MissingInfo missingInfo) {
        error("Information manquante");
      }
      catch (ItemAlreadyExists itemAlreadyExists) {
        error("L'identifiant est déjà utilisé");
      }
    }
  }

  private class CancelButton extends AjaxFallbackLink {
    private CancelButton() {
      super("cancel");
      add(new AttributeModifier("value", new Model(cancelAction.getName())));
      if (cancelAction == GlobFormCancelAction.NULL) {
        setVisible(false);
      }
    }

    public void onClick(final AjaxRequestTarget target) {
      cancelAction.run(GlobForm.this, target);
    }
  }

  private class RowList extends ListView {
    private final String parentId;

    private RowList(String parentId) {
      super("rows", fields);
      this.parentId = parentId;
      setReuseItems(true);
    }

    protected void populateItem(final ListItem item) {
      GlobPage page = (GlobPage)getPage();
      DescriptionService descriptionService = page.getDescriptionService();
      Object obj = item.getModelObject();

      if (obj instanceof Link) {
        Link link = (Link)obj;
        addLabel(item, descriptionService.getLabel(link));
        String componentId = parentId + "_" + link.getName();
        item.add(LinkEditorPanelFactory.getPanel("editor", componentId, link, values,
                                                 page.getRepository(), descriptionService));
      }
      else if (obj instanceof Field) {
        Field field = (Field)obj;
        addLabel(item, descriptionService.getLabel(field));
        String componentId = parentId + "_" + field.getName();
        item.add(FieldEditorPanelFactory.getPanel("editor", componentId, field, values, descriptionService));
      }
      else {
        throw new InvalidParameter("Unexpected item: " + obj);
      }
    }

    private void addLabel(ListItem item, String labelText) {
      Label label = new Label("label", labelText);
      label.setRenderBodyOnly(true);
      item.add(label);
    }
  }
}

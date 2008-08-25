package org.globsframework.wicket.table.columns;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.MutableFieldValues;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.wicket.GlobPage;

class ButtonPanel extends Panel {

  ButtonPanel(String id,
              String buttonLabel,
              String buttonId,
              ButtonColumn column,
              Key key,
              MutableFieldValues fieldValues,
              int rowIndex) {
    super(id);

    LinkButton link = new LinkButton(buttonId, buttonLabel, column, key, fieldValues, rowIndex);
    init(link);
    add(link);
  }

  protected void init(Link link) {
  }

  private class LinkButton extends Link {
    private ButtonColumn column;
    private Key key;
    private MutableFieldValues fieldValues;
    private int rowIndex;
    private String buttonId;

    LinkButton(String buttonId,
               String buttonLabel,
               ButtonColumn column,
               Key key,
               MutableFieldValues fieldValues,
               int rowIndex) {
      super("button");
      this.buttonId = buttonId;
      this.column = column;
      this.key = key;
      this.fieldValues = fieldValues;
      this.rowIndex = rowIndex;
      add(new AttributeModifier("id", new Model(getInputName())));
      add(new AttributeModifier("value", new Model(buttonLabel)));
    }

    public String getInputName() {
      return buttonId + "_" + rowIndex;
    }

    public void onClick() {
      column.onSubmit(key, fieldValues, rowIndex, getRepository(), getDescriptionService());
    }
  }

  private DescriptionService getDescriptionService() {
    GlobPage page = (GlobPage)getPage();
    return page.getDirectory().get(DescriptionService.class);
  }

  private GlobRepository getRepository() {
    GlobPage page = (GlobPage)getPage();
    return page.getRepository();
  }
}


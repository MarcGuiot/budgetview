package org.crossbowlabs.globs.wicket.table.columns;

import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.Key;
import org.crossbowlabs.globs.model.MutableFieldValues;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.wicket.GlobPage;
import wicket.markup.html.panel.Panel;
import wicket.markup.html.link.Link;
import wicket.model.Model;
import wicket.AttributeModifier;

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


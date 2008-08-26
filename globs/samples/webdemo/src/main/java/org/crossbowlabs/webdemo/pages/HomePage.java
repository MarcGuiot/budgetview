package org.crossbowlabs.webdemo.pages;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.crossbowlabs.webdemo.model.Person;
import org.crossbowlabs.webdemo.model.RepositoryLoader;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.MutableFieldValues;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.wicket.GlobPage;
import org.globsframework.wicket.GlobRepositoryLoader;
import org.globsframework.wicket.component.LinkPanel;
import org.globsframework.wicket.form.GlobFormBuilder;
import org.globsframework.wicket.table.GlobTableBuilder;
import org.globsframework.wicket.table.GlobTableRowEditor;
import org.globsframework.wicket.table.GlobTableRowEditorFactory;
import org.globsframework.wicket.table.TableEditPolicy;
import org.globsframework.wicket.table.columns.AbstractGlobTableColumn;
import org.globsframework.wicket.table.columns.DeleteButtonColumn;

public class HomePage extends GlobPage {

  public HomePage() {

    FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
    feedbackPanel.setFilter(new IFeedbackMessageFilter() {
      public boolean accept(FeedbackMessage message) {
        System.out.println("HomePage.accept - " + message.getMessage() + " ==> " + message.isRendered());
        return !message.isRendered();
      }
    });
    add(feedbackPanel);

    add(GlobFormBuilder.init(Person.TYPE)
      .add(Person.FIRST_NAME)
      .add(Person.LAST_NAME)
      .add(Person.EMAIL)
      .add(Person.BIRTH_DATE)
      .add(Person.AGE)
      .add(Person.WEIGHT)
      .add(Person.REGISTERED)
      .add(Person.COMMENT)
      .create("creationForm"));

    add(GlobTableBuilder.init(Person.TYPE, TableEditPolicy.READ_ONLY, getRepository(), getDirectory())
      .add(Person.FIRST_NAME)
      .add(Person.LAST_NAME)
      .add(Person.EMAIL)
      .add(Person.BIRTH_DATE)
      .add(Person.REGISTERED)
      .add(Person.COMMENT)
      .getPanel("readOnlyTable"));

    add(GlobTableBuilder.init(Person.TYPE, TableEditPolicy.ROW, getRepository(), getDirectory())
      .addFieldEditor(Person.FIRST_NAME)
      .addFieldEditor(Person.LAST_NAME)
      .addFieldEditor(Person.EMAIL)
      .addFieldEditor(Person.BIRTH_DATE)
      .addFieldEditor(Person.REGISTERED)
      .addSubmitButton("")
      .getPanel("rowEditTable"));

    add(GlobTableBuilder.init(Person.TYPE, TableEditPolicy.TABLE, getRepository(), getDirectory())
      .addFieldEditor(Person.FIRST_NAME)
      .addFieldEditor(Person.LAST_NAME)
      .addFieldEditor(Person.EMAIL)
      .addFieldEditor(Person.BIRTH_DATE)
      .addFieldEditor(Person.REGISTERED)
      .add(new DeleteButtonColumn("", "Delete", "delete"))
      .getPanel("globalEditTable"));

    add(GlobTableBuilder.init(Person.TYPE, TableEditPolicy.READ_ONLY, getRepository(), getDirectory())
      .add(Person.FIRST_NAME)
      .add(Person.LAST_NAME)
      .add(Person.EMAIL)
      .add(Person.BIRTH_DATE)
      .add(Person.REGISTERED)
      .addRowEditor("Modify", new TableRowEditorFactory())
      .getPanel("expandableRowTable"));

    add(GlobTableBuilder.init(Person.TYPE, TableEditPolicy.READ_ONLY, getRepository(), getDirectory())
      .add(Person.FIRST_NAME)
      .add(Person.LAST_NAME)
      .add(Person.EMAIL)
      .add(Person.BIRTH_DATE)
      .add(Person.REGISTERED)
      .add(Person.COMMENT)
      .addDefaultRowEditor("Modify", "Edit")
      .add(new DeleteButtonColumn("", "Delete", "delete"))
      .add(new AbstractGlobTableColumn("Link") {
        public Component getComponent(String id,
                                      String tableId,
                                      Key key,
                                      MutableFieldValues fieldValues,
                                      int rowIndex,
                                      Component row,
                                      GlobRepository repository,
                                      DescriptionService descriptionService) {
          return new LinkPanel(id, "Go!", new ExternalLink(LinkPanel.ID, "http://www.google.com"));
        }
      })
      .getPanel("defaultRowEditor"));
  }

  protected GlobRepositoryLoader newGlobRepositoryLoader() {
    return new RepositoryLoader();
  }

  private class TableRowEditorFactory implements GlobTableRowEditorFactory {
    public GlobTableRowEditor getEditor(final String switcherId,
                                        final String editorId,
                                        Key key,
                                        MutableFieldValues fieldValues,
                                        final Component tr,
                                        int rowIndex,
                                        GlobRepository repository,
                                        DescriptionService descriptionService) {
      final PersonEditionPanel editionPanel = new PersonEditionPanel(editorId, tr);

      final Component switcher = new AjaxFallbackLink(switcherId) {
        public void onClick(final AjaxRequestTarget target) {
          editionPanel.setVisible(true);
          target.addComponent(tr);
        }
      };

      return new GlobTableRowEditor(switcher, editionPanel);
    }
  }
}
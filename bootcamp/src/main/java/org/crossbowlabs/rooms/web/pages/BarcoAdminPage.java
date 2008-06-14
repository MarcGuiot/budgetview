package org.globsframework.rooms.web.pages;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.globsframework.rooms.model.Barco;
import org.globsframework.rooms.model.PersistenceManager;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;


public class BarcoAdminPage extends Skin {
  private String barcoFilter;
  private TextField barcoNameFilter;
  private WebMarkupContainer barcosTable;

  public BarcoAdminPage() {

    barcoNameFilter = new TextField("BarcoNameFilter", new Model());
    barcoNameFilter.add(new OnChangeAjaxBehavior() {

      protected void onUpdate(AjaxRequestTarget ajaxRequestTarget) {
        barcoFilter = barcoNameFilter.getModelObjectAsString();
        ajaxRequestTarget.addComponent(barcosTable);
      }
    });

    barcosTable = new WebMarkupContainer("BarcosTable");

    ListView listView = new ListView("ShowBarcos", new BarcosModel()) {
      protected void populateItem(ListItem listItem) {
        Barco barco = (Barco) listItem.getModelObject();
        final Integer barcoId = barco.getId();
        listItem.add(new Label("name", barco.getName()));
        listItem.add(new Label("serialNumber", barco.getSerialNumber()));
        listItem.add(new Link("remove") {
          public void onClick() {
            Session session = PersistenceManager.getInstance().getNewSession();
            Transaction transaction = session.beginTransaction();
            session.delete(session.get(Barco.class, barcoId));
            transaction.commit();
            session.close();
          }
        });
      }
    };
    barcosTable.add(listView);
    barcosTable.setOutputMarkupId(true);

    WebMarkupContainer container = new WebMarkupContainer("BarcosDiv");
    container.add(barcoNameFilter);
    container.add(barcosTable);
    add(container);

    WebMarkupContainer rootForNew = new WebMarkupContainer("NewBarco");
    rootForNew.add(new MyForm());
    add(rootForNew);
  }

  class BarcosModel extends Model {

    public Object getObject() {
      Session session = PersistenceManager.getInstance().getNewSession();
      List<Barco> filteredResult = null;
      try {
        List<Barco> list = session.createCriteria(Barco.class).list();
        if (barcoFilter == null || "".equals(barcoFilter)) {
          return list;
        }
        filteredResult = new ArrayList<Barco>();
        for (Barco o : list) {
          if (o.getName().contains(barcoFilter)) {
            filteredResult.add(o);
          }
        }
      } finally {
        session.close();
      }
      return filteredResult;
    }
  }

  private static class MyForm extends Form {
    private TextField barcoName;
    private TextField barcoSerialNumber;

    public MyForm() {
      super("NewBarco");
      barcoName = new TextField("barcoName", new Model());
      add(barcoName);
      barcoSerialNumber = new TextField("barcoSerialNumber", new Model());
      add(barcoSerialNumber);
    }

    protected void onSubmit() {
      Session session = PersistenceManager.getInstance().getNewSession();
      Transaction transaction = session.beginTransaction();
      Barco barco = new Barco(barcoName.getInput(), barcoSerialNumber.getInput());
      session.save(barco);
      transaction.commit();
      session.close();
    }
  }
}
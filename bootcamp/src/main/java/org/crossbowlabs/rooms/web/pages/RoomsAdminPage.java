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
import org.globsframework.rooms.model.PersistenceManager;
import org.globsframework.rooms.model.Room;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;


public class RoomsAdminPage extends Skin {
  private String roomFilter;
  private TextField roomNameFilter;
  private WebMarkupContainer roomsTable;
 
  public RoomsAdminPage() {

    roomNameFilter = new TextField("RoomNameFilter", new Model());
    roomNameFilter.add(new OnChangeAjaxBehavior() {

      protected void onUpdate(AjaxRequestTarget ajaxRequestTarget) {
        roomFilter = roomNameFilter.getModelObjectAsString();
        ajaxRequestTarget.addComponent(roomsTable);
      }
    });

    roomsTable = new WebMarkupContainer("RoomsTable");

    ListView listView = new ListView("ShowRooms", new RoomsModel()) {
      protected void populateItem(ListItem listItem) {
        Room room = (Room) listItem.getModelObject();
        final Integer roomId = room.getId();
        listItem.add(new Label("name", room.getName()));
        listItem.add(new Label("size", room.getSize().toString()));
        listItem.add(new Link("remove") {
          public void onClick() {
            Session session = PersistenceManager.getInstance().getNewSession();
            Transaction transaction = session.beginTransaction();
            session.delete(session.get(Room.class, roomId));
            transaction.commit();
            session.close();
          }
        });
      }
    };
    roomsTable.add(listView);
    roomsTable.setOutputMarkupId(true);

    WebMarkupContainer container = new WebMarkupContainer("RoomsDiv");
    container.add(roomNameFilter);
    container.add(roomsTable);
    add(container);
    
    WebMarkupContainer rootForNew = new WebMarkupContainer("NewRoom");
    rootForNew.add(new MyForm());
    add(rootForNew);
  }

  class RoomsModel extends Model {

    public Object getObject() {
      Session session = PersistenceManager.getInstance().getNewSession();
      List<Room> filteredResult = null;
      try {
        List<Room> list = session.createCriteria(Room.class).list();
        if (roomFilter == null || "".equals(roomFilter)) {
          return list;
        }
        filteredResult = new ArrayList<Room>();
        for (Room o : list) {
          if (o.getName().contains(roomFilter)) {
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
    private TextField roomName;
    private TextField roomSize;

    public MyForm() {
      super("NewRoom");
      roomName = new TextField("roomName", new Model());
      add(roomName);
      roomSize = new TextField("roomSize", new Model());
      add(roomSize);
    }

    protected void onSubmit() {
      org.hibernate.Session session = PersistenceManager.getInstance().getNewSession();
      Transaction transaction = session.beginTransaction();
      Room room = new Room(roomName.getInput(), Integer.parseInt(roomSize.getInput()));
      session.save(room);
      transaction.commit();
      session.close();
    }
  }
}

package org.crossbowlabs.rooms.web.pages;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.crossbowlabs.rooms.model.*;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import static org.hibernate.criterion.Restrictions.*;

import java.util.*;

public class RoomsPage extends Skin {
  private TextField roomNameFilter;
  private WebMarkupContainer roomsTable;
  public String roomFilter;
  private WebMarkupContainer table;
  private boolean withBarco;

  public RoomsPage() {
    initHeader();

    roomNameFilter = new TextField("RoomNameFilter", new Model());
    roomNameFilter.add(new OnChangeAjaxBehavior() {

      protected void onUpdate(AjaxRequestTarget ajaxRequestTarget) {
        roomFilter = roomNameFilter.getModelObjectAsString();
        ajaxRequestTarget.addComponent(roomsTable);
        ajaxRequestTarget.addComponent(table);

      }
    });

    roomsTable = new WebMarkupContainer("RoomsTable");

    ListView showRooms = new ListView("ShowRooms", new RoomsModel()) {
      protected void populateItem(ListItem listItem) {
        Room room = (Room) listItem.getModelObject();
        final String roomName = room.getName();
        Link linkToRoom = new Link("linkToRoom") {

          public void onClick() {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            int beginMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 +
                               (calendar.get(Calendar.MINUTE) / 15) * 15;
            calendar.setTime(new Date());
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            setResponsePage(new RoomPage(roomName,
                                         calendar.getTime(),
                                         beginMinutes, beginMinutes + 60));
          }
        };
        listItem.add(linkToRoom);
        linkToRoom.add(new Label("roomName", roomName));
        listItem.add(new Label("size", room.getSize().toString()));
      }
    };
    roomsTable.add(showRooms);
    roomsTable.setOutputMarkupId(true);

    WebMarkupContainer container = new WebMarkupContainer("RoomsList");
    container.add(roomNameFilter);
    container.add(roomsTable);
    add(container);


    WebMarkupContainer rootDiv = new WebMarkupContainer("RoomsDiv");
    DateModel model = new DateModel();
    ListView listView = new ListView("dateId", model) {

      protected void populateItem(ListItem item) {
        String date = (String) item.getModelObject();
        item.add(new Label("dateName", date));
      }
    };
    table = buildPath(listView, "RoomMatriceTable", "thread", "tr");
    table.setOutputMarkupId(true);
    rootDiv.add(table);
    table.add(buildPath(new rowListView(model), "tbody"));
    add(rootDiv);
  }

  private void initHeader() {
    WebMarkupContainer criteria = new WebMarkupContainer("criteria");
    add(criteria);
    criteria.add(new AjaxCheckBox("barco", new Model()) {
      protected void onUpdate(AjaxRequestTarget target) {
        withBarco = getModelObject() != null && (Boolean) getModelObject();
        target.addComponent(table);
        target.addComponent(roomsTable);
      }
    });
    ResourceBundle resourceBundle = ResourceBundle.getBundle("translation/rooms");
    criteria.add(new Label("labelBarco", resourceBundle.getString("barco")));
  }

  private WebMarkupContainer buildPath(ListView listView, String... path) {
    WebMarkupContainer root = new WebMarkupContainer(path[0]);
    WebMarkupContainer tmp = root;
    for (int i = 1; i < path.length; i++) {
      WebMarkupContainer child = new WebMarkupContainer(path[i]);
      tmp.add(child);
      tmp = child;
    }
    tmp.add(listView);
    return root;
  }

  private class rowListView extends ListView {
    private DateModel model;

    public rowListView(DateModel model) {
      super("rowId", new TimeRoomModel());
      this.model = model;
    }

    protected void populateItem(ListItem item) {
      TimeRoomModel.TimePeriod timePeriod =
        (TimeRoomModel.TimePeriod) item.getModelObject();
      WebMarkupContainer timeId = new WebMarkupContainer("timeId");
      timeId.add(new Label("TimeDuration", timePeriod.toString()));
      item.add(timeId);
      item.add(new RoomsListView(timePeriod, model));
    }

    private class RoomsListView extends ListView {
      private TimeRoomModel.TimePeriod time;

      public RoomsListView(final TimeRoomModel.TimePeriod time, DateModel model) {
        super("rooms");
        setModel(new RoomsListModel(model));
        this.time = time;
      }

      protected void populateItem(final ListItem dateItem) {
        dateItem.add(new ListView("ListOfRooms", new IModel() {
          public Object getObject() {
            return getFreeRooms(((Date) dateItem.getModelObject()));
          }

          public void setObject(Object object) {
          }

          public void detach() {
          }
        }) {
          protected void populateItem(final ListItem roomItem) {
            Link child = new Link("linkToRoom") {
              public void onClick() {
                setResponsePage(new RoomPage(roomItem.getModelObjectAsString(),
                                             ((Date) dateItem.getModelObject()),
                                             time.beginInMinutes, time.endInMinutes));
              }
            };
            roomItem.add(child);
            child.add(new Label("RoomsName", roomItem.getModelObjectAsString()));
          }
        });
      }

      private List<String> getFreeRooms(Date dateDay) {
        Session session = PersistenceManager.getInstance().getNewSession();
        Transaction transaction = session.beginTransaction();
        List<String> freeRooms = getFreeRooms(dateDay, session);

        if (withBarco) {
          Criteria criteria = session.createCriteria(UseDateForBarco.class);
          criteria.add(
            and(eq("dateInDay", dateDay),
                or(
                  or(and(isNull("fromInMinutes"), gt("toInMinutes", time.beginInMinutes)),
                     and(isNull("toInMinutes"), lt("fromInMinutes", time.endInMinutes))),
                  and(gt("toInMinutes", time.beginInMinutes), lt("fromInMinutes", time.endInMinutes)))));
          List<UseDateForBarco> usedBarco = criteria.list();
          List<Barco> barcos = session.createCriteria(Barco.class).list();
          if (barcos.size() <= usedBarco.size()) {
            freeRooms.clear();
          }
        }
        transaction.commit();
        session.close();
        return freeRooms;
      }

      private List<String> getFreeRooms(Date dateDay, Session session) {
        Criteria criteria = session.createCriteria(UseDateByRoom.class);
        criteria.add(
          and(eq("dateInDay", dateDay),
              or(
                or(and(isNull("fromInMinutes"), gt("toInMinutes", time.beginInMinutes)),
                   and(isNull("toInMinutes"), lt("fromInMinutes", time.endInMinutes))),
                and(gt("toInMinutes", time.beginInMinutes), lt("fromInMinutes", time.endInMinutes)))));
        List<Room> rooms = session.createCriteria(Room.class).list();
        List<UseDateByRoom> c = criteria.list();
        Set<Integer> usedRooms = new HashSet<Integer>();
        for (UseDateByRoom useDateByRoom : c) {
          usedRooms.add(useDateByRoom.getRoom().getId());
        }
        List<String> freeRooms = new ArrayList<String>();
        for (Room room : rooms) {
          if (!usedRooms.contains(room.getId())) {
            if (filterRooms(dateDay, time, room)) {
              freeRooms.add(room.getName());
            }
          }
        }
        return freeRooms;
      }

      private class RoomsListModel implements IModel {
        private final DateModel model;

        public RoomsListModel(DateModel model) {
          this.model = model;
        }

        public Object getObject() {
          return model.getDates();
        }

        public void setObject(Object object) {
        }

        public void detach() {
        }
      }
    }
  }

  class RoomsModel extends Model {

    public Object getObject() {
      Session session = PersistenceManager.getInstance().getNewSession();
      List<Room> filteredResult = null;
      try {
        List<Room> list = session.createCriteria(Room.class).list();
        filteredResult = new ArrayList<Room>();
        for (Room o : list) {
          if (filterRooms(null, null, o)) {
            filteredResult.add(o);
          }
        }
      } finally {
        session.close();
      }
      return filteredResult;
    }
  }

  private boolean filterRooms(Date date, TimeRoomModel.TimePeriod time, Room room) {
    if (roomFilter != null && !"".equals(roomFilter) && !room.getName().contains(roomFilter)) {
      return false;
    }
    if (withBarco && !room.getCanHaveBarco()) {
      return false;
    }
    return true;
  }

}

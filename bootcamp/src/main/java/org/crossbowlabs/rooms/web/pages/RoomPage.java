package org.crossbowlabs.rooms.web.pages;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.crossbowlabs.rooms.model.PersistenceManager;
import org.crossbowlabs.rooms.model.Room;
import org.crossbowlabs.rooms.model.UseDateByRoom;
import org.crossbowlabs.rooms.model.User;
import org.crossbowlabs.rooms.web.RoomSession;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import static org.hibernate.criterion.Restrictions.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;

public class RoomPage extends Skin {
  static SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm");
  private Model fromModel;
  private Model toModel;
  private String roomName;

  public RoomPage(final String roomName, final Date date, int beginInMinutes, int endInMinutes) {
    this.roomName = roomName;
    ResourceBundle messages = ResourceBundle.getBundle("translation/rooms");
    WebMarkupContainer roomNameDiv = new WebMarkupContainer("roomName");
    roomNameDiv.add(new Label("roomName", roomName));
    add(roomNameDiv);

    WebMarkupContainer hourChange = new WebMarkupContainer("hoursChange");
    WebMarkupContainer dateDiv = new WebMarkupContainer("date");
    hourChange.add(dateDiv);
    dateDiv.add(new Label("date", new SimpleDateFormat(messages.getString("LongFullDateFormat")).format(date)));
    add(hourChange);
    initFrom(hourChange, date, beginInMinutes);
    initTo(hourChange, date, endInMinutes);
    initSave(hourChange, roomName, date, messages);
    initTable();
  }

  private void initTable() {
    WebMarkupContainer tableDiv = new WebMarkupContainer("tableDiv");
    add(tableDiv);
    WebMarkupContainer table = new WebMarkupContainer("table");
    tableDiv.add(table);
    WebMarkupContainer head = new WebMarkupContainer("head");
    table.add(head);
    WebMarkupContainer row = new WebMarkupContainer("row");
    head.add(row);
    DateModel model = new DateModel();
    ListView days = new ListView("days", model) {

      protected void populateItem(ListItem item) {
        String date = (String) item.getModelObject();
        item.add(new Label("dateName", date));
      }
    };
    row.add(days);

    WebMarkupContainer body = new WebMarkupContainer("body");
    WebMarkupContainer bodyRow = new RowView(roomName, model);
    body.add(bodyRow);
    table.add(body);
  }

  private void initSave(WebMarkupContainer hourChange, final String roomName, final Date date, final ResourceBundle messages) {
    WebMarkupContainer saveDiv = new WebMarkupContainer("save");
    hourChange.add(saveDiv);
    Form form = new Form("form");
    saveDiv.add(form);
    form.add(new Button("save", new Model(messages.getString("rooms.save"))) {
      public void onSubmit() {
        Session session = PersistenceManager.getInstance().getNewSession();
        Transaction transaction = session.beginTransaction();
        transaction.begin();
        try {
          Room room = (Room) session.createCriteria(Room.class).add(Restrictions.eq("name", roomName)).uniqueResult();
          User user = ((RoomSession) this.getSession()).getContext().getUser();
          UseDateByRoom dateByRoom = new UseDateByRoom(user, room, date);
          try {
            String fromInString = (String) fromModel.getObject();
            Date fromDate = hourFormat.parse(fromInString);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(fromDate);
            dateByRoom.setFromInMinutes(calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE));
          } catch (ParseException e) {
            e.printStackTrace();
          }
          try {
            String toInString = (String) toModel.getObject();
            Date toDate = hourFormat.parse(toInString);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(toDate);
            dateByRoom.setToInMinutes(calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE));
          } catch (ParseException e) {
            e.printStackTrace();
          }
          session.save(dateByRoom);
          setResponsePage(RoomsPage.class);
        } finally {
          transaction.commit();
          session.close();
        }
      }
    });
  }

  private void initFrom(WebMarkupContainer hourChange, Date date, int beginInMinutes) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.add(Calendar.MINUTE, beginInMinutes);
    String fromDate = hourFormat.format(cal.getTime());
    ResourceBundle messages = ResourceBundle.getBundle("translation/rooms");
    WebMarkupContainer from = new WebMarkupContainer("from");
    Form fromParamater = new Form("parameter");
    from.add(fromParamater);
    hourChange.add(from);
    fromModel = new Model(fromDate);
    final TextField fromText = new TextField("from", fromModel);
    fromText.setOutputMarkupId(true);
    fromParamater.add(fromText);
    fromParamater.add(new Label("tr.from", messages.getString("rooms.from")));
    fromParamater.add(new AjaxLink("increase") {

      public void onClick(AjaxRequestTarget target) {
        String objectAsString = fromText.getModelObjectAsString();
        try {
          Date date = hourFormat.parse(objectAsString);
          Calendar calendar = Calendar.getInstance();
          calendar.setTime(date);
          calendar.add(Calendar.MINUTE, 15);
          fromModel.setObject(hourFormat.format(calendar.getTime()));
        } catch (ParseException e) {
        }
        target.addComponent(fromText);
      }
    });

    fromParamater.add(new AjaxLink("decrease") {

      public void onClick(AjaxRequestTarget target) {
        String objectAsString = fromText.getModelObjectAsString();
        try {
          Date date = hourFormat.parse(objectAsString);
          Calendar calendar = Calendar.getInstance();
          calendar.setTime(date);
          calendar.add(Calendar.MINUTE, -15);
          fromModel.setObject(hourFormat.format(calendar.getTime()));
        } catch (ParseException e) {
        }
        target.addComponent(fromText);
      }
    });
  }

  private void initTo(WebMarkupContainer hourChange, Date date, int endInMinutes) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.add(Calendar.MINUTE, endInMinutes);
    String toDate = hourFormat.format(cal.getTime());
    ResourceBundle messages = ResourceBundle.getBundle("translation/rooms");
    WebMarkupContainer from = new WebMarkupContainer("to");
    Form fromParamater = new Form("parameter");
    from.add(fromParamater);
    hourChange.add(from);
    toModel = new Model(toDate);
    final TextField fromText = new TextField("to", toModel);
    fromText.setOutputMarkupId(true);
    fromParamater.add(fromText);
    fromParamater.add(new Label("tr.to", messages.getString("rooms.to")));
    fromParamater.add(new AjaxLink("increase") {

      public void onClick(AjaxRequestTarget target) {
        String objectAsString = fromText.getModelObjectAsString();
        try {
          Date date = hourFormat.parse(objectAsString);
          Calendar calendar = Calendar.getInstance();
          calendar.setTime(date);
          calendar.add(Calendar.MINUTE, 15);
          toModel.setObject(hourFormat.format(calendar.getTime()));
        } catch (ParseException e) {
        }
        target.addComponent(fromText);
      }
    });

    fromParamater.add(new AjaxLink("decrease") {

      public void onClick(AjaxRequestTarget target) {
        String objectAsString = fromText.getModelObjectAsString();
        try {
          Date date = hourFormat.parse(objectAsString);
          Calendar calendar = Calendar.getInstance();
          calendar.setTime(date);
          calendar.add(Calendar.MINUTE, -15);
          toModel.setObject(hourFormat.format(calendar.getTime()));
        } catch (ParseException e) {
        }
        target.addComponent(fromText);
      }
    });
  }

  private static class RowView extends ListView {
    private String roomName;
    private DateModel dateModel;

    public RowView(String roomName, DateModel dateModel) {
      super("row", new TimeRoomModel());
      this.roomName = roomName;
      this.dateModel = dateModel;
    }

    protected void populateItem(ListItem item) {
      final TimeRoomModel.TimePeriod timePeriod =
        (TimeRoomModel.TimePeriod) item.getModelObject();
      WebMarkupContainer timeId = new WebMarkupContainer("timeId");
      timeId.add(new Label("durationId", timePeriod.toString()));
      item.add(timeId);
      ListView td = new ListView("isFree", dateModel.getDates()) {

        protected void populateItem(ListItem item) {
          Date day = (Date) item.getModelObject();

          Session session = PersistenceManager.getInstance().getNewSession();
          Transaction transaction = session.beginTransaction();
          Criteria criteria = session.createCriteria(UseDateByRoom.class);
          criteria.createAlias("room", "theRoom");
          criteria.add(
            and(eq("theRoom.name", roomName),
                and(eq("dateInDay", day),
                    or(
                      or(and(isNull("fromInMinutes"), gt("toInMinutes", timePeriod.beginInMinutes)),
                         and(isNull("toInMinutes"), lt("fromInMinutes", timePeriod.endInMinutes))),
                      and(gt("toInMinutes", timePeriod.beginInMinutes), lt("fromInMinutes", timePeriod.endInMinutes))))));
          UseDateByRoom c = (UseDateByRoom) criteria.uniqueResult();
          if (c == null) {
            item.add(new AttributeModifier("class", new Model("roomStateFree")));
          }
          else {
            item.add(new AttributeModifier("class", new Model("roomStateNotFree")));
          }
          transaction.commit();
          session.close();
        }
      };
      item.add(td);
    }
  }
}

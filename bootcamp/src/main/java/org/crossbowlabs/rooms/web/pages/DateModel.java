package org.crossbowlabs.rooms.web.pages;

import org.apache.wicket.model.IModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;

public class DateModel implements IModel {
  private DateFormat dateFormat = new SimpleDateFormat("EEEE d");
  private Date beginDate = new Date();
  private int dayCount = 7;


  public List<Date> getDates() {
    List<Date> dates = new ArrayList<Date>();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(beginDate);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    for (int i = 0; i < dayCount; i++) {
      dates.add(calendar.getTime());
      calendar.add(Calendar.DAY_OF_WEEK, 1);
    }
    return dates;
  }

  public Object getObject() {
    List<String> stringifiedDate = new ArrayList<String>();
    List<Date> dates = getDates();
    for (Date date : dates) {
      stringifiedDate.add(dateFormat.format(date));
    }
    return stringifiedDate;
  }

  public void setObject(Object object) {
  }

  public void detach() {
  }
}

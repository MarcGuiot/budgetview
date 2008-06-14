package org.globsframework.rooms.web.pages;

import org.apache.wicket.model.IModel;

import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.io.Serializable;
import java.text.SimpleDateFormat;

public class TimeRoomModel implements IModel {
  List<TimePeriod> timePeriodList = new ArrayList<TimePeriod>();
  private static final int INTERVALLE = 60;

  public TimeRoomModel() {
    for (int i = 7 * INTERVALLE; i < 20 * INTERVALLE; i += INTERVALLE) {
      timePeriodList.add(new TimePeriod(i, INTERVALLE));
    }
  }

  static class TimePeriod implements Serializable {
    int beginInMinutes;
    int endInMinutes;

    public TimePeriod(int i, int intervalle) {
      beginInMinutes = i;
      endInMinutes = i + intervalle;
    }

    public String toString() {
      Calendar instance = Calendar.getInstance();
      instance.setTimeInMillis(0);
      gotoDate(instance, beginInMinutes);
      SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
      String s1 = dateFormat.format(instance.getTime());
      instance.setTimeInMillis(0);
      gotoDate(instance, endInMinutes);
      String s2 = dateFormat.format(instance.getTime());
      return s1 + "-" + s2;
    }

    private void gotoDate(Calendar instance, int minutes) {
      int hour = minutes / 60;
      instance.set(Calendar.HOUR_OF_DAY, hour);
      instance.set(Calendar.MINUTE, minutes - hour * 60);
    }
  }

  public Object getObject() {
    return timePeriodList;
  }

  public void setObject(Object object) {
  }

  public void detach() {
  }
}

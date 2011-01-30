package org.designup.picsou.gui.model;

import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Glob;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobBuilder;

public class SeriesShape {
  public static GlobType TYPE;

  @Target(Series.class)
  @org.globsframework.metamodel.annotations.Key
  public static LinkField SERIES_ID;
//attention PERCENT_1 doit etre le field numero 1...
  public static IntegerField PERCENT_1; // 1->4     1->5    1->6
  public static IntegerField PERCENT_2; // 5->8     6->10   7->12
  public static IntegerField PERCENT_3; // 9->12   11->15  13->18
  public static IntegerField PERCENT_4; //13->16   16->20  19->24
  public static IntegerField PERCENT_5; //17->20   21->25  25->31
  public static IntegerField PERCENT_6; //21->24   26->31
  public static IntegerField PERCENT_7; //25->31
  public static DoubleField TOTAL;

  static {
    GlobTypeLoader.init(SeriesShape.class);
  }

  public static IntegerField getField(int period) {
    switch (period) {
      case 1:
        return PERCENT_1;
      case 2:
        return PERCENT_2;
      case 3:
        return PERCENT_3;
      case 4:
        return PERCENT_4;
      case 5:
        return PERCENT_5;
      case 6:
        return PERCENT_6;
      case 7:
        return PERCENT_7;
    }
    throw new RuntimeException("not valid period " + period);
  }

  public static int getDay(int periodCount, int period, int monthId, boolean isPositive) {
    int day = 0;
    if (periodCount <= 4) {
      if (isPositive) {
        day = getEnd4(period);
      }
      else {
        day = getBegin4(period);
      }
    }
    else if (periodCount <= 5) {
      if (isPositive) {
        day = getEnd5(period);
      }
      else {
        day = getBegin5(period);
      }
    }
    else if (periodCount == 6) {
      if (isPositive) {
        day = getEnd6(period);
      }
      else {
        day = getBegin6(period);
      }
    }
    else if (periodCount >= 7) {
      if (isPositive) {
        day = getEnd7(period);
      }
      else {
        day = getBegin7(period);
      }
    }

    int lastDay = Month.getLastDayNumber(monthId);
    if (day > lastDay) {
      return lastDay;
    }
    return day;
  }

  public static int getBegin(int periodCount, int period) {
    if (periodCount <= 5) {
      return getBegin5(period);
    }
    if (periodCount == 6) {
      return getBegin6(period);
    }
    if (periodCount >= 7) {
      return getBegin7(period);
    }
    return 0;
  }

  public static int getEnd(int periodCount, int period) {
    if (periodCount <= 5) {
      return getEnd5(period);
    }
    if (periodCount == 6) {
      return getEnd6(period);
    }
    if (periodCount >= 7) {
      return getEnd7(period);
    }
    return 0;
  }

  private static int getBegin7(int period) {
    switch (period) {
      case 1:
        return 1;
      case 2:
        return 5;
      case 3:
        return 9;
      case 4:
        return 13;
      case 5:
        return 17;
      case 6:
        return 21;
      case 7:
        return 29;
      default:
        return 29;
    }
  }

  private static int getEnd7(int period) {
    switch (period) {
      case 1:
        return 4;
      case 2:
        return 8;
      case 3:
        return 12;
      case 4:
        return 16;
      case 5:
        return 20;
      case 6:
        return 28;
      case 7:
        return 31;
      default:
        return 31;
    }
  }

  private static int getBegin6(int period) {
    switch (period) {
      case 1:
        return 1;
      case 2:
        return 6;
      case 3:
        return 11;
      case 4:
        return 16;
      case 5:
        return 21;
      case 6:
        return 26;
      default:
        return 26;
    }
  }

  private static int getEnd6(int period) {
    switch (period) {
      case 1:
        return 5;
      case 2:
        return 10;
      case 3:
        return 15;
      case 4:
        return 20;
      case 5:
        return 25;
      case 6:
        return 31;
      default:
        return 31;
    }
  }

  private static int getBegin5(int period) {
    switch (period) {
      case 1:
        return 1;
      case 2:
        return 7;
      case 3:
        return 13;
      case 4:
        return 19;
      case 5:
        return 25;
      default:
        return 25;
    }
  }

  private static int getEnd5(int period) {
    switch (period) {
      case 1:
        return 6;
      case 2:
        return 12;
      case 3:
        return 18;
      case 4:
        return 24;
      case 5:
        return 31;
      default:
        return 31;
    }
  }

  private static int getBegin4(int period) {
    switch (period) {
      case 1:
        return 1;
      case 2:
        return 8;
      case 3:
        return 16;
      case 4:
        return 24;
      default:
        return 24;
    }
  }

  private static int getEnd4(int period) {
    switch (period) {
      case 1:
        return 7;
      case 2:
        return 15;
      case 3:
        return 23;
      case 4:
        return 31;
      default:
        return 31;
    }
  }

  public static Glob getDefault(Key key, int periodSize) {
    if (periodSize <= 4) {
      return GlobBuilder.init(key, value(PERCENT_2, 100)).get();
    }
    if (periodSize == 5) {
      return GlobBuilder.init(key, value(PERCENT_3, 100)).get();
    }
    if (periodSize == 6) {
      return GlobBuilder.init(key, value(PERCENT_4, 100)).get();

    }
    return GlobBuilder.init(key, value(PERCENT_5, 100)).get();

  }
}

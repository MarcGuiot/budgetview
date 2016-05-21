package com.budgetview.gui.model;

import com.budgetview.model.Month;
import com.budgetview.model.Series;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
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
  public static IntegerField PERCENT_8; //25->31
  public static IntegerField PERCENT_9; //25->31
  public static IntegerField PERCENT_10; //25->31
  public static DoubleField TOTAL;  //=> premier champ apres le PERCENT ne pas changer de place
  public static IntegerField LAST_MONTH;
  public static IntegerField FIXED_DATE;

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
      case 8:
        return PERCENT_8;
      case 9:
        return PERCENT_9;
      case 10:
        return PERCENT_10;
    }
    throw new RuntimeException("not valid period " + period);
  }

  public static int getDay(int periodCount, int period, int monthId) {
    int day = 0;
    if (periodCount <= 4) {
      day = getMiddle4(period);
    }
    else if (periodCount <= 5) {
      day = getMiddle5(period);
    }
    else if (periodCount == 6) {
      day = getMiddle6(period);
    }
    else if (periodCount == 7) {
      day = getMiddle7(period);
    }
    else if (periodCount == 10){
      day = getMiddle10(period);
    }
    return Month.getDay(day, monthId);
  }

  public static int getBegin(int periodCount, int period) {
    if (periodCount <= 4) {
      return getBegin4(period);
    }
    if (periodCount == 5) {
      return getBegin5(period);
    }
    if (periodCount == 6) {
      return getBegin6(period);
    }
    if (periodCount == 7) {
      return getBegin7(period);
    }
    if (periodCount == 10){
      return getBegin10(period);
    }
    return 0;
  }

  public static int getEnd(int periodCount, int period) {
    if (periodCount <= 4) {
      return getEnd4(period);
    }
    if (periodCount == 5) {
      return getEnd5(period);
    }
    if (periodCount == 6) {
      return getEnd6(period);
    }
    if (periodCount == 7) {
      return getEnd7(period);
    }
    if (periodCount == 10){
      return getEnd10(period);
    }
    return 0;
  }

  private static int getBegin10(int period) {
    switch (period) {
      case 1:
        return 1;
      case 2:
        return 3;
      case 3:
        return 6;
      case 4:
        return 10;
      case 5:
        return 14;
      case 6:
        return 18;
      case 7:
        return 21;
      case 8:
        return 24;
      case 9:
        return 27;
      case 10:
        return 29;
      default:
        return 25;
    }
  }

  private static int getMiddle10(int period) {
    switch (period) {
      case 1:
        return 2; // 1 -> 2
      case 2:
        return 4; // 3 -> 5
      case 3:
        return 8; // 6 -> 9
      case 4:
        return 12; // 10 -> 13
      case 5:
        return 15; // 14 -> 17
      case 6:
        return 19; // 18 -> 20
      case 7:
        return 22; // 21 -> 23
      case 8:
        return 25; // 24 -> 26
      case 9:
        return 27; // 27 -> 28
      case 10:
        return 29; // 29 -> 31
      default:
        return 29;
    }
  }

  private static int getEnd10(int period) {
    switch (period) {
      case 1:
        return 2;
      case 2:
        return 5;
      case 3:
        return 9;
      case 4:
        return 13;
      case 5:
        return 17;
      case 6:
        return 20;
      case 7:
        return 23;
      case 8:
        return 26;
      case 9:
        return 28;
      case 10:
        return 31;
      default:
        return 31;
    }
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
        return 25;
      default:
        return 25;
    }
  }

  private static int getMiddle7(int period) {
    switch (period) {
      case 1:
        return 2; // 1 -> 4
      case 2:
        return 6; // 5 -> 8
      case 3:
        return 10; // 9 -> 12
      case 4:
        return 14; // 13 -> 16
      case 5:
        return 18; // 17 -> 20
      case 6:
        return 22; // 21 -> 24
      case 7:
        return 26; // 25 -> 31
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
        return 24;
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

  private static int getMiddle6(int period) {
    switch (period) {
      case 1:
        return 3; // 1 -> 5
      case 2:
        return 8; // 6 -> 10
      case 3:
        return 13; // 11 -> 15
      case 4:
        return 18; // 16 -> 20
      case 5:
        return 23; // 21 -> 25
      case 6:
        return 28; // 26 -> 31
      default:
        return 28;
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

  private static int getMiddle5(int period) {
    switch (period) {
      case 1:
        return 3; // 1 -> 6
      case 2:
        return 9; // 7 -> 12
      case 3:
        return 15; // 13 -> 18
      case 4:
        return 21; // 19 -> 24
      case 5:
        return 27; // 24 -> 31
      default:
        return 27;
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

  private static int getMiddle4(int period) {
    switch (period) {
      case 1:
        return 4; //1->7
      case 2:
        return 11; // 8-> 15
      case 3:
        return 19; //16->23
      case 4:
        return 27; //24->31;
      default:
        return 27;
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

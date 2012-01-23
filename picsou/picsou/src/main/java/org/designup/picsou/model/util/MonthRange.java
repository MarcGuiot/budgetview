package org.designup.picsou.model.util;

import org.designup.picsou.model.Month;
import org.globsframework.utils.collections.Range;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

public class MonthRange extends Range<Integer> {

  public MonthRange(int min, int max) throws InvalidParameterException {
    super(min, max);
  }

  public List<Integer> asList() {
    List<Integer> result = new ArrayList<Integer>();
    for (int month = getMin(); month <= getMax(); month = Month.next(month)) {
      result.add(month);
    }
    return result;
  }
  
  public MonthRange intersection(MonthRange other) {
    int min = (other.getMin() > getMin()) ? other.getMin() : getMin();
    int max = (other.getMax() < getMax()) ? other.getMax() : getMax();
    return new MonthRange(min, max);
  }

  public int length() {
    return Month.distance(getMin(), getMax());
  }
}
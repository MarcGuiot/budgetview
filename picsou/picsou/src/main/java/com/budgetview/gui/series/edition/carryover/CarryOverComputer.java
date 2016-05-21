package com.budgetview.gui.series.edition.carryover;

import com.budgetview.model.Month;
import com.budgetview.shared.utils.Amounts;

import java.util.ArrayList;
import java.util.List;

public class CarryOverComputer {

  private List<CarryOver> result = new ArrayList<CarryOver>();
  private AbstractCarryOver last;

  private final int initialMonth;
  private final double initialPlannned;

  public CarryOverComputer(int month, double actual, double planned) {
    this.initialMonth = month;
    this.initialPlannned = planned;
    this.last = createInitial(month, actual, planned);
    this.result.add(last);
  }

  private AbstractCarryOver createInitial(int month, double actual, double planned) {
    if (planned > 0) {
      if (actual > planned) {
        return new PositiveOverrun(month, 0, actual, 0, actual - planned);
      }
      else {
        return new PositiveRemainder(month, 0, actual, 0, planned - actual);
      }
    }
    else {
      if (actual > planned) {
        return new NegativeRemainder(month, 0, actual, 0, planned - actual);
      }
      else {
        return new NegativeOverdraw(month, 0, actual, 0, actual - planned);
      }
    }
  }

  public boolean next(double nextActual, double nextPlanned) {
    last = last.getNext(nextActual, nextPlanned);
    result.add(last);
    return last.hasNext();
  }

  public List<CarryOver> getResult() {
    return result;
  }

  public boolean hasNext() {
    return last.hasNext();
  }

  public boolean canCarryOver() {
    for (CarryOver carryOver : result) {
      if (carryOver.getCarriedOver() != 0.00) {
        return true;
      }
    }
    return false;
  }

  public abstract class AbstractCarryOver implements CarryOver {

    protected final int month;
    protected final double newPlanned;
    protected final double carriedOver;
    protected final double remainder;
    private double available;

    protected AbstractCarryOver(int month, double available, double newPlanned, double carriedOver, double remainder) {
      this.month = month;
      this.available = available;
      this.newPlanned = newPlanned;
      this.carriedOver = carriedOver;
      this.remainder = remainder;
    }

    public int getMonth() {
      return month;
    }

    public double getAvailable() {
      return available;
    }

    public double getNewPlanned() {
      return newPlanned;
    }

    public double getCarriedOver() {
      return carriedOver;
    }

    public double getRemainder() {
      return remainder;
    }

    public boolean hasNext() {
      return Amounts.isNotZero(remainder);
    }

    public abstract AbstractCarryOver getNext(double nextActual, double nextPlanned);
  }

  public class PositiveOverrun extends AbstractCarryOver {

    private PositiveOverrun(int month, double available, double newPlanned, double carriedOver, double remainder) {
      super(month, available, newPlanned, carriedOver, remainder);
    }

    public AbstractCarryOver getNext(double nextActual, double nextPlanned) {
      double nextAvailable = getAvailable(nextActual, nextPlanned);
      if ((nextAvailable > 0) && (remainder > 0)) {
        if (nextAvailable >= remainder) {
          return createNext(nextAvailable, remainder, nextAvailable - remainder, 0);
        }
        else {
          return createNext(nextAvailable, nextAvailable, nextPlanned - nextAvailable, remainder - nextAvailable);
        }
      }
      return createNext(0, 0, nextPlanned, remainder);
    }

    private double getAvailable(double nextActual, double nextPlanned) {
      if (nextPlanned > 0) {
        return nextActual > 0 ? nextPlanned - nextActual : nextPlanned;
      }
      return 0;
    }

    protected AbstractCarryOver createNext(double available, double carriedOver, double newPlanned, double remainder) {
      return new PositiveOverrun(Month.next(month), available, newPlanned, carriedOver, remainder);
    }
  }

  public class PositiveRemainder extends AbstractCarryOver {

    private PositiveRemainder(int month, double available, double newPlanned, double carriedOver, double remainder) {
      super(month, available, newPlanned, carriedOver, remainder);
    }

    public AbstractCarryOver getNext(double nextActual, double nextPlanned) {
      return createNext(nextPlanned, nextPlanned + remainder, remainder, 0);
    }

    protected AbstractCarryOver createNext(double available, double newPlanned, double carriedOver, double remainder) {
      return new PositiveRemainder(Month.next(month), available, newPlanned, carriedOver, remainder);
    }
  }

  public class NegativeRemainder extends AbstractCarryOver {

    private NegativeRemainder(int month, double available, double newPlanned, double carriedOver, double remainder) {
      super(month, available, newPlanned, carriedOver, remainder);
    }

    public AbstractCarryOver getNext(double nextActual, double nextPlanned) {
      return createNext(nextPlanned, nextPlanned + remainder, remainder, 0);
    }

    protected AbstractCarryOver createNext(double available, double newPlanned, double carriedOver, double remainder) {
      return new NegativeRemainder(Month.next(month), available, newPlanned, carriedOver, remainder);
    }
  }

  public class NegativeOverdraw extends AbstractCarryOver {

    private NegativeOverdraw(int month, double available, double newPlanned, double carriedOver, double remainder) {
      super(month, available, newPlanned, carriedOver, remainder);
    }

    public AbstractCarryOver getNext(double nextActual, double nextPlanned) {
      double nextAvailable = getAvailable(nextActual, nextPlanned);
      if ((nextAvailable < 0) && (remainder < 0)) {
        if (nextAvailable <= remainder) {
          return createNext(nextAvailable, nextPlanned - remainder, remainder, 0);
        }
        else {
          return createNext(nextAvailable, nextPlanned - nextAvailable, nextAvailable, remainder - nextAvailable);
        }
      }
      return createNext(0, nextPlanned, 0, remainder);
    }

    private double getAvailable(double nextActual, double nextPlanned) {
      if (nextPlanned < 0) {
        return nextActual < 0 ? nextPlanned - nextActual : nextPlanned;
      }
      return 0;
    }

    protected AbstractCarryOver createNext(double available, double newPlanned, double carriedOver, double remainder) {
      return new NegativeOverdraw(Month.next(month), available, newPlanned, carriedOver, remainder);
    }
  }

  public List<CarryOver> forceSingleMonth(double nextMonthActual, double nextMonthPlanned) {
    result.clear();
    double nextMonthAvailable = getNextAvailable(nextMonthActual, nextMonthPlanned);
    last = new ForceSingleMonth(initialMonth, 0, initialPlannned + nextMonthAvailable, 0, 0);
    result.add(last);
    last = last.getNext(nextMonthActual, nextMonthPlanned);
    result.add(last);
    return result;
  }

  private double getNextAvailable(double nextActual, double nextPlanned) {
    return Amounts.isSameSign(nextActual, nextPlanned) ? nextPlanned - nextActual : nextPlanned;
  }

  public class ForceSingleMonth extends AbstractCarryOver {

    private ForceSingleMonth(int month, double available, double newPlanned, double carriedOver, double remainder) {
      super(month, available, newPlanned, carriedOver, remainder);
    }

    public AbstractCarryOver getNext(double nextActual, double nextPlanned) {
      double nextAvailable = getNextAvailable(nextActual, nextPlanned);
      return createNext(nextAvailable, nextPlanned - nextAvailable, nextAvailable, 0);
    }

    protected ForceSingleMonth createNext(double available, double newPlanned, double carriedOver, double remainder) {
      return new ForceSingleMonth(Month.next(month), available, newPlanned, carriedOver, remainder);
    }
  }
}

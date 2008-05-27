package org.prevayler;

public class RWLock {
  Turn lastTurn = new Turn(null, State.NO_PROCESSING);

  public interface Action {
    Object run() throws Exception;
  }

  public RWLock() {
  }

  public Object excecuteRead(Action action) throws Exception {
    Turn myTurn;
    synchronized (this) {
      lastTurn = lastTurn.addRead(action);
      myTurn = lastTurn;
    }
    return myTurn.elect(action);
  }

  public Object excecuteWrite(Action action) throws Exception {
    Turn myTurn;
    synchronized (this) {
      lastTurn = lastTurn.addWrite(action);
      myTurn = lastTurn;
    }
    return myTurn.elect(action);
  }

  private final static class Turn {
    Turn previous;
    State currentState;
    public int rwCount;

    public Turn(Turn previous, State state) {
      this.previous = previous;
      currentState = state;
    }

    final Turn addWrite(Action action) {
      synchronized (this) {                  // turn can be this so rwCount++ is protected or new
        Turn turn = currentState.addWrite(this, action);
        turn.rwCount++;
        return turn;
      }
    }

    final Turn addRead(Action action) {
      synchronized (this) {
        Turn turn = currentState.addRead(this, action);
        turn.rwCount++;
        return turn;
      }
    }

    final Object elect(Action action) throws Exception {
      synchronized (previous) {
        while (previous.currentState != State.NO_PROCESSING) {
          try {
            previous.wait();
          } catch (InterruptedException e) {
          }
        }
      }
      currentState = currentState.getRunningState();
      Object result;
      try {
        result = action.run();
      } finally {
        synchronized (this) {
          rwCount--;
          if (rwCount == 0) {
            currentState = State.NO_PROCESSING;
            notifyAll();
            previous = null;
          }
        }
      }
      return result;
    }
  }

  private static interface State {
    static final NoProcessing NO_PROCESSING = new NoProcessing();
    static final WriteTurnWanted WRITE_TURN_WANTED = new WriteTurnWanted();
    static final WriteTurnRunning WRITE_TURN_RUNNING = new WriteTurnRunning();
    static final ReadTurnRunning READ_TURN_RUNNING = new ReadTurnRunning();
    static final ReadTurnWanted READ_TURN_WANTED = new ReadTurnWanted();

    Turn addWrite(Turn turn, Action action);

    Turn addRead(Turn turn, Action action);

    State getRunningState();
  }

  private final static class NoProcessing implements State {

    final public Turn addWrite(Turn turn, Action action) {
      return new Turn(turn, State.WRITE_TURN_RUNNING);
    }

    final public Turn addRead(Turn turn, Action action) {
      return new Turn(turn, State.READ_TURN_RUNNING);
    }

    final public State getRunningState() {
      return this;
    }
  }

  private final static class WriteTurnWanted implements State {

    final public Turn addWrite(Turn turn, Action action) {
      return new Turn(turn, State.WRITE_TURN_WANTED);
    }

    final public Turn addRead(Turn turn, Action action) {
      return new Turn(turn, State.READ_TURN_WANTED);
    }

    final public State getRunningState() {
      return State.WRITE_TURN_RUNNING;
    }
  }

  private final static class WriteTurnRunning implements State {

    final public Turn addWrite(Turn turn, Action action) {
      return new Turn(turn, State.WRITE_TURN_WANTED);
    }

    final public Turn addRead(Turn turn, Action action) {
      return new Turn(turn, State.READ_TURN_WANTED);
    }

    final public State getRunningState() {
      return this;
    }
  }

  private final static class ReadTurnWanted implements State {

    final public Turn addWrite(Turn turn, Action action) {
      return new Turn(turn, State.WRITE_TURN_WANTED);
    }

    final public Turn addRead(Turn turn, Action action) {
      return turn;
    }

    final public State getRunningState() {
      return State.READ_TURN_RUNNING;
    }
  }

  private final static class ReadTurnRunning implements State {

    final public Turn addWrite(Turn turn, Action action) {
      return new Turn(turn, State.WRITE_TURN_WANTED);
    }

    final public Turn addRead(Turn turn, Action action) {
      return turn;
    }

    final public State getRunningState() {
      return this;
    }
  }
}

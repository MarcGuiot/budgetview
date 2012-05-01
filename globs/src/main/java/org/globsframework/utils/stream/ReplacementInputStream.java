package org.globsframework.utils.stream;

import java.io.IOException;
import java.io.InputStream;

public class ReplacementInputStream extends InputStream {
  private static final EOFCharAccessor EOF_CHAR_ACCESSOR = new EOFCharAccessor();
  private InputStream stream;
  private Node root;
  private CharAccessor currentCharAccessor;
  private StartCharAccessor startCharAccessor;
  private MultiCharAccessor multiCharAccessor;
  private PathCharAccessor pathCharAccessor;
  private PathWithLookAHeadCharAccessor pathWithLookAHeadCharAccessor;


  public ReplacementInputStream(InputStream stream, Node root, int maxSize) {
    this.stream = stream;
    this.root = root;
    DefaultState state = new DefaultState(maxSize, root);
    startCharAccessor = new StartCharAccessor();
    startCharAccessor.setState(state);
    currentCharAccessor = startCharAccessor;
    multiCharAccessor = new MultiCharAccessor();
    pathCharAccessor = new PathCharAccessor();
    pathWithLookAHeadCharAccessor = new PathWithLookAHeadCharAccessor();
  }


  interface CharAccessor {
    int read() throws IOException;

    CharAccessor previous();
  }

  public int read() throws IOException {
    int read = currentCharAccessor.read();
    while (read == -1) {
      currentCharAccessor = currentCharAccessor.previous();
      if (currentCharAccessor == EOF_CHAR_ACCESSOR) {
        return -1;
      }
      read = currentCharAccessor.read();
    }
    return read;
  }

  static public class EOFCharAccessor implements CharAccessor {

    public int read() throws IOException {
      return -1;
    }

    public CharAccessor previous() {
      return this;
    }
  }

  public class StartCharAccessor implements CharAccessor {
    private DefaultState state;
    private CharAccessor accessor;

    public void setState(DefaultState state) {
      this.state = state;
    }

    public int read() throws IOException {
      int read = stream.read();
      if (read == -1) {
        accessor = EOF_CHAR_ACCESSOR;
        return -1;
      }
      DefaultState.Stat next = state.nextWithStat(read);
      if (next == DefaultState.Stat.NotFound) {
        state = state.free();
        return read;
      }
      multiCharAccessor.setState(state);
      accessor = multiCharAccessor;
      return -1;
    }

    public CharAccessor previous() {
      return accessor;
    }
  }

  private class MultiCharAccessor implements CharAccessor {
    DefaultState state;
    CharAccessor previous = EOF_CHAR_ACCESSOR;

    public MultiCharAccessor() {
    }

    public void setState(DefaultState state) {
      this.state = state;
      previous = EOF_CHAR_ACCESSOR;
    }

    public int read() throws IOException {
      if (state.getStat() == DefaultState.Stat.NeedMore) {
        int read = stream.read();
        while (state.nextWithStat(read) == DefaultState.Stat.NeedMore) {
          read = stream.read();
        }
      }
      if (state.getStat() == DefaultState.Stat.NotFound) {
        int ch = state.getFirstCh();
        state = state.free();
        return ch;
      }
      if (state.getStat() == DefaultState.Stat.FullyComplete) {
        Node complete = state.getComplete();
        DefaultState tmp = state;
        for (int i = 0; i < complete.getMatchingLength(); i++) {
          tmp = tmp.free();
        }
        if (tmp.root == tmp.current) {
          pathCharAccessor.setPath(complete.getReplacement(), state);
          previous = pathCharAccessor;
        }
        else {
          pathWithLookAHeadCharAccessor.setPath(complete.getReplacement(), tmp);
          previous = pathWithLookAHeadCharAccessor;
        }
        return -1;
      }
      throw new RuntimeException("Bug");
    }

    public CharAccessor previous() {
      CharAccessor accessor = previous;
      previous = null;
      return accessor;
    }
  }

  private class PathCharAccessor implements CharAccessor {
    int index = 0;
    private DefaultState state;
    private int[] path;


    public void setPath(int[] path, DefaultState state) {
      this.path = path;
      this.state = state;
      index = 0;
    }

    public int read() throws IOException {
      return index >= path.length ? -1 : path[index++];
    }

    public CharAccessor previous() {
      startCharAccessor.setState(state);
      return startCharAccessor;
    }

    public String toString() {
      return "PathCharAccessor{" +
             "index=" + index +
             ", path=" + convert(path) +
             '}';
    }
  }

  private class PathWithLookAHeadCharAccessor implements CharAccessor {
    int index = 0;
    private int[] path;
    private DefaultState defaultState;

    public int read() throws IOException {
      return index >= path.length ? -1 : path[index++];
    }

    public void setPath(int[] path, DefaultState defaultState) {
      this.path = path;
      this.defaultState = defaultState;
      index = 0;
    }

    public CharAccessor previous() {
      multiCharAccessor.setState(defaultState);
      return multiCharAccessor;
    }

    public String toString() {
      return "PathCharAccessor{" +
             "index=" + index +
             ", path=" + convert(path) +
             '}';
    }
  }

  static private class DefaultState implements State {
    DefaultState next;
    DefaultState cached;
    int firstCh;
    Node current;
    int completeCount;
    Node[] complete;
    private final Node root;

    public DefaultState(int maxSize, Node root) {
      this.root = root;
      this.current = this.root;
      complete = new Node[maxSize];
      completeCount = 0;
      cached = new DefaultState(maxSize, root, maxSize, this);
    }

    private DefaultState(int maxSize, Node root, int currentIndex, DefaultState first) {
      this.root = root;
      this.current = this.root;
      complete = new Node[maxSize];
      completeCount = 0;
      if (currentIndex != 0) {
        cached = new DefaultState(maxSize, root, currentIndex - 1, first);
      }
      else {
        cached = first;
      }
    }

    public void complete(Node level) {
      complete[completeCount++] = level;
    }

    public void next(int read){
      if (current != null) {
        current = current.next(read, this);
      }
      if (next == null) {
        next = cached;
        firstCh = read;
      }
      else {
        next.next(read);
      }
    }

    public Stat nextWithStat(int read) {
      next(read);
      return getStat();
    }

    public int getFirstCh() {
      return firstCh;
    }

    public DefaultState free() {
      DefaultState tmp = next;
      next = null;
      current = root;
      completeCount = 0;
      return tmp;
    }

    private Stat getStat() {
      if (current == null) {
        if (completeCount != 0) {
          return Stat.FullyComplete;
        }
        else {
          return Stat.NotFound;
        }
      }
      return Stat.NeedMore;
    }

    public Node getComplete() {
      return complete[completeCount - 1];
    }

    public static enum Stat {
      FullyComplete,
      NotFound,
      NeedMore
    }
  }

  static private String convert(int[] path) {
    byte[] bytes = new byte[path.length];
    for (int i = 0; i < bytes.length; i++) {
      bytes[i] = (byte)path[i];
    }
    return new String(bytes);
  }
}

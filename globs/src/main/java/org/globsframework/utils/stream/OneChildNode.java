package org.globsframework.utils.stream;

public class OneChildNode implements Node {
  private int[] path;
  private final int ch;
  private final Node node;

  public OneChildNode(int[] path, int ch, Node node) {
    this.path = path;
    this.ch = ch;
    this.node = node;
  }

  public Node next(int next, State state) {
    if (next == ch) {
      return node.react(state);
    }
    return null;
  }

  public Node react(State s) {
    return this;
  }

  public int getMatchingLength() {
    return path.length;
  }

  public int[] getPath() {
    return path;
  }

  public int[] getReplacement() {
    throw new RuntimeException("Not terminal");
  }

  public String toString() {
    return "OneChildNode{" +
           "value=" + path +
           '}';
  }
}

package org.globsframework.utils.stream;

public class Terminal implements Node {
  private int[] path;
  private int[] replacement;

  Terminal(int[] path, int[] replacement) {
    this.path = path;
    this.replacement = replacement;
  }

  public Node next(int next, State state) {
    return null;
  }

  public Node react(State state) {
    state.complete(this);
    return null;
  }

  public int getMatchingLength() {
    return path.length;
  }

  public int[] getPath() {
    return path;
  }

  public int[] getReplacement() {
    return replacement;
  }
}

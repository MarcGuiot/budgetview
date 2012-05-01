package org.globsframework.utils.stream;

public class DotStarNode implements Node {
  private Node next;

  public DotStarNode(Node next) {
    this.next = next;
  }

  public Node next(int next, State state) {
    return null;
  }

  public Node react(State state) {
    return new RemoveNode();
  }

  public int getMatchingLength() {
    return 0;
  }

  public int[] getReplacement() {
    return new int[0];
  }

  private static class RemoveNode implements Node {
    public Node next(int next, State state) {
      return null;
    }

    public Node react(State state) {
      return null;
    }

    public int getMatchingLength() {
      return 0;
    }

    public int[] getReplacement() {
      return new int[0];
    }
  }
}

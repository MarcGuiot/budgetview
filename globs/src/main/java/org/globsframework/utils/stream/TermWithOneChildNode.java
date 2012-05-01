package org.globsframework.utils.stream;

public class TermWithOneChildNode extends OneChildNode{
  private int[] replacement;

  public TermWithOneChildNode(int[] replacement, int[] path, int ch, Node node) {
    super(path, ch, node);
    this.replacement = replacement;
  }

  public Node react(State s) {
    s.complete(this);
    return this;
  }

  public int[] getReplacement() {
    return replacement;
  }

  public String toString() {
    return "TermWithOneChildNode{" +
           "replacement=" + replacement +
           '}';
  }
}

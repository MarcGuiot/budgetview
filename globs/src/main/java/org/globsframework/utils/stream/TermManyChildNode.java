package org.globsframework.utils.stream;

public class TermManyChildNode extends ManyChildNode {
  private int[] replacement;

  public TermManyChildNode(int[] replacement, int[] path) {
    super(path);
    this.replacement = replacement;
  }

  public Node react(State state) {
    state.complete(this);
    return this;
  }

  public int[] getReplacement() {
    return replacement;
  }
}

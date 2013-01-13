package org.globsframework.utils.stream;

import java.util.HashMap;

public class ManyChildNode implements Node {
  private HashMap<Integer, Node> child = new HashMap<Integer, Node>();
  private int[] partialPath;

  public ManyChildNode(int[] partialPath) {
    this.partialPath = partialPath;
  }

  public void add(int ch, Node node){
    child.put(ch, node);
  }

  public Node next(int next, State state) {
    Node element = child.get(next);
    if (element != null) {
      return element.react(state);
    }
    return null;
  }

  public Node react(State state) {
    return this;
  }

  public int getMatchingLength() {
    return partialPath.length;
  }

  public int[] getReplacement() {
    throw new RuntimeException("Not terminal " + partialPath);
  }
}

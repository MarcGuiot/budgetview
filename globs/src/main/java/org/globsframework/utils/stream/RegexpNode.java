package org.globsframework.utils.stream;

import java.util.regex.Pattern;

public class RegexpNode implements Node {
  private Pattern pattern;

  public RegexpNode(String expr) {
    pattern = Pattern.compile(expr);
  }

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

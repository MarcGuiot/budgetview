package org.globsframework.utils.stream;

public interface Node {
  
//  Node match(int ch);
  
  Node next(int next, State state);

  Node react(State state);
  
  int getMatchingLength();
  
  int[] getReplacement();
}

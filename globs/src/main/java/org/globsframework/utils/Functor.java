package org.globsframework.utils;

public interface Functor {
  void run() throws Exception;

  static Functor NULL = new Functor() {
    public void run() throws Exception {
    }
  };
}

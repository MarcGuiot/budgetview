package org.globsframework.utils;

public interface Functor {
  void run() throws Exception;

  Functor NULL = new Functor() {
    public void run() throws Exception {
    }
  };
}

package com.budgetview.gui.upgrade;

import org.globsframework.model.GlobRepository;

import java.util.ArrayList;
import java.util.List;

public class PostProcessor {

  public interface Functor {
    void apply(GlobRepository repository);
  }

  private List<Functor> functors = new ArrayList<Functor>();

  public void add(Functor functor) {
    functors.add(functor);
  }

  public void run(GlobRepository repository) {
    for (Functor functor : functors) {
      functor.apply(repository);
    }
    functors.clear();
  }
}

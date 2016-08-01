package com.budgetview.server.model;

import org.globsframework.utils.exceptions.InvalidData;

public enum ServerState {
  CREATED(0),
  DELETED(1),
  UPDATED(2),
  UNCHANGED(3);

  private int state;

  ServerState(int state) {
    this.state = state;
  }

  public int getId() {
    return state;
  }

  public static ServerState get(int state) {
    switch (state) {
      case 0:
        return CREATED;
      case 1:
        return DELETED;
      case 2:
        return UPDATED;
      case 3:
        return UNCHANGED;
    }
    throw new InvalidData("state " + state + " not valid");
  }
}

package com.budgetview.client.exceptions;

import java.io.IOException;

public class BadConnection extends RemoteException {
  static int ID = 1;

  static {
    checkId(ID);
  }

  public BadConnection(IOException e) {
    super(e);
  }

  public void visit(Visitor visitor) {
    visitor.visitBadConnection(this);
  }

  public int getId() {
    return ID;
  }
}

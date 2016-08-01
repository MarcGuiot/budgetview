package com.budgetview.client.exceptions;

public class UnknownId extends RemoteException {
  public static final int ID = 4;

  static {
    checkId(ID);
  }

  public UnknownId() {
    super("Unknown session ID");
  }

  public void visit(Visitor visitor) {
    visitor.visitUnknownId(this);
  }

  public int getId() {
    return ID;
  }

}

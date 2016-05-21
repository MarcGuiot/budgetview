package com.budgetview.client.exceptions;

public class UnableToWrite extends RemoteException {
  public static final int ID = 8;

  static {
    checkId(ID);
  }

  public UnableToWrite(String details) {
    super(details);
  }

  public void visit(Visitor visitor) {
    visitor.visitUnableToWrite(this);
  }

  public int getId() {
    return ID;
  }

}

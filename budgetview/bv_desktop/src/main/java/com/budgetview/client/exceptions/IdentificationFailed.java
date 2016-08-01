package com.budgetview.client.exceptions;

public class IdentificationFailed extends RemoteException {
  public static final int ID = 3;

  static {
    checkId(ID);
  }

  public IdentificationFailed(String details) {
    super(details);
  }

  public void visit(Visitor visitor) {
    visitor.visitIdentificationFailed(this);
  }

  public int getId() {
    return ID;
  }

}

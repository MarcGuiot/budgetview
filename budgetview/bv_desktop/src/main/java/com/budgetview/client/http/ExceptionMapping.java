package com.budgetview.client.http;

import com.budgetview.client.exceptions.*;

public class ExceptionMapping {

  public static void visit(int exceptionCode, Visitor visitor) {
    switch (exceptionCode) {
      case InvalidActionForState.ID:
        visitor.visitInvalidActionForState();
        return;
      case IdentificationFailed.ID:
        visitor.visitIdentificationFailed();
        return;
      case UserAlreadyExists.ID:
        visitor.visitUserAlreadyExist();
        return;
      case BadPassword.ID:
        visitor.visitBadPassword();
        return;
      case UserNotRegistered.ID:
        visitor.visitUserNotRegistered();
        return;
      case UnknownId.ID:
        visitor.visitUnknownId();
        return;
      default:
        visitor.visitUndefinedId();
    }
  }

  interface Visitor {
    void visitUserAlreadyExist();

    void visitInvalidActionForState();

    void visitIdentificationFailed();

    void visitBadPassword();

    void visitUnknownId();

    void visitUserNotRegistered();

    void visitUndefinedId();
  }
}

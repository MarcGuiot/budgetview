package com.budgetview.server.license.mail;

public enum Mailbox {
  SUPPORT("support@budgetview.fr"),
  ADMIN("admin@budgetview.fr");
  private String email;

  Mailbox(String email) {
    this.email = email;
  }

  public String getEmail() {
    return email;
  }
}

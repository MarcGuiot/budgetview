package org.designup.picsou.license.mail;

public enum Mailbox {
  SUPPORT("support@mybudgetview.fr"),
  ADMIN("admin@mybudgetview.fr");
  private String email;

  Mailbox(String email) {
    this.email = email;
  }

  public String getEmail() {
    return email;
  }
}

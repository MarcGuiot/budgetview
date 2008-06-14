package org.globsframework.rooms.model;

import javax.persistence.*;

@Entity
public class User {
  private Integer id;
  private String login;
  private String firstName;
  private String lastName;
  private String mail;

  public User() {
  }

  public User(String login, String firstName, String lastName, String mail) {
    this.login = login;
    this.firstName = firstName;
    this.lastName = lastName;
    this.mail = mail;
  }

  @Id
  @GeneratedValue( strategy = GenerationType.AUTO)
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  @Basic
  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  @Basic
  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  @Basic
  public String getMail() {
    return mail;
  }

  public void setMail(String mail) {
    this.mail = mail;
  }

  @Basic
  public String getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = login;
  }
}
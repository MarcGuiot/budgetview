package org.crossbowlabs.rooms.model;

import javax.persistence.*;
import java.util.Date;

@Entity
public class UseDateForBarco {
  private Integer id;
  private Date dateInDay;
  // null for from begin of the day
  private Integer fromInMinutes;
  // null for to end of the day
  private Integer toInMinutes;
  private User user;
  private Barco barco;

  public UseDateForBarco() {
  }

  public UseDateForBarco(Barco barco, Date dateInDay) {
    this.barco = barco;
    this.dateInDay = dateInDay;
  }

  @Id
  @GeneratedValue( strategy = GenerationType.AUTO)
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Date getDateInDay() {
    return dateInDay;
  }

  public void setDateInDay(Date dateInDay) {
    this.dateInDay = dateInDay;
  }

  public Integer getFromInMinutes() {
    return fromInMinutes;
  }

  public void setFromInMinutes(Integer fromInMinutes) {
    this.fromInMinutes = fromInMinutes;
  }

  public Integer getToInMinutes() {
    return toInMinutes;
  }

  public void setToInMinutes(Integer toInMinutes) {
    this.toInMinutes = toInMinutes;
  }

  @ManyToOne
  @JoinColumn(name="barcoId")
  public Barco getBarco() {
    return barco;
  }

  public void setBarco(Barco barco) {
    this.barco = barco;
  }

  @ManyToOne
  @JoinColumn(name="userId")
  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }
}
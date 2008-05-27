package org.crossbowlabs.rooms.model;

import javax.persistence.*;

@Entity
public class Barco {
  private Integer id;
  private String name;
  private String serialNumber;

  public Barco() {
  }

  public Barco(String name, String serialNumber) {
    this.name = name;
    this.serialNumber = serialNumber;
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
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Basic
  public String getSerialNumber() {
    return serialNumber;
  }

  public void setSerialNumber(String serialNumber) {
    this.serialNumber = serialNumber;
  }
}

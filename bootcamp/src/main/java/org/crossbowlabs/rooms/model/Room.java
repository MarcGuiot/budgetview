package org.crossbowlabs.rooms.model;

import javax.persistence.*;

@Entity
public class Room {
  private Integer id;
  private String name;
  private Integer size;
  private boolean canHaveBarco = true;

  public Room() {
  }

  public Room(String name, int size) {
    this.size = size;
    this.name = name;
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
  public Integer getSize() {
    return size;
  }


  public void setSize(Integer size) {
    this.size = size;
  }

  @Basic
  public boolean getCanHaveBarco() {
    return canHaveBarco;
  }

  public void setCanHaveBarco(boolean canHaveBarco) {
    this.canHaveBarco = canHaveBarco;
  }
}

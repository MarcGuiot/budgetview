package com.budgetview.shared.cloud.budgea;

public enum BudgeaCategory {
  SALAIRES_NETS(139, "Salaires nets"),
  LOYERS(225, "Loyers"),
  ELECTRICITE(230, "Electricité"),
  GAZ(229, "Gaz"),
  WATER(228, "Eau"),
  TELECOMS(226, "Télécoms"),
  RESTAURANTS1(143, "Restaurants"),
  RESTAURANTS2(191, "Restaurants"),
  FUEL1(193, "Fuel"),
  FUEL2(194, "Fuel"),
  CASH(1, "Cash"),
  FRAIS_BANCAIRES(167, "Frais bancaires"),
  AGOS(168, "Agios");

  private final int id;
  private final String name;

  BudgeaCategory(int id, String name) {
    this.id = id;
    this.name = name;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}

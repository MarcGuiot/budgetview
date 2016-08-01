package com.budgetview.shared.gui.histochart;

import org.globsframework.model.Key;

import java.util.Set;

public interface HistoDataset {

  public int size();

  public boolean isEmpty();

  public double getMaxPositiveValue();

  public double getMaxNegativeValue();

  int getId(int index);

  int getIndex(int id);

  public String getLabel(int index);

  public String getSection(int index);

  boolean containsSections();

  String getTooltip(int index, Set<Key> objectKey);

  boolean isSelected(int index);

  boolean isCurrent(int index);

  boolean isFuture(int index);

  public static final HistoDataset NULL = new HistoDataset() {

    public int getId(int index) {
      return 0;
    }

    public int getIndex(int id) {
      return -1;
    }

    public int size() {
      return 0;
    }

    public boolean isEmpty() {
      return true;
    }

    public double getMaxPositiveValue() {
      return 0;
    }

    public double getMaxNegativeValue() {
      return 0;
    }

    public String getLabel(int index) {
      return "";
    }

    public String getSection(int index) {
      return "";
    }

    public String getTooltip(int index, Set<Key> objectKey) {
      return "";
    }

    public boolean containsSections() {
      return false;
    }

    public boolean isSelected(int index) {
      return false;
    }

    public boolean isCurrent(int index) {
      return false;
    }

    public boolean isFuture(int index) {
      return false;
    }

    public String toString() {
      return "[null dataset]";
    }
  };
}

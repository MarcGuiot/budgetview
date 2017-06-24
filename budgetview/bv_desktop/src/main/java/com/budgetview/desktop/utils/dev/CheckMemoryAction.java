package com.budgetview.desktop.utils.dev;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CheckMemoryAction extends AbstractAction {

  public CheckMemoryAction() {
    super("[Check Memory]");
  }

  public void actionPerformed(ActionEvent e) {
    int mb = 1024 * 1024;

    // get Runtime instance
    Runtime instance = Runtime.getRuntime();

    System.out.println("***** Heap utilization statistics [MB] *****\n");

    System.out.println("Total Memory: " + instance.totalMemory() / mb);

    System.out.println("Free Memory: " + instance.freeMemory() / mb);

    double memoryUsage = (double) instance.freeMemory() * 100 / (double) instance.totalMemory();
    System.out.println("Memory usage : " + memoryUsage);

    System.out.println("Used Memory: " + (instance.totalMemory() - instance.freeMemory()) / mb);

    System.out.println("Max Memory: " + instance.maxMemory() / mb);
  }
}

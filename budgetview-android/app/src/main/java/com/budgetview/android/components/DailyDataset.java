package com.budgetview.android.components;

import com.budgetview.shared.gui.dailychart.AbstractHistoDailyDataset;

import org.globsframework.model.Key;

import java.util.Set;

public class DailyDataset extends AbstractHistoDailyDataset {

    public DailyDataset(Integer currentMonthId, Integer currentDayId, String currentDayLabel) {
        super("", currentMonthId, currentDayId, currentDayLabel);
    }

    public String getTooltip(int i, Set<Key> keys) {
        return "";
    }
}

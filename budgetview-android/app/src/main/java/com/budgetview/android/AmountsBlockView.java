package com.budgetview.android;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.budgetview.android.components.GaugeView;
import com.budgetview.shared.gui.gauge.GaugeModel;

import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.Glob;

public class AmountsBlockView extends LinearLayout {
    public AmountsBlockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.amounts_block, this);
    }

    public void update(Glob seriesValues,
                       DoubleField actualAmount, DoubleField plannedAmount,
                       DoubleField overrunAmount, DoubleField remainingAmount,
                       boolean invertAmounts) {
        if (seriesValues == null) {
            setVisibility(GONE);
            return;
        }

        setVisibility(VISIBLE);

        Views.setText(this, R.id.actualAmount, seriesValues.get(actualAmount), invertAmounts);
        Views.setText(this, R.id.plannedAmount, seriesValues.get(plannedAmount), invertAmounts);
        GaugeView gaugeView = (GaugeView) findViewById(R.id.amountsGauge);
        GaugeModel gaugeModel = gaugeView.getModel();
        gaugeModel.setValues(seriesValues.get(actualAmount, 0.00),
                seriesValues.get(plannedAmount, 0.00),
                seriesValues.get(overrunAmount, 0.00),
                seriesValues.get(remainingAmount, 0.00),
                "", false);

    }
}

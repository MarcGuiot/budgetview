package com.budgetview.android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.budgetview.shared.model.TransactionValues;
import com.budgetview.shared.utils.AmountFormat;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.exceptions.InvalidParameter;

import static org.globsframework.model.utils.GlobMatchers.*;

public class TransactionListFragment extends Fragment {

  public static String MONTH_PARAMETER = "transactionListActivity.parameters.month";
  public static String SERIES_VALUES_PARAMETER = "transactionListActivity.parameters.series";
  public static String ACCOUNT_PARAMETER = "transactionListActivity.parameters.account";

  private GlobMatcher matcher;

  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    Bundle args = getArguments();
    int monthId = args.getInt(MONTH_PARAMETER, -1);

    View view = inflater.inflate(R.layout.transaction_list, container, false);

    if (args.containsKey(SERIES_VALUES_PARAMETER)) {
      int seriesValuesId = args.getInt(SERIES_VALUES_PARAMETER, -1);
      matcher = and(fieldEquals(TransactionValues.BANK_MONTH, monthId),
                    fieldEquals(TransactionValues.SERIES, seriesValuesId));
    }
    else if (args.containsKey(ACCOUNT_PARAMETER)) {
      int accountId = args.getInt(ACCOUNT_PARAMETER, -1);
      matcher = and(fieldEquals(TransactionValues.BANK_MONTH, monthId),
                    fieldEquals(TransactionValues.ACCOUNT, accountId));
    }
    else {
      throw new InvalidParameter("Missing filtering parameter");
    }

    ListView list = (ListView)view.findViewById(R.id.transactionList);
    list.setAdapter(new TransactionListAdapter(inflater));

    return view;
  }

  private class TransactionListAdapter extends BaseAdapter {

    private GlobList transactionValuesList;
    private LayoutInflater inflater;

    private TransactionListAdapter(LayoutInflater inflater) {
      this.inflater = inflater;
      App app = (App)getActivity().getApplication();
      transactionValuesList =
        app.getRepository()
          .getAll(TransactionValues.TYPE, matcher)
          .sort(TransactionValues.SEQUENCE_NUMBER);
    }

    public int getCount() {
      return transactionValuesList.size();
    }

    public Object getItem(int i) {
      return transactionValuesList.get(i);
    }

    public long getItemId(int i) {
      return getItem(i).hashCode();
    }

    public View getView(int i, View previousView, ViewGroup parent) {

      View view = previousView;
      if (view == null) {
        view = inflater.inflate(R.layout.transaction_block, parent, false);
      }

      final Glob values = transactionValuesList.get(i);
      Double amount = values.get(TransactionValues.AMOUNT);
      setText(view, R.id.transactionLabel, values.get(TransactionValues.LABEL));
      setText(view, R.id.transactionAmount, amount);
      setText(view, R.id.transactionDate, getDate(values));

      if (values.isTrue(TransactionValues.PLANNED)) {
        Views.setTextColor(view, R.id.transactionLabel, R.color.item_label_disabled);
      }

      Views.setColorAmount(view, R.id.transactionAmount, amount);

      return view;
    }

    private String getDate(Glob values) {
      if (values.isTrue(TransactionValues.PLANNED)) {
        return Text.toPlannedOnDayMonthString(values.get(TransactionValues.BANK_DAY),
                                              values.get(TransactionValues.BANK_MONTH),
                                              getResources());
      }
      else {
        return Text.toOnDayMonthString(values.get(TransactionValues.BANK_DAY),
                                       values.get(TransactionValues.BANK_MONTH),
                                       getResources());
      }
    }

    private void setText(View view, int textId, Double value) {
      String text = (value == null) ? "-" : AmountFormat.DECIMAL_FORMAT.format(value);
      setText(view, textId, text);
    }

    private void setText(View view, int textId, String text) {
      TextView textView = (TextView)view.findViewById(textId);
      textView.setText(text);
    }
  }
}

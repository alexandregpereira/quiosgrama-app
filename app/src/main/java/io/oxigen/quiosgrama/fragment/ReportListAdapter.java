package io.oxigen.quiosgrama.fragment;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import io.oxigen.quiosgrama.R;
import io.oxigen.quiosgrama.Report;

public class ReportListAdapter extends RecyclerView.Adapter<ReportListAdapter.ViewHolder> {

    private final List<Report> mValues;
    private final ReportListFragment.OnListFragmentInteractionListener mListener;

    public ReportListAdapter(List<Report> items, ReportListFragment.OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Report report = mValues.get(position);
        holder.mTxtReport.setText(report.toString());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onListFragmentInteraction(report);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTxtReport;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTxtReport = (TextView) view.findViewById(R.id.txtReport);
        }

    }
}

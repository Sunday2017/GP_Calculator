package com.example.gpcalculator.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.gpcalculator.R;

import java.util.List;

public class GradeAdapter
        extends RecyclerView.Adapter<GradeAdapter.GradeViewHolder> {

    final private RecyclerItemClickListener mOnItemClickListener;
    private List<GradeEntry> mGradeEntries;
    private  Context mContext;

    /**
     * Constructor for the TaskAdapter that initializes the Context
     *
     * @param context the current Context
     * @param onItemClickListener the itemClickListener
     * */
    public GradeAdapter(Context context, RecyclerItemClickListener onItemClickListener) {
        mContext = context;
        mOnItemClickListener = onItemClickListener;
    }

    /**
     *When data changed, this method updates the list of gradeEntries
     * and notifies the adapter to use the new values on iit
     ***/
    public void setGrades(List<GradeEntry> gradeEntries) {
        mGradeEntries = gradeEntries;
        notifyDataSetChanged();
    }

    public interface RecyclerItemClickListener{
        void onItemClick(String level, String session, String semester);
    }

    /**
     * This is called when ViewHolders are created to fill a RecyclerView
     *
     * @return A new GradeViewHolder that holds the view for each grade entry
     * **/

    @NonNull
    @Override
    public GradeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        int layoutIdForListItem = R.layout.item_gp_report;

        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForListItem, parent, false);

        return new GradeViewHolder(view);
    }

    /**
     * Called by RecyclerView to display data at a specified position
     *
     * @param holder The ViewHolder to bind GradeEntry data to
     * @param position The position of the data in the list
     * **/

    @Override
    public void onBindViewHolder(@NonNull GradeViewHolder holder, int position) {
        TextView gpTV = holder.GPTextView;
        TextView gpClassTV = holder.GPClassTextView;
        TextView detailsTV = holder.detailsTextView;

        GradeEntry gradeEntry = mGradeEntries.get(position);
        double GP = gradeEntry.getGP();
        String level = gradeEntry.getLevel();
        String session = gradeEntry.getSession();
        String semester = gradeEntry.getSemester();

        gpTV.setText(String.valueOf(GP));
        gpClassTV.setText(Helper.getGradeClass(GP));
        detailsTV.setText(String.format("%s %s %s", level, semester, session));
    }

    class GradeViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{

        final TextView GPClassTextView;
        final TextView GPTextView;
        final TextView detailsTextView;
        final TextView GPDeleteTextView;

        GradeViewHolder(@NonNull View itemView) {

            super(itemView);

            GPClassTextView = itemView.findViewById(R.id.item_gp_class);
            GPTextView = itemView.findViewById(R.id.item_gp);
            detailsTextView = itemView.findViewById(R.id.item_details);
            GPDeleteTextView = itemView.findViewById(R.id.item_gp_delete);

            itemView.setOnClickListener(this);
            GPDeleteTextView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            GradeEntry gradeEntry = mGradeEntries.get(getAdapterPosition());

            String itemLevel = gradeEntry.getLevel();
            String itemSession = gradeEntry.getSession();
            String itemSemester = gradeEntry.getSemester();

            mOnItemClickListener.onItemClick(itemLevel, itemSession, itemSemester);
        }
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemCount() {
        return mGradeEntries == null ? 0 : mGradeEntries.size();
    }
}

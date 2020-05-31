package com.example.gpcalculator.data;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gpcalculator.data.GPContract.GPEntry;
import com.example.gpcalculator.data.GPContract.GPConstants;

import com.example.gpcalculator.R;

public class RecyclerViewCursorAdapter extends AbstractCustomAdapter<RecyclerViewCursorAdapter.ItemViewHolder> {

    final private RecyclerItemClickListener mOnItemClickListener;

    public RecyclerViewCursorAdapter(Cursor cursor, RecyclerItemClickListener onItemClickListener) {
        super(cursor);

        mOnItemClickListener = onItemClickListener;
    }

    public interface RecyclerItemClickListener{
        void onItemClick(Cursor cursor, int position);
        void onItemDeleteClick(View v, int position);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder viewHolder, final Cursor cursor) {

        TextView gpTV = viewHolder.GPTextView;
        TextView gpClassTV = viewHolder.GPClassTextView;
        final TextView detailsTV = viewHolder.detailsTextView;

        /* Level ### Session*/
        // [0] is ""
        String[] details = (cursor.getString(cursor.getColumnIndex(GPEntry.COLUMN_DETAILS)).split(GPConstants.STRING_SPLIT));

        double gp = cursor.getDouble(cursor.getColumnIndex(GPEntry.COLUMN_GP));
        String semester = cursor.getString(cursor.getColumnIndex(GPEntry.COLUMN_SEMESTER));


        gpTV.setText(String.valueOf(gp));
        gpClassTV.setText(GPConstants.getGradeClass(gp));
        detailsTV.setText(String.format("%s %s %s", details[1], semester, details[2]));

    }

    class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        final TextView GPClassTextView;
        final TextView GPTextView;
        final TextView detailsTextView;
        final TextView GPDeleteTextView;

        ItemViewHolder(@NonNull View itemView) {

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
            if (v.getId() == R.id.item_gp_delete) {
                mOnItemClickListener.onItemDeleteClick(v.getRootView(), getAdapterPosition());
            } else {
                mOnItemClickListener.onItemClick(getCursor(), getAdapterPosition());
            }
        }
    }


    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        // ID of the layout to inflate
        int layoutIdForListItem = R.layout.item_gp_report;

        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForListItem, parent, false);

        return new ItemViewHolder(view);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }
}

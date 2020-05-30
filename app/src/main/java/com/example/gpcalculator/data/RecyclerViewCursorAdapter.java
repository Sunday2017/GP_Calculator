package com.example.gpcalculator.data;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gpcalculator.MainActivity;
import com.example.gpcalculator.data.GPContract.GPEntry;
import com.example.gpcalculator.data.GPContract.GPConstants;

import com.example.gpcalculator.R;

public class RecyclerViewCursorAdapter extends AbstractCustomAdapter<RecyclerViewCursorAdapter.ItemViewHolder> {

    final private RecyclerItemClickListener mOnItemClickListener;

    public RecyclerViewCursorAdapter(Context context, Cursor cursor, RecyclerItemClickListener onItemClickListener) {
        super(context, cursor);

        mOnItemClickListener = onItemClickListener;
    }

    public interface RecyclerItemClickListener{
        void onItemClick(Cursor cursor, int position);
        void onItemDeleteClick(View v, int position);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder viewHolder, final Cursor cursor) {

        final int position = cursor.getPosition();

        TextView deleteTV = viewHolder.GPDeleteTextView;
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
        detailsTV.setText(details[1] +" "+ semester +" "+ details[2]);

    }

    class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView GPClassTextView, GPTextView, detailsTextView, GPDeleteTextView;

        public ItemViewHolder(@NonNull View itemView) {

            super(itemView);

            GPClassTextView = (TextView) itemView.findViewById(R.id.item_gp_class);
            GPTextView = (TextView) itemView.findViewById(R.id.item_gp);
            detailsTextView = (TextView) itemView.findViewById(R.id.item_details);
            GPDeleteTextView = (TextView) itemView.findViewById(R.id.item_gp_delete);

            itemView.setOnClickListener(this);
            GPDeleteTextView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.item_gp_delete:
                    mOnItemClickListener.onItemDeleteClick(v.getRootView(), getAdapterPosition());
                    break;
                default:
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

        // Create a NumberViewHolder object
        ItemViewHolder viewHolder = new ItemViewHolder(view);

        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }
}

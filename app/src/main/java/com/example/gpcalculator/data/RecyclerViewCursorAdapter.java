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

import com.example.gpcalculator.R;

public class RecyclerViewCursorAdapter extends AbstractCustomAdapter<RecyclerViewCursorAdapter.ItemViewHolder> {

    final private RecyclerItemClickListener mOnItemClickListener;

    public RecyclerViewCursorAdapter(Context context, Cursor cursor, RecyclerItemClickListener onItemClickListener) {
        super(context, cursor);

        mOnItemClickListener = onItemClickListener;
    }

    public interface RecyclerItemClickListener{
        void onItemClick(Cursor cursor, int position);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder viewHolder, Cursor cursor) {

        TextView deleteTV = viewHolder.GPDeleteTextView;
        TextView gpTV = viewHolder.GPTextView;
        TextView gpClassTV = viewHolder.GPClassTextView;
        final TextView detailsTV = viewHolder.detailsTextView;

        deleteTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Context context = v.getContext();
                // Create an AlertDialog.Builder and set the message, and click listeners
                // for the positive and negative buttons on the dialog.
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(R.string.confirm_to_delete);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // This is an array of {level, semester, session}
                        String[] detArray = detailsTV.getText().toString().split(" ");

                        // Using their text, format the details and subsequently, the Uri
                        String gradeDetails = GPContract.GPConstants.STRING_SPLIT +detArray[0]+
                                GPContract.GPConstants.STRING_SPLIT +detArray[2]+ GPContract.GPConstants.STRING_SPLIT+ detArray[1];

                        Uri uri = Uri.withAppendedPath(GPContract.GPEntry.CONTENT_URI, gradeDetails);

                        int num = context.getContentResolver().delete(uri, GPContract.GPEntry.COLUMN_DETAILS +"= ?", new String[] {gradeDetails});

                        if (num > 1) {
                            Toast.makeText(context, R.string.successful_delete_msg, Toast.LENGTH_SHORT).show();
                        }
                    }
                });


                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked the "Keep editing" button, so dismiss the dialog
                        // and continue editing the pet.
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });

                // Create and show the AlertDialog
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            }
        });

        /* ###+Grade.getSession()+###+Grade.getLevel()+###+ Grade.getSemester()+###+ Grade.getGP();*/
        // [0] is ""
        String[] details = (cursor.getString(cursor.getColumnIndex(GPContract.GPEntry.COLUMN_DETAILS))
                .split(GPContract.GPConstants.STRING_SPLIT));
        double gp = cursor.getDouble(cursor.getColumnIndex(GPContract.GPEntry.COLUMN_GP));


        gpTV.setText("" +gp);
        gpClassTV.setText(GPContract.GPConstants.getGradeClass(gp));
        detailsTV.setText(details[1] +" "+ details[3] +" "+ details[2]);

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
        }

        @Override
        public void onClick(View v) {
            mOnItemClickListener.onItemClick(getCursor(), getAdapterPosition());
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

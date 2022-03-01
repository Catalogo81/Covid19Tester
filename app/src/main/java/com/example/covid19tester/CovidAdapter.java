package com.example.covid19tester;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CovidAdapter extends RecyclerView.Adapter<CovidAdapter.ViewHolder>
{
    private List <CovidEntry> covidEntryList; //Backendless List "CovidEntry"
    ItemClicked activity;//linked with MainActivity

    //For when it has been clicked on my recyclerview on my MainActivity
    // list returning back a index where it was clicked
    public interface ItemClicked
    {
        void onItemClicked(int index);
    }

    //Constructor accepting the context from used activity
    // accepting also my Backendless list "CovidEntry"
    public CovidAdapter (Context context, List <CovidEntry> list)
    {
        covidEntryList = list;
        activity = (ItemClicked) context;//activity is now the context passed when item in list is clicked
    }

    //Custom ViewHolder that will link with my list layout
    public class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView tvFullName, tvPositive, tvPending;
        ImageView ivResult;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvPositive = itemView.findViewById(R.id.tvPositive);
            tvPending = itemView.findViewById(R.id.tvPending);
            ivResult = itemView.findViewById(R.id.ivResult);

            //so that we can click every item on the list getting the index clicked
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    activity.onItemClicked(covidEntryList.indexOf((CovidEntry) v.getTag()));
                }
            });
        }
    }

    //My own ViewHolder which is used by the CovidAdapter
    @NonNull
    @Override
    public CovidAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //setting the LayoutInflater which will inflate the layout resource of row_layout which is of my list
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_layout, parent, false);

        //connects the ContactAdapter ViewHolder
        return new ViewHolder(v);
    }

    //onBindViewHolder will retrieve all the data the user entered and set them to the CovidEntry database
    @Override
    public void onBindViewHolder(@NonNull CovidAdapter.ViewHolder holder, int position) {

        holder.itemView.setTag(covidEntryList.get(position));

        holder.tvFullName.setText(covidEntryList.get(position).getFullName());


        if(covidEntryList.get(position).isPositive())//if positive is true
        {
            holder.ivResult.setImageResource(R.drawable.positive);//set the image view to a red positive picture
        }
        else//if positive is false
        {
            holder.ivResult.setImageResource(R.drawable.pending);//set the image view to a yellow clock picture
        }

    }

    @Override
    public int getItemCount() {

        //how many contacts is saved on the list running the amount of x times the list has
        return covidEntryList.size();
    }

}

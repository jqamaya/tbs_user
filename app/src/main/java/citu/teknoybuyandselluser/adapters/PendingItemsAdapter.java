package citu.teknoybuyandselluser.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import citu.teknoybuyandselluser.Constants;
import citu.teknoybuyandselluser.PendingItemActivity;
import citu.teknoybuyandselluser.R;
import citu.teknoybuyandselluser.Utils;
import citu.teknoybuyandselluser.models.Item;
import io.realm.RealmResults;

/**
 ** Created by jack on 5/02/16.
 */
public class PendingItemsAdapter extends RecyclerView.Adapter<PendingItemsAdapter.ItemViewHolder> {
    private static final String TAG = "PendingItemsAdapter";

    private RealmResults<Item> mItems;

    public PendingItemsAdapter(RealmResults<Item> items) {
        mItems = items;
    }

    public void updateData (RealmResults<Item> items) {
        mItems = items;
        notifyDataSetChanged();
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        Item item = mItems.get(position);
        holder.itemImage.setImageURI(Uri.parse(item.getPicture()));
        holder.itemName.setText(Utils.capitalize(item.getName()));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        SimpleDraweeView itemImage;
        TextView itemName;

        public ItemViewHolder(View itemView) {
            super(itemView);

            itemImage = (SimpleDraweeView) itemView.findViewById(R.id.image);
            itemName = (TextView) itemView.findViewById(R.id.textViewItem);

            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            Context context = view.getContext();
            int position = getAdapterPosition();
            Item item = mItems.get(position);
            Intent intent;
            intent = new Intent(context, PendingItemActivity.class);
            intent.putExtra(Constants.Item.ID, item.getId());
            intent.putExtra(Constants.Item.ITEM_NAME, item.getName());
            intent.putExtra(Constants.Item.DESCRIPTION, item.getDescription());
            intent.putExtra(Constants.Item.PRICE, item.getPrice());
            intent.putExtra(Constants.Item.PICTURE, item.getPicture());
            intent.putExtra(Constants.Item.STARS_REQUIRED, item.getStars_required());
            intent.putExtra(Constants.Item.FORMAT_PRICE, Utils.formatFloat(item.getPrice()));
            intent.putExtra(Constants.Item.PURPOSE, item.getPurpose());

            context.startActivity(intent);
        }
    }

}

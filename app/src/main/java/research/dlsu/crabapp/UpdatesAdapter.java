package research.dlsu.crabapp;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.sql.Date;

/**
 * Created by courtneyngo on 4/26/16.
 */
public class UpdatesAdapter extends CursorRecyclerViewAdapter<UpdatesAdapter.UpdateViewHolder> {

    OnSyncListener onSyncListener;
    int viewWidth = -1;

    public UpdatesAdapter(Context context, Cursor cursor) {
        super(context, cursor);
    }

    @Override
    public int getItemCount() {
        return getCursor().getCount();
    }

    @Override
    public void onBindViewHolder(UpdateViewHolder viewHolder, Cursor cursor) {
        CrabUpdate crabUpdate = cursorToUpdate(cursor);
        viewHolder.tvUpdateId.setText((String.valueOf(crabUpdate.getId())));
        viewHolder.tvImgPath.setText(crabUpdate.getPath());
        viewHolder.tvDate.setText(crabUpdate.getDate().toString());

        setSyncable(crabUpdate.getServerIdCrabUpdate(), viewHolder);

        if(viewWidth > 0) {
            Bitmap bitmap = decodeSampledBitmapFromPath(crabUpdate.getPath(), viewWidth, -1);
            viewHolder.ivCrab.setImageBitmap(bitmap);
        }

        crabUpdate.setAdapterPosition(viewHolder.getAdapterPosition());
        viewHolder.buttonSync.setTag(R.id.model_object, crabUpdate);


        viewHolder.buttonSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean canSync = Boolean.parseBoolean(v.getTag(R.id.sync_enabled).toString());

                if(canSync) {
                    CrabUpdate cu = (CrabUpdate) v.getTag(R.id.model_object);
                    onSyncListener.onSync(cu);
                }
            }
        });

    }

    public void setSyncable(long serverId, UpdateViewHolder viewHolder){
        Log.i("tag updates adapter", serverId + "");
        if(serverId > 0){
            // crab update has already been synced
            viewHolder.buttonSync.setImageResource(R.drawable.sync_64_gray);
            viewHolder.buttonSync.setEnabled(false);
            viewHolder.buttonSync.setTag(R.id.sync_enabled, false);
        }else{
            viewHolder.buttonSync.setImageResource(R.drawable.sync_64_green);
            viewHolder.buttonSync.setEnabled(true);
            viewHolder.buttonSync.setTag(R.id.sync_enabled, true);
        }
    }

    public CrabUpdate cursorToUpdate(Cursor cursor){
        CrabUpdate crabUpdate = new CrabUpdate();
        crabUpdate.setId(cursor.getLong(cursor.getColumnIndex(DatabaseContract.CrabUpdate._ID)));
        crabUpdate.setDate(new Date(cursor.getLong(cursor.getColumnIndex(DatabaseContract.CrabUpdate.COLUMN_DATE))));
        crabUpdate.setPath(cursor.getString(cursor.getColumnIndex(DatabaseContract.CrabUpdate.COLUMN_PATH)));

        // crabUpdate.setIdCrab(cursor.getInt(cursor.getColumnIndex(DatabaseContract.CrabUpdate.COLUMN_IDCRAB)));
//        Log.i("TAG", "columnidcrab " + )
        crabUpdate.setServerIdCrabUpdate(cursor.getInt(cursor.getColumnIndex(DatabaseContract.CrabUpdate.COLUMN_SERVERIDCRABUPDATE)));

        return crabUpdate;
    }

    @Override
    public UpdateViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_update_item, parent, false);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        ViewTreeObserver viewTreeObserver = view.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    viewWidth = view.getWidth();

                    notifyDataSetChanged();
                }
            });
        }


        return new UpdateViewHolder(view);
    }

    public class UpdateViewHolder extends RecyclerView.ViewHolder{

        TextView tvUpdateId, tvDate, tvImgPath;
        ImageButton buttonSync;
        ImageView ivCrab;

        public UpdateViewHolder(View itemView) {
            super(itemView);
            tvUpdateId = (TextView) itemView.findViewById(R.id.tv_id);
            tvDate = (TextView) itemView.findViewById(R.id.tv_date);
            tvImgPath = (TextView) itemView.findViewById(R.id.tv_path);
            ivCrab = (ImageView) itemView.findViewById(R.id.iv_image);
            buttonSync = (ImageButton) itemView.findViewById(R.id.button_sync);
        }
    }



    public interface OnSyncListener{
        public void onSync(CrabUpdate crabUpdate);
    }

    public void setOnSyncListener(OnSyncListener onSyncListener){
        this.onSyncListener = onSyncListener;
    }

    // Bitmap related methods
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

//        Log.i("calculate sample size", "inSampleSize is " + inSampleSize + " and with is " + width);

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

//        if (width > reqWidth) {
////            final int halfHeight = height / 2;
//            final int halfWidth = width / 2;
//            Log.i("calculate sample size", "inSampleSize is " + inSampleSize);
//
//            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
//            // height and width larger than the requested height and width.
//            while ((halfWidth / inSampleSize) > reqWidth) {
//
//                Log.i("calculate sample size", "inSampleSize is " + inSampleSize);
//                inSampleSize *= 2;
//            }
//        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromPath(String path,
                                                     int reqWidth, int reqHeight) {


        // First decode with inJustDecodeBounds=true to check dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // BitmapFactory.decodeResource(res, resId, options);
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        // return BitmapFactory.decodeResource(res, resId, options);
        return BitmapFactory.decodeFile(path, options);
    }
}

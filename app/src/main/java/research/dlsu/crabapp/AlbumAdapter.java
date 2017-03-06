package research.dlsu.crabapp;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.sql.Date;
import java.util.Calendar;

/**
 * Created by courtneyngo on 4/22/16.
 */
public class AlbumAdapter extends CursorRecyclerViewAdapter<AlbumAdapter.AlbumViewHolder>{

    private OnItemClickListener mOnItemClickListener;
    private OnLoadDataListener mOnLoadDataListener;

    int viewWidth = -1;

    public AlbumAdapter(Context context, Cursor cursor) {
        super(context, cursor);
    }

    @Override
    public void onBindViewHolder(AlbumViewHolder viewHolder, Cursor cursor) {
        CrabUpdate crabupdate = cursorToCrabUpdate(cursor);
        viewHolder.tvId.setText(String.valueOf(crabupdate.getId()));
        viewHolder.tvPath.setText(crabupdate.getPath());
        viewHolder.tvType.setText(crabupdate.getCrabType().name());

        if(!crabupdate.getResult().isEmpty()) {
            viewHolder.tvResult.setText(crabupdate.getResult());
        }else{
            viewHolder.tvResult.setText("No results yet");
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(crabupdate.getDate());
        viewHolder.tvDate.setText(calendar.get(Calendar.DAY_OF_MONTH) + " "
                                  + calendar.get(Calendar.MONTH)+1 + " "
                                  + calendar.get(Calendar.YEAR) + " || "
                                  + calendar.get(Calendar.HOUR_OF_DAY)+ ":"
                                  + calendar.get(Calendar.MINUTE));

        // button sync
        if(crabupdate.getServerIdCrabUpdate() < 1){
            viewHolder.buttonSync.setImageResource(R.drawable.sync_64_green);
        }else{
            viewHolder.buttonSync.setImageResource(R.drawable.sync_64_gray);
        }

//        viewHolder.tvNumUpdates.setText(String.valueOf(crab.getNumOfUpdates()));
//
//        if(crab.getNumOfUpdates()!=0){
//            viewHolder.tvLastUpdate.setText(crab.getLastUpdate().toString());
//        }else{
//            viewHolder.tvLastUpdate.setText("No entries yet.");
//        }

//        if(viewWidth > 0) {
//            Bitmap bitmap = decodeSampledBitmapFromPath(crabupdate.getPath(), viewWidth, -1);
//            viewHolder.ivImage.setImageBitmap(bitmap);
//        }

        viewHolder.container.setTag(crabupdate.getId());

        viewHolder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long id = Integer.parseInt(v.getTag().toString());
                mOnItemClickListener.onItemClick(id);
            }
        });
    }

    @Override
    public AlbumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_album, parent, false);

        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_update_item, parent, false);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

//        ViewTreeObserver viewTreeObserver = view.getViewTreeObserver();
//        if (viewTreeObserver.isAlive()) {
//            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                @Override
//                public void onGlobalLayout() {
//                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                    viewWidth = view.getWidth();
//
//                    notifyDataSetChanged();
//                }
//            });
//        }

        return new AlbumViewHolder(view);
    }

    public CrabUpdate cursorToCrabUpdate(Cursor cursor){
        CrabUpdate crabupdate = new CrabUpdate();
        long id = cursor.getLong(cursor.getColumnIndex(DatabaseContract.CrabUpdate._ID));
        crabupdate.setId(id);
//        crabupdate.setPath(cursor.getString(cursor.getColumnIndex(DatabaseContract.CrabUpdate.COLUMN_PATH)));
        crabupdate.setDate(new Date(cursor.getLong(cursor.getColumnIndex(DatabaseContract.CrabUpdate.COLUMN_DATE))));

        String type = cursor.getString(cursor.getColumnIndex(DatabaseContract.CrabUpdate.COLUMN_CRABTYPE));
        crabupdate.setCrabType(CrabUpdate.CrabType.valueOf(type));

        crabupdate.setServerIdCrabUpdate(cursor.getLong(cursor.getColumnIndex(DatabaseContract.CrabUpdate.COLUMN_SERVERIDCRABUPDATE)));

        crabupdate.setResult(cursor.getString(cursor.getColumnIndex(DatabaseContract.CrabUpdate.COLUMN_RESULT)));


//        crab.setTag(cursor.getString(cursor.getColumnIndex(DatabaseContract.Crab.COLUMN_TAG)));
//        crab.setStatus(cursor.getString(cursor.getColumnIndex(DatabaseContract.Crab.COLUMN_STATUS)));

//        crab.setNumOfUpdates(mOnLoadDataListener.onLoadDataNumUpdates(id));
//        crab.setLastUpdate(mOnLoadDataListener.onLoadDataLastUpdate(id));

        return crabupdate;
    }

    @Override
    public int getItemCount() {
        return getCursor().getCount();
    }

    public class AlbumViewHolder extends RecyclerView.ViewHolder{

        TextView tvId;
        TextView tvDate;
        TextView tvPath;
        TextView tvType;
        TextView tvResult;
        ImageButton buttonSync;
//        ImageView ivImage;
//        TextView tvStatus;
//        TextView tvLastUpdate;
//        TextView tvNumUpdates;
        View container;

        public AlbumViewHolder(View itemView) {
            super(itemView);
            tvId = (TextView) itemView.findViewById(R.id.tv_id);
            tvDate = (TextView) itemView.findViewById(R.id.tv_date);
            tvPath = (TextView) itemView.findViewById(R.id.tv_path);
            tvType = (TextView) itemView.findViewById(R.id.tv_type);
            tvResult = (TextView) itemView.findViewById(R.id.tv_result);
            buttonSync = (ImageButton) itemView.findViewById(R.id.button_sync);
//            ivImage = (ImageView) itemView.findViewById(R.id.iv_image);

//            tvLastUpdate = (TextView) itemView.findViewById(R.id.tv_last_update);
//            tvNumUpdates = (TextView) itemView.findViewById(R.id.tv_entries);
            container = itemView.findViewById(R.id.container);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.mOnItemClickListener = onItemClickListener;
    }

    public void setmOnLoadDataListener(OnLoadDataListener onLoadDataListener){
        this.mOnLoadDataListener = onLoadDataListener;
    }

    public interface OnItemClickListener{
        public void onItemClick(long id);
    }

    public interface OnLoadDataListener{
        public int onLoadDataNumUpdates(long id);
        public Date onLoadDataLastUpdate(long id);
    }

    // Clean all elements of the recycler

    public void clear() {
        // items.clear();
        getCursor().close();
        notifyDataSetChanged();
    }

    public void addAll(Cursor cursor) {
        // items.addAll(list);
        changeCursor(cursor);
        notifyDataSetChanged();

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



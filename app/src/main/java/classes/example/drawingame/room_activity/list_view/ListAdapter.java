package classes.example.drawingame.room_activity.list_view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import classes.example.drawingame.R;
import classes.example.drawingame.data_base.DataBase;
import classes.example.drawingame.drawing_activity.DrawingActivity;
import classes.example.drawingame.room_activity.RoomActivity;
import classes.example.drawingame.utils.Utils;

public class ListAdapter extends ArrayAdapter<Item> {
   private RoomActivity roomActivity;
   private ListView listView;
   private LayoutInflater inflater;

   public ListAdapter(Context context, RoomActivity roomActivity, ListView listView) {
      super(context, RoomActivity.holderLayoutId, ItemList.list);
      inflater = roomActivity.getLayoutInflater();
      this.roomActivity = roomActivity;
      this.listView = listView;
   }

   @Override
   public int getCount() {
      return ItemList.list.size();
   }

   @Override
   public Item getItem(int pos) {
      return ItemList.list.get(pos);
   }

   @Override
   public int getPosition(Item item) {
      return item.pos;
   }

   @Override
   public View getView(final int pos, View convertView, ViewGroup parent) {
      final ItemHolder holder;
      if (convertView == null) {
         convertView = inflater.inflate(RoomActivity.holderLayoutId, listView, false);
         scaleViewAndChildren(convertView, convertView.getLayoutParams(),
                 new ViewGroup.LayoutParams(Utils.displayWidth, Utils.displayHeight));
         holder = new ItemHolder();
      } else
         holder = (ItemHolder) convertView.getTag();

      holder.llHolder = (LinearLayout) convertView.findViewById(R.id.llHolder);
      holder.rlHolder = (RelativeLayout) convertView.findViewById(R.id.rlHolder);
      holder.pbHolderDownload = (ProgressBar) convertView.findViewById(R.id.pbHolderDownload);
      holder.tvHolderRoomName = (TextView) convertView.findViewById(R.id.tvHolderRoomName);
      holder.ibHolderRoomDrawing = (ImageButton) convertView.findViewById(R.id.ivHolderRoomDrawing);
      holder.ibHolderEdit = (ImageButton) convertView.findViewById(R.id.btnHolderEdit);

      holder.ibHolderRemove = (ImageButton) convertView.findViewById(R.id.btnHolderRemove);
      if (DataBase.thisDeviceId.equals(DataBase.DEVELOPER_DEVICE_ID))
         holder.ibHolderRemove.setVisibility(View.VISIBLE);

      holder.ibHolderRefresh = (ImageButton) convertView.findViewById(R.id.btnHolderRefresh);
      holder.tvHolderError = (TextView) convertView.findViewById(R.id.tvHolderError);
      holder.tvHolderRetry = (TextView) convertView.findViewById(R.id.tvHolderRetry);
      convertView.setTag(holder);

      holder.tvHolderRoomName.setText(ItemList.list.get(pos).roomName);
      holder.tvHolderRetry.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            ItemList.downloadItemFromImgur(ItemList.list.get(pos), "holder_retry");
         }
      });

      holder.ibHolderRemove.setOnClickListener(new View.OnClickListener() {
         public void onClick(View v) {
            ItemList.removeItem(ItemList.list.get(pos), getContext());
         }
      });

      holder.ibHolderEdit.setOnClickListener(new View.OnClickListener() {
         public void onClick(View v) {
//            Utils.roomActivity.showProgress();
            editImg(pos);
         }
      });

      holder.ibHolderRoomDrawing.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            editImg(pos);
         }
      });

      holder.ibHolderRefresh.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            ItemList.refreshItemFromDB(ItemList.list.get(pos), getContext());
         }
      });

      showHolder(holder, ItemList.list.get(pos));
      return convertView;
   }

   private void showHolder(ItemHolder holder, Item item) {
      String state = item.busyState;
      if (state == null) state = "";
      switch (state) {
         case ItemList.ITEM_PROGRESS:
            showProgress(holder);
            break;
         case "":
            if (Memory.cache.containsKey(item.getImgFileName()))
               showImage(holder, Memory.cache.get(item.getImgFileName()));
            else
               Memory.add(item);
            break;
         default:
            showError(holder);
      }
   }

   private void showProgress(final ItemHolder holder) {
      holder.pbHolderDownload.setVisibility(View.VISIBLE);

      holder.ibHolderRoomDrawing.setVisibility(View.INVISIBLE);
      holder.ibHolderEdit.setVisibility(View.INVISIBLE);
      holder.ibHolderRefresh.setVisibility(View.INVISIBLE);

      holder.tvHolderError.setVisibility(View.INVISIBLE);
      holder.tvHolderRetry.setVisibility(View.INVISIBLE);
   }

   private void showImage(final ItemHolder holder, Bitmap bitmap) {
      holder.pbHolderDownload.setVisibility(View.INVISIBLE);

      holder.ibHolderRoomDrawing.setImageBitmap(bitmap);
      holder.ibHolderRoomDrawing.setVisibility(View.VISIBLE);
      holder.ibHolderEdit.setVisibility(View.VISIBLE);
      holder.ibHolderRefresh.setVisibility(View.VISIBLE);

      holder.tvHolderError.setVisibility(View.INVISIBLE);
      holder.tvHolderRetry.setVisibility(View.INVISIBLE);
   }

   private void showError(final ItemHolder holder) {
      holder.pbHolderDownload.setVisibility(View.INVISIBLE);

      holder.ibHolderRefresh.setVisibility(View.INVISIBLE);
      holder.ibHolderRoomDrawing.setVisibility(View.INVISIBLE);
      holder.ibHolderEdit.setVisibility(View.INVISIBLE);

      holder.tvHolderError.setVisibility(View.VISIBLE);
      holder.tvHolderRetry.setVisibility(View.VISIBLE);
   }

   private void editImg(int pos) {
      ItemList.setBusyState(ItemList.list.get(pos), ItemList.ITEM_PROGRESS);
      Intent intent = new Intent(roomActivity, DrawingActivity.class);
      intent.putExtra("pos", pos);
      roomActivity.startActivity(intent);
   }

   // Scale the given view, its contents, and all of its children by its container params
   private void scaleViewAndChildren(View root, ViewGroup.LayoutParams rootParams, ViewGroup.LayoutParams containerParams) {
      float xScale = (float) containerParams.width/*container.getWidth()*/ / rootParams.width;
      float yScale = (float) containerParams.height /*container.getHeight()*/ / rootParams.height;
      float scale = Math.min(xScale, yScale);
      // Scale the view itself
      if (rootParams.width != ViewGroup.LayoutParams.MATCH_PARENT &&
              rootParams.width != ViewGroup.LayoutParams.WRAP_CONTENT) {
         rootParams.width *= scale;
      }
      if (rootParams.height != ViewGroup.LayoutParams.MATCH_PARENT &&
              rootParams.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
         rootParams.height *= scale;
      }

      // If this view has margins, scale those too
      if (rootParams instanceof ViewGroup.MarginLayoutParams) {
         ViewGroup.MarginLayoutParams marginParams =
                 (ViewGroup.MarginLayoutParams) rootParams;
         marginParams.leftMargin *= scale;
         marginParams.rightMargin *= scale;
         marginParams.topMargin *= scale;
         marginParams.bottomMargin *= scale;
      }

      // Set the layout information back into the view
      root.setLayoutParams(rootParams);
      // Scale the view's padding
      root.setPadding(
              (int) (root.getPaddingLeft() * scale),
              (int) (root.getPaddingTop() * scale),
              (int) (root.getPaddingRight() * scale),
              (int) (root.getPaddingBottom() * scale));

      if (root.getId() == R.id.tvHolderRoomName) {
         resizeText((TextView) root, scale);
      }

      // If the root view is a ViewGroup, scale all of its children recursively
      if (root instanceof ViewGroup) {
         ViewGroup groupView = (ViewGroup) root;
         for (int cnt = 0; cnt < groupView.getChildCount(); ++cnt) {
            View child = groupView.getChildAt(cnt);
            scaleViewAndChildren(child, child.getLayoutParams(), rootParams/*root*/);
         }
      }
   }

   private void resizeText(TextView textView, float scale) {//}, float scale) {
      final String DOUBLE_BYTE_SPACE = "\u3000";
      String fixString = "";
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB_MR1
              && android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
         fixString = DOUBLE_BYTE_SPACE;
      }
//      Log.d("!", "textSize = " + String.valueOf(textView.getTextSize()));

      textView.setText(fixString + textView.getText().toString() + fixString);
      textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textView.getTextSize() * scale);

//      Log.d("!", "new textSize = " + String.valueOf(textView.getTextSize()));
   }
}

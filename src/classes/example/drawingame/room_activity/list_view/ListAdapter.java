package classes.example.drawingame.room_activity.list_view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import classes.example.drawingame.R;
import classes.example.drawingame.data_base.BanChecker;
import classes.example.drawingame.drawing_activity.DrawingActivity;
import classes.example.drawingame.utils.Utils;

public class ListAdapter extends ArrayAdapter<Item> {

   private LayoutInflater inflater;

   public ListAdapter() {
      super(Utils.appContext, R.layout.holder_3_4, ItemList.list);
      inflater = Utils.roomActivity.getLayoutInflater();
   }

   @Override
   public int getCount() {
      return ItemList.size();
   }

   @Override
   public Item getItem(int pos) {
      return ItemList.get(pos);
   }

   @Override
   public int getPosition(Item item) {
      return item.pos;
   }

   @Override
   public View getView(final int pos, View convertView, ViewGroup parent) {
      final ItemHolder holder;
      if (convertView == null) {
         convertView = inflater.inflate(R.layout.holder_3_4, Utils.roomActivity.roomListView, false);
         Log.d("!", "for pos #" + String.valueOf(pos));
         scaleViewAndChildren(convertView, convertView.getLayoutParams(),
                 new ViewGroup.LayoutParams(Utils.displayMetrics.widthPixels, Utils.displayMetrics.heightPixels));
//                 Utils.roomActivity.roomListView/*.getLayoutParams()*/);
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
      holder.ibHolderRefresh = (ImageButton) convertView.findViewById(R.id.btnHolderRefresh);
      holder.tvHolderError = (TextView) convertView.findViewById(R.id.tvHolderError);
      holder.tvHolderRetry = (TextView) convertView.findViewById(R.id.tvHolderRetry);
      convertView.setTag(holder);

      holder.tvHolderRoomName.setText(ItemList.get(pos).roomName);
      holder.tvHolderRetry.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            if (ItemList.get(pos).bitmapToUpload != null) {
               Log.d("!", "pre-uploadin");
               ItemList.uploadItem(pos);
            } else {
               Log.d("!", "pre-downloadin");
               ItemList.downloadItem(pos);
            }
         }
      });

      holder.ibHolderRemove.setOnClickListener(new View.OnClickListener() {
         public void onClick(View v) {
            ItemList.removeItem(pos);
         }
      });

      holder.ibHolderEdit.setOnClickListener(new View.OnClickListener() {
         public void onClick(View v) {
            Utils.roomActivity.showProgress();
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
            ItemList.refreshItem(pos);
         }
      });

      showHolder(holder, pos);
      return convertView;
   }

   private void showHolder(ItemHolder holder, int pos) {
      Log.d("!", "#" + String.valueOf(pos) + " refreshing ");
//                + "onDownloading=" + String.valueOf(ItemList.get(pos).onDownloading)
//                + " onUploading=" + String.valueOf(ItemList.get(pos).onUploading));
      if (ItemList.get(pos).onProgress) {
         showProgress(holder);
         return;
      }
      if (ItemList.get(pos).onError) {
         showError(holder);
         if (ItemList.get(pos).currentBitmap == null)
            showDeleteBtn(holder);
         else
            showUndoBtn(holder);
         return;
      }
      setBitmapToIv(holder, pos);
//            }
//        }
   }

   private void showProgress(final ItemHolder holder) {
//        onError = false;
//        onProgress = true;
      Utils.roomActivity.runOnUiThread(new Runnable() {
         @Override
         public void run() {
            holder.pbHolderDownload.setVisibility(View.VISIBLE);

//                holder.tvHolderRoomName.setVisibility(View.VISIBLE);
            holder.ibHolderRoomDrawing.setVisibility(View.INVISIBLE);
            holder.ibHolderEdit.setVisibility(View.INVISIBLE);
            holder.ibHolderRemove.setVisibility(View.INVISIBLE);

            holder.ibHolderRefresh.setVisibility(View.INVISIBLE);

            holder.tvHolderError.setVisibility(View.INVISIBLE);
            holder.tvHolderRetry.setVisibility(View.INVISIBLE);
         }
      });
   }

   private void showImage(final ItemHolder holder) {
      Log.d("!", "showImage");
      Utils.roomActivity.runOnUiThread(new Runnable() {
         @Override
         public void run() {
            Log.d("!", "showImage in UI");
            holder.pbHolderDownload.setVisibility(View.INVISIBLE);

//        holder.tvHolderRoomName.setVisibility(View.VISIBLE);
            holder.ibHolderRoomDrawing.setVisibility(View.VISIBLE);
            holder.ibHolderEdit.setVisibility(View.VISIBLE);
            holder.ibHolderRemove.setVisibility(View.VISIBLE);

            showRefreshBtn(holder);

            holder.tvHolderError.setVisibility(View.INVISIBLE);
            holder.tvHolderRetry.setVisibility(View.INVISIBLE);
         }
      });
   }

   private void showError(final ItemHolder holder) {
//        onError = true;
//        onProgress = false;
      Utils.roomActivity.runOnUiThread(new Runnable() {
         @Override
         public void run() {
            holder.pbHolderDownload.setVisibility(View.INVISIBLE);

//                holder.tvHolderRoomName.setVisibility(View.INVISIBLE);
            holder.ibHolderRoomDrawing.setVisibility(View.INVISIBLE);
            holder.ibHolderEdit.setVisibility(View.INVISIBLE);
            holder.ibHolderRemove.setVisibility(View.INVISIBLE);

            holder.tvHolderError.setVisibility(View.VISIBLE);
            holder.tvHolderRetry.setVisibility(View.VISIBLE);
         }
      });
   }

   private void showUndoBtn(ItemHolder holder) {
      holder.ibHolderRefresh.setBackgroundResource(R.drawable.holder_refresh_undo_btn_selector);
      holder.ibHolderRefresh.setVisibility(View.VISIBLE);
      holder.ibHolderRefresh.requestLayout();
   }

   private void showDeleteBtn(ItemHolder holder) {
      holder.ibHolderRefresh.setBackgroundResource(R.drawable.holder_refresh_remove_btn_selector);
      holder.ibHolderRefresh.setVisibility(View.VISIBLE);
      holder.ibHolderRefresh.requestLayout();
   }

   private void showRefreshBtn(ItemHolder holder) {
      holder.ibHolderRefresh.setBackgroundResource(R.drawable.holder_refresh_btn_selector);
      holder.ibHolderRefresh.setVisibility(View.VISIBLE);
      holder.ibHolderRefresh.requestLayout();
   }

   private void setBitmapToIv(final ItemHolder holder, final int pos) {
      Log.d("!", "setting bitmap");
      Utils.roomActivity.runOnUiThread(new Runnable() {
         @Override
         public void run() {
//                setLl(holder);
            Bitmap bitmap = ItemList.get(pos).currentBitmap;
            if (bitmap == null) {
               Log.d("!", "setting bitmap is null!");
               ItemList.get(pos).onError = true;
               Utils.notifyAdapter();
               return;
            }
//            RelativeLayout.LayoutParams layoutParams = Utils.scaleToRl(bitmap.getWidth(), bitmap.getHeight(),
//                    holder.ibHolderRoomDrawing.getWidth(), holder.ibHolderRoomDrawing.getHeight());
//            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
//            holder.ibHolderRoomDrawing.setLayoutParams(layoutParams);
            holder.ibHolderRoomDrawing.setImageBitmap(bitmap);
//            holder.ibHolderRoomDrawing.requestLayout();
            showImage(holder);
         }
      });
   }

//    private int calcHeight() {
//        return Utils.displayMetrics.heightPixels * 2 / 3 -
//                Utils.dpToPx(20) - Utils.dpToPx(35) - Utils.dpToPx(25) - Utils.dpToPx(20);
//                top of btns      //btns             //bot of btns      //top of txt
//    }

//   private void setLl(final ItemHolder holder) {
//      AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(
//              Utils.displayMetrics.widthPixels, Utils.displayMetrics.heightPixels * 2 / 3);
//      holder.llHolder.setLayoutParams(layoutParams);
//      holder.llHolder.requestLayout();
//
//      Log.d("!", "sizes = " + String.valueOf(holder.rlHolder.getWidth()) + " "
//              + String.valueOf(holder.rlHolder.getHeight()));
//   }

   private void editImg(final int pos) {
      new BanChecker(new BanChecker.OnBanCheckedListener() {
         @Override
         public void onBanChecked(boolean isBanned) {
            if (isBanned) {
               Utils.toast(Utils.stringFromRes(R.string.banMessage));
               Utils.roomActivity.showList();
            } else {
               Intent intent = new Intent(Utils.appContext, DrawingActivity.class);
               intent.putExtra("position", pos);
               Utils.roomActivity.startActivity(intent);
            }
         }
      }).start();
   }

//   private void scaleContents(View rootView, View container) {
//      ViewGroup.LayoutParams rootParams = rootView.getLayoutParams();
//      ViewGroup.LayoutParams containerParams = container.getLayoutParams();
      // Compute the scaling ratio
//      float xScale = (float) container.getWidth() / rootParams.width;
//      float yScale = (float) container.getHeight() / rootParams.height;
//      float scale = Math.min(xScale, yScale);

      // Scale our contents
//      Log.d("!", "container width = " + String.valueOf(container.getWidth()));
//      Log.d("!", "container height = " + String.valueOf(container.getHeight()));
//      Log.d("!", "scale = " + String.valueOf(scale));
//
//   }

   // Scale the given view, its contents, and all of its children by the given factor.
   private void scaleViewAndChildren(View root, ViewGroup.LayoutParams rootParams, /*View container) {*/ViewGroup.LayoutParams containerParams) {
      float xScale = (float) containerParams.width/*container.getWidth()*/ / rootParams.width;
      float yScale = (float) containerParams.height /*container.getHeight()*/ / rootParams.height;
      float scale = Math.min(xScale, yScale);
      Log.d("!", root.toString());
      Log.d("!", "view width = " + String.valueOf(rootParams.width));
      Log.d("!", "view height = " + String.valueOf(rootParams.height));
      Log.d("!", "container width = " + String.valueOf(containerParams.width/*container.getWidth()*/));
      Log.d("!", "container height = " + String.valueOf(containerParams.height /*container.getHeight()*/));
      Log.d("!", "scale = " + String.valueOf(scale));
//      }
      // Retrieve the view's layout information
//        ViewGroup.LayoutParams layoutParams = root.getLayoutParams();
      // Scale the view itself
      if (rootParams.width != ViewGroup.LayoutParams.MATCH_PARENT &&
              rootParams.width != ViewGroup.LayoutParams.WRAP_CONTENT) {
//                 && !(root instanceof AutoResizeTextView)) {
         rootParams.width *= scale;
      }
      if (rootParams.height != ViewGroup.LayoutParams.MATCH_PARENT &&
              rootParams.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
//                 && !(root instanceof AutoResizeTextView)) {
         rootParams.height *= scale;
      }

      // If this view has margins, scale those too
      if (rootParams instanceof ViewGroup.MarginLayoutParams) {
         ViewGroup.MarginLayoutParams marginParams =
                 (ViewGroup.MarginLayoutParams) rootParams;
//         if (root.getId() == R.id.ivHolderRoomDrawing) {
//            marginParams.leftMargin = 1;
//            marginParams.rightMargin = 1;
//            marginParams.topMargin = 1;
//            marginParams.bottomMargin = 1;
//         } else {
         marginParams.leftMargin *= scale;
         marginParams.rightMargin *= scale;
         marginParams.topMargin *= scale;
         marginParams.bottomMargin *= scale;
//         }
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
      Log.d("!", "textSize = " + String.valueOf(textView.getTextSize()));

      textView.setText(fixString + textView.getText().toString() + fixString);
      textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textView.getTextSize() * scale);

      Log.d("!", "new textSize = " + String.valueOf(textView.getTextSize()));
   }
}

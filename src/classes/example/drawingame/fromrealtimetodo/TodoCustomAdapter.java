package classes.example.drawingame.fromrealtimetodo;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import classes.example.drawingame.R;
import classes.example.drawingame.activities.ChooseRoomActivity;
import co.realtime.storage.ItemAttribute;

public class TodoCustomAdapter extends ArrayAdapter<LinkedHashMap<String, ItemAttribute>> {
    Context context;
    int layoutResId;
    ArrayList<LinkedHashMap<String, ItemAttribute>> data = new ArrayList<LinkedHashMap<String, ItemAttribute>>();
    TodoCustomAdapterReceiver receiver;

    public TodoCustomAdapter(Context context, int layoutResourceId, ArrayList<LinkedHashMap<String, ItemAttribute>> data) {
        super(context, layoutResourceId, data);
        this.context = context;
        this.layoutResId = layoutResourceId;
        this.data = data;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ItemHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResId, parent, false);
            holder = new ItemHolder();
            holder.tvRoomName = (TextView) row.findViewById(R.id.tvHolderRoomName);
            holder.ivRoomDrawing = (ImageView) row.findViewById(R.id.ivHolderRoomDrawing);
            holder.btnEdit = (Button) row.findViewById(R.id.btnHolderEdit);
            holder.btnRemove = (Button) row.findViewById(R.id.btnHolderRemove);
            row.setTag(holder);
        } else {
            holder = (ItemHolder) row.getTag();
        }
        LinkedHashMap<String, ItemAttribute> item = data.get(position);
        holder.tvRoomName.setText(item.get(ChooseRoomActivity.ATTRIBUTE_ROOM_NAME).toString());
        Picasso.with(context).load(item.get(ChooseRoomActivity.ATTRIBUTE_ROOM_IMAGE_URL).toString()).into(holder.ivRoomDrawing);

        holder.btnRemove.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (receiver != null)
                    receiver.removeRoom(position);
            }
        });

        holder.btnEdit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (receiver != null)
                    receiver.editRoom(position);
            }
        });

        return row;
    }

    public void setActionsReceiver(TodoCustomAdapterReceiver receiver) {
        this.receiver = receiver;
    }

    public interface TodoCustomAdapterReceiver {
        public void removeRoom(int position);

        public void editRoom(int position);
    }

    static class ItemHolder {
        TextView tvRoomName;
        ImageView ivRoomDrawing;
        Button btnRemove;
        Button btnEdit;
    }

}

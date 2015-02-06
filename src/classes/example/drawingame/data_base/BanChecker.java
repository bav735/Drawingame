package classes.example.drawingame.data_base;

import classes.example.drawingame.utils.Utils;
import co.realtime.storage.ItemAttribute;
import co.realtime.storage.ItemSnapshot;
import co.realtime.storage.ext.OnError;
import co.realtime.storage.ext.OnItemSnapshot;

/**
 * Created by A on 29.12.2014.
 */
public class BanChecker {
    private OnBanCheckedListener listener;

    public BanChecker(OnBanCheckedListener listener) {
        this.listener = listener;
    }

    public void start() {
        DataBase.banTableRef.item(new ItemAttribute(DataBase.thisDeviceId)).get(new OnItemSnapshot() {
            @Override
            public void run(ItemSnapshot itemSnapshot) {
                if (itemSnapshot != null && itemSnapshot.val() != null && !itemSnapshot.val().isEmpty()) {
                    listener.onBanChecked(true);
                } else {
                    listener.onBanChecked(false);
                }
            }
        }, new OnError() {
            @Override
            public void run(Integer integer, String s) {
                listener.onBanChecked(false);
            }
        });
    }

    public interface OnBanCheckedListener {
        void onBanChecked(boolean isBanned);
    }
}

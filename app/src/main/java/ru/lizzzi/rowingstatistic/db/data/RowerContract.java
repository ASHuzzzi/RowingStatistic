package ru.lizzzi.rowingstatistic.db.data;

import android.provider.BaseColumns;

public class RowerContract {

    private RowerContract() {
    }

    public static final class RowerData implements BaseColumns{
        public final static String TABLE_NAME = "rower";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_ROWER = "id";
        public final static String COLUMN_DISTANCE = "distance";
        public final static String COLUMN_TIME = "time";
        public final static String COLUMN_SPEED = "speed";
        public final static String COLUMN_STROKE_RATE = "strokerate";
        public final static String COLUMN_POWER = "power";
    }
}

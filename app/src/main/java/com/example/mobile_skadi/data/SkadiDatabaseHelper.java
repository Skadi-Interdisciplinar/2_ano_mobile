package com.example.mobile_skadi.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.mobile_skadi.network.dto.AlertResponse;
import com.example.mobile_skadi.network.dto.ColdRoomResponse;
import com.example.mobile_skadi.network.dto.TemperatureReadingResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkadiDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "skadi_local.db";
    private static final int DATABASE_VERSION = 1;

    // Tabelas
    private static final String TABLE_COLD_ROOMS = "tb_local_cold_rooms";
    private static final String TABLE_ALERTS = "tb_local_alerts";
    private static final String TABLE_READINGS = "tb_local_readings";

    // Colunas Câmaras
    private static final String COL_ROOM_ID = "id";
    private static final String COL_ROOM_LOCATION = "location";
    private static final String COL_ROOM_MIN_TEMP = "min_temp";
    private static final String COL_ROOM_MAX_TEMP = "max_temp";
    private static final String COL_ROOM_CD_ID = "distribution_center_id";

    // Colunas Alertas
    private static final String COL_ALERT_ID = "id";
    private static final String COL_ALERT_ROOM_ID = "cold_room_id";
    private static final String COL_ALERT_SEVERITY = "severity_level";
    private static final String COL_ALERT_STATUS = "status";
    private static final String COL_ALERT_CREATED_AT = "created_at";
    private static final String COL_ALERT_TYPE = "type";

    // Colunas Leituras
    private static final String COL_READING_ID = "reading_id";
    private static final String COL_READING_ROOM_ID = "cold_room_id";
    private static final String COL_READING_TEMP = "temperature";
    private static final String COL_READING_MEASURED_AT = "measured_at";

    private static SkadiDatabaseHelper instance;

    public static synchronized SkadiDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SkadiDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    public SkadiDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createColdRooms = "CREATE TABLE " + TABLE_COLD_ROOMS + " (" +
                COL_ROOM_ID + " INTEGER PRIMARY KEY, " +
                COL_ROOM_LOCATION + " TEXT, " +
                COL_ROOM_MIN_TEMP + " REAL, " +
                COL_ROOM_MAX_TEMP + " REAL, " +
                COL_ROOM_CD_ID + " INTEGER);";

        String createAlerts = "CREATE TABLE " + TABLE_ALERTS + " (" +
                COL_ALERT_ID + " INTEGER PRIMARY KEY, " +
                COL_ALERT_ROOM_ID + " INTEGER, " +
                COL_ALERT_SEVERITY + " TEXT, " +
                COL_ALERT_STATUS + " TEXT, " +
                COL_ALERT_CREATED_AT + " TEXT, " +
                COL_ALERT_TYPE + " TEXT);";

        String createReadings = "CREATE TABLE " + TABLE_READINGS + " (" +
                COL_READING_ID + " INTEGER PRIMARY KEY, " +
                COL_READING_ROOM_ID + " INTEGER, " +
                COL_READING_TEMP + " REAL, " +
                COL_READING_MEASURED_AT + " TEXT);";

        db.execSQL(createColdRooms);
        db.execSQL(createAlerts);
        db.execSQL(createReadings);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COLD_ROOMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALERTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_READINGS);
        onCreate(db);
    }

    // --- Persistência de Câmaras ---
    public synchronized void saveColdRooms(List<ColdRoomResponse> rooms) {
        if (rooms == null) return;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_COLD_ROOMS, null, null);
            for (ColdRoomResponse r : rooms) {
                if (r == null || r.getId() == null) continue;
                ContentValues cv = new ContentValues();
                cv.put(COL_ROOM_ID, r.getId());
                cv.put(COL_ROOM_LOCATION, r.getLocation());
                if (r.getMinimumTemperature() != null) {
                    cv.put(COL_ROOM_MIN_TEMP, r.getMinimumTemperature().doubleValue());
                }
                if (r.getMaximumTemperature() != null) {
                    cv.put(COL_ROOM_MAX_TEMP, r.getMaximumTemperature().doubleValue());
                }
                if (r.getDistributionCenterId() != null) {
                    cv.put(COL_ROOM_CD_ID, r.getDistributionCenterId());
                }
                db.insertWithOnConflict(TABLE_COLD_ROOMS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public synchronized List<ColdRoomResponse> getCachedColdRooms() {
        List<ColdRoomResponse> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_COLD_ROOMS, null, null, null, null, null, COL_ROOM_ID + " ASC");
        if (cursor != null) {
            try {
                int idxId = cursor.getColumnIndexOrThrow(COL_ROOM_ID);
                int idxLoc = cursor.getColumnIndexOrThrow(COL_ROOM_LOCATION);
                int idxMin = cursor.getColumnIndexOrThrow(COL_ROOM_MIN_TEMP);
                int idxMax = cursor.getColumnIndexOrThrow(COL_ROOM_MAX_TEMP);
                int idxCd = cursor.getColumnIndexOrThrow(COL_ROOM_CD_ID);

                while (cursor.moveToNext()) {
                    ColdRoomResponse r = new ColdRoomResponse();
                    r.setId(cursor.getLong(idxId));
                    r.setLocation(cursor.getString(idxLoc));
                    if (!cursor.isNull(idxMin)) {
                        r.setMinimumTemperature(BigDecimal.valueOf(cursor.getDouble(idxMin)));
                    }
                    if (!cursor.isNull(idxMax)) {
                        r.setMaximumTemperature(BigDecimal.valueOf(cursor.getDouble(idxMax)));
                    }
                    if (!cursor.isNull(idxCd)) {
                        r.setDistributionCenterId(cursor.getLong(idxCd));
                    }
                    list.add(r);
                }
            } finally {
                cursor.close();
            }
        }
        return list;
    }

    // --- Persistência de Alertas ---
    public synchronized void saveAlerts(List<AlertResponse> alerts) {
        if (alerts == null) return;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_ALERTS, null, null);
            for (AlertResponse a : alerts) {
                if (a == null || a.getId() == null) continue;
                ContentValues cv = new ContentValues();
                cv.put(COL_ALERT_ID, a.getId());
                cv.put(COL_ALERT_ROOM_ID, a.getColdRoomId());
                cv.put(COL_ALERT_SEVERITY, a.getSeverityLevel());
                cv.put(COL_ALERT_STATUS, a.getStatus());
                cv.put(COL_ALERT_CREATED_AT, a.getCreatedAt());
                cv.put(COL_ALERT_TYPE, a.getType());
                db.insertWithOnConflict(TABLE_ALERTS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public synchronized List<AlertResponse> getCachedAlerts() {
        List<AlertResponse> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_ALERTS, null, null, null, null, null, COL_ALERT_ID + " DESC");
        if (cursor != null) {
            try {
                int idxId = cursor.getColumnIndexOrThrow(COL_ALERT_ID);
                int idxRoom = cursor.getColumnIndexOrThrow(COL_ALERT_ROOM_ID);
                int idxSev = cursor.getColumnIndexOrThrow(COL_ALERT_SEVERITY);
                int idxStat = cursor.getColumnIndexOrThrow(COL_ALERT_STATUS);
                int idxCreated = cursor.getColumnIndexOrThrow(COL_ALERT_CREATED_AT);
                int idxType = cursor.getColumnIndexOrThrow(COL_ALERT_TYPE);

                while (cursor.moveToNext()) {
                    AlertResponse a = new AlertResponse();
                    a.setId(cursor.getInt(idxId));
                    if (!cursor.isNull(idxRoom)) {
                        a.setColdRoomId(cursor.getInt(idxRoom));
                    }
                    a.setSeverityLevel(cursor.getString(idxSev));
                    a.setStatus(cursor.getString(idxStat));
                    a.setCreatedAt(cursor.getString(idxCreated));
                    a.setType(cursor.getString(idxType));
                    list.add(a);
                }
            } finally {
                cursor.close();
            }
        }
        return list;
    }

    // --- Persistência de Leituras de Temperatura ---
    public synchronized void saveReadings(List<TemperatureReadingResponse> readings) {
        if (readings == null) return;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_READINGS, null, null);
            for (TemperatureReadingResponse tr : readings) {
                if (tr == null || tr.getReadingId() == null) continue;
                ContentValues cv = new ContentValues();
                cv.put(COL_READING_ID, tr.getReadingId());
                cv.put(COL_READING_ROOM_ID, tr.getColdRoomId());
                if (tr.getTemperature() != null) {
                    cv.put(COL_READING_TEMP, tr.getTemperature().doubleValue());
                }
                cv.put(COL_READING_MEASURED_AT, tr.getMeasuredAt());
                db.insertWithOnConflict(TABLE_READINGS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public synchronized List<TemperatureReadingResponse> getCachedReadings() {
        List<TemperatureReadingResponse> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_READINGS, null, null, null, null, null, COL_READING_ID + " DESC");
        if (cursor != null) {
            try {
                int idxId = cursor.getColumnIndexOrThrow(COL_READING_ID);
                int idxRoom = cursor.getColumnIndexOrThrow(COL_READING_ROOM_ID);
                int idxTemp = cursor.getColumnIndexOrThrow(COL_READING_TEMP);
                int idxTime = cursor.getColumnIndexOrThrow(COL_READING_MEASURED_AT);

                while (cursor.moveToNext()) {
                    TemperatureReadingResponse tr = new TemperatureReadingResponse();
                    tr.setReadingId(cursor.getLong(idxId));
                    if (!cursor.isNull(idxRoom)) {
                        tr.setColdRoomId(cursor.getLong(idxRoom));
                    }
                    if (!cursor.isNull(idxTemp)) {
                        tr.setTemperature(BigDecimal.valueOf(cursor.getDouble(idxTemp)));
                    }
                    tr.setMeasuredAt(cursor.getString(idxTime));
                    list.add(tr);
                }
            } finally {
                cursor.close();
            }
        }
        return list;
    }

    public synchronized Map<Long, TemperatureReadingResponse> getCachedReadingsMap() {
        Map<Long, TemperatureReadingResponse> map = new HashMap<>();
        for (TemperatureReadingResponse tr : getCachedReadings()) {
            if (tr.getColdRoomId() != null) {
                map.put(tr.getColdRoomId(), tr);
            }
        }
        return map;
    }

    public synchronized Map<Long, ColdRoomResponse> getCachedColdRoomsMap() {
        Map<Long, ColdRoomResponse> map = new HashMap<>();
        for (ColdRoomResponse cr : getCachedColdRooms()) {
            if (cr.getId() != null) {
                map.put(cr.getId(), cr);
            }
        }
        return map;
    }
}

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
    private static final int DATABASE_VERSION = 3;

    // Tabelas
    private static final String TABLE_COLD_ROOMS = "tb_local_cold_rooms";
    private static final String TABLE_ALERTS = "tb_local_alerts";
    private static final String TABLE_READINGS = "tb_local_readings";
    private static final String TABLE_CHAT_MESSAGES = "tb_local_chat_messages";
    private static final String TABLE_CHAT_SESSIONS = "tb_local_chat_sessions";

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

    // Colunas ChatBot Trimmy
    private static final String COL_CHAT_ID = "id";
    private static final String COL_CHAT_SESSION_ID = "session_id";
    private static final String COL_CHAT_SENDER = "sender";
    private static final String COL_CHAT_MESSAGE = "message";
    private static final String COL_CHAT_TIMESTAMP = "timestamp";
    private static final String COL_CHAT_IS_ERROR = "is_error";

    // Colunas Sessões de Chat
    private static final String COL_SESSION_ID = "id";
    private static final String COL_SESSION_TITLE = "title";
    private static final String COL_SESSION_CREATED_AT = "created_at";

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

        String createSessions = "CREATE TABLE IF NOT EXISTS " + TABLE_CHAT_SESSIONS + " (" +
                COL_SESSION_ID + " TEXT PRIMARY KEY, " +
                COL_SESSION_TITLE + " TEXT, " +
                COL_SESSION_CREATED_AT + " INTEGER);";

        String createChatMessages = "CREATE TABLE IF NOT EXISTS " + TABLE_CHAT_MESSAGES + " (" +
                COL_CHAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_CHAT_SESSION_ID + " TEXT, " +
                COL_CHAT_SENDER + " TEXT, " +
                COL_CHAT_MESSAGE + " TEXT, " +
                COL_CHAT_TIMESTAMP + " INTEGER, " +
                COL_CHAT_IS_ERROR + " INTEGER);";

        db.execSQL(createColdRooms);
        db.execSQL(createAlerts);
        db.execSQL(createReadings);
        db.execSQL(createSessions);
        db.execSQL(createChatMessages);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        ensureChatTables(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        ensureChatTables(db);
    }

    private void ensureChatTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CHAT_SESSIONS + " (" +
                COL_SESSION_ID + " TEXT PRIMARY KEY, " +
                COL_SESSION_TITLE + " TEXT, " +
                COL_SESSION_CREATED_AT + " INTEGER);");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CHAT_MESSAGES + " (" +
                COL_CHAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_CHAT_SESSION_ID + " TEXT, " +
                COL_CHAT_SENDER + " TEXT, " +
                COL_CHAT_MESSAGE + " TEXT, " +
                COL_CHAT_TIMESTAMP + " INTEGER, " +
                COL_CHAT_IS_ERROR + " INTEGER);");

        // Garante adição de session_id se tabela antiga v2 existia sem a coluna
        try {
            db.execSQL("ALTER TABLE " + TABLE_CHAT_MESSAGES + " ADD COLUMN " + COL_CHAT_SESSION_ID + " TEXT;");
        } catch (Exception ignored) {}
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
                    ColdRoomResponse cr = new ColdRoomResponse();
                    cr.setId(cursor.getLong(idxId));
                    cr.setLocation(cursor.getString(idxLoc));
                    if (!cursor.isNull(idxMin)) {
                        cr.setMinimumTemperature(BigDecimal.valueOf(cursor.getDouble(idxMin)));
                    }
                    if (!cursor.isNull(idxMax)) {
                        cr.setMaximumTemperature(BigDecimal.valueOf(cursor.getDouble(idxMax)));
                    }
                    if (!cursor.isNull(idxCd)) {
                        cr.setDistributionCenterId(cursor.getLong(idxCd));
                    }
                    list.add(cr);
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

    // --- Persistência de Leituras ---
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

    // --- Sessões de Chat (Trimmy) ---
    public synchronized void createOrUpdateChatSession(String sessionId, String title) {
        if (sessionId == null || sessionId.trim().isEmpty()) return;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_SESSION_ID, sessionId);
        if (title != null && !title.trim().isEmpty()) {
            cv.put(COL_SESSION_TITLE, title.trim());
        }
        cv.put(COL_SESSION_CREATED_AT, System.currentTimeMillis());
        db.insertWithOnConflict(TABLE_CHAT_SESSIONS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public synchronized List<ChatSession> getAllChatSessions() {
        List<ChatSession> sessions = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT s." + COL_SESSION_ID + ", s." + COL_SESSION_TITLE + ", s." + COL_SESSION_CREATED_AT +
                ", COUNT(m." + COL_CHAT_ID + ") as msg_count " +
                " FROM " + TABLE_CHAT_SESSIONS + " s " +
                " LEFT JOIN " + TABLE_CHAT_MESSAGES + " m ON s." + COL_SESSION_ID + " = m." + COL_CHAT_SESSION_ID +
                " GROUP BY s." + COL_SESSION_ID +
                " ORDER BY s." + COL_SESSION_CREATED_AT + " DESC";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null) {
            try {
                int idxId = cursor.getColumnIndexOrThrow(COL_SESSION_ID);
                int idxTitle = cursor.getColumnIndexOrThrow(COL_SESSION_TITLE);
                int idxCreated = cursor.getColumnIndexOrThrow(COL_SESSION_CREATED_AT);
                int idxCount = cursor.getColumnIndexOrThrow("msg_count");

                while (cursor.moveToNext()) {
                    String id = cursor.getString(idxId);
                    String title = cursor.getString(idxTitle);
                    long created = cursor.getLong(idxCreated);
                    int count = cursor.getInt(idxCount);
                    sessions.add(new ChatSession(id, title, created, count));
                }
            } finally {
                cursor.close();
            }
        }
        return sessions;
    }

    public synchronized void deleteChatSession(String sessionId) {
        if (sessionId == null) return;
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_CHAT_MESSAGES, COL_CHAT_SESSION_ID + " = ?", new String[]{sessionId});
        db.delete(TABLE_CHAT_SESSIONS, COL_SESSION_ID + " = ?", new String[]{sessionId});
    }

    public synchronized void clearAllChatSessions() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_CHAT_MESSAGES, null, null);
        db.delete(TABLE_CHAT_SESSIONS, null, null);
    }

    // --- Mensagens do ChatBot Trimmy ---
    public synchronized long saveChatMessage(String sessionId, String sender, String message, boolean isError) {
        if (message == null || message.trim().isEmpty()) return -1;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_CHAT_SESSION_ID, sessionId);
        cv.put(COL_CHAT_SENDER, sender);
        cv.put(COL_CHAT_MESSAGE, message.trim());
        cv.put(COL_CHAT_TIMESTAMP, System.currentTimeMillis());
        cv.put(COL_CHAT_IS_ERROR, isError ? 1 : 0);
        return db.insert(TABLE_CHAT_MESSAGES, null, cv);
    }

    public synchronized List<ChatMessage> getChatMessagesForSession(String sessionId) {
        List<ChatMessage> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String selection = sessionId == null ? COL_CHAT_SESSION_ID + " IS NULL" : COL_CHAT_SESSION_ID + " = ?";
        String[] args = sessionId == null ? null : new String[]{sessionId};

        Cursor cursor = db.query(TABLE_CHAT_MESSAGES, null, selection, args, null, null, COL_CHAT_ID + " ASC");
        if (cursor != null) {
            try {
                int idxId = cursor.getColumnIndexOrThrow(COL_CHAT_ID);
                int idxSession = cursor.getColumnIndexOrThrow(COL_CHAT_SESSION_ID);
                int idxSender = cursor.getColumnIndexOrThrow(COL_CHAT_SENDER);
                int idxMsg = cursor.getColumnIndexOrThrow(COL_CHAT_MESSAGE);
                int idxTime = cursor.getColumnIndexOrThrow(COL_CHAT_TIMESTAMP);
                int idxErr = cursor.getColumnIndexOrThrow(COL_CHAT_IS_ERROR);

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idxId);
                    String sId = cursor.getString(idxSession);
                    String sender = cursor.getString(idxSender);
                    String message = cursor.getString(idxMsg);
                    long timestamp = cursor.getLong(idxTime);
                    boolean isError = cursor.getInt(idxErr) == 1;

                    list.add(new ChatMessage(id, sId, sender, message, timestamp, isError));
                }
            } finally {
                cursor.close();
            }
        }
        return list;
    }

    public synchronized String getLatestSessionId() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_CHAT_SESSIONS, new String[]{COL_SESSION_ID}, null, null, null, null, COL_SESSION_CREATED_AT + " DESC", "1");
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    return cursor.getString(0);
                }
            } finally {
                cursor.close();
            }
        }
        return null;
    }
}

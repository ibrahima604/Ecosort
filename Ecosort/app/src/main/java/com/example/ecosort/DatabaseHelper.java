package com.example.ecosort;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG             = "DatabaseHelper";
    private static final String DATABASE_NAME   = "ecosort.db";
    // VERSION 2 : ajout des tables dechet et type_dechet
    private static final int    DATABASE_VERSION = 2;

    // ─── Table users ────────────────────────────────────────────────────────
    public static final String TABLE_USERS        = "users";
    public static final String COL_USER_ID        = "id";
    public static final String COL_USER_NOM       = "nom";
    public static final String COL_USER_PRENOM    = "prenom";
    public static final String COL_USER_EMAIL     = "email";

    // ─── Table type_dechet ───────────────────────────────────────────────────
    public static final String TABLE_TYPE_DECHET  = "type_dechet";
    public static final String COL_TYPE_ID        = "id_type_dechet";
    public static final String COL_TYPE_LABEL     = "label";

    // ─── Table dechet ────────────────────────────────────────────────────────
    public static final String TABLE_DECHET           = "dechet";
    public static final String COL_DECHET_ID          = "id_dechet";
    public static final String COL_DECHET_TYPE_FK     = "id_type_dechet";
    public static final String COL_DECHET_DATE        = "date_tri";
    public static final String COL_DECHET_USER_EMAIL  = "user_email";
    // synced = 0 si pas encore envoyé au serveur, 1 si envoyé avec succès
    public static final String COL_DECHET_SYNCED      = "synced";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Création des tables
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Table users
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID     + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_NOM    + " TEXT NOT NULL, " +
                COL_USER_PRENOM + " TEXT NOT NULL, " +
                COL_USER_EMAIL  + " TEXT NOT NULL UNIQUE)");

        createTypeDechetTable(db);
        createDechetTable(db);
    }

    private void createTypeDechetTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_TYPE_DECHET + " (" +
                COL_TYPE_ID    + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TYPE_LABEL + " TEXT NOT NULL UNIQUE)");

        // Deux types de base — correspondent aux IDs dans Supabase
        db.execSQL("INSERT INTO " + TABLE_TYPE_DECHET +
                " (" + COL_TYPE_LABEL + ") VALUES ('Plastique')");
        db.execSQL("INSERT INTO " + TABLE_TYPE_DECHET +
                " (" + COL_TYPE_LABEL + ") VALUES ('Non plastique')");
    }

    private void createDechetTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_DECHET + " (" +
                COL_DECHET_ID         + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_DECHET_TYPE_FK    + " INTEGER NOT NULL, " +
                COL_DECHET_DATE       + " TEXT NOT NULL, " +
                COL_DECHET_USER_EMAIL + " TEXT NOT NULL, " +
                COL_DECHET_SYNCED     + " INTEGER DEFAULT 0, " +
                "FOREIGN KEY (" + COL_DECHET_TYPE_FK + ") REFERENCES " +
                TABLE_TYPE_DECHET + "(" + COL_TYPE_ID + "))");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // On crée les nouvelles tables sans toucher à users
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_TYPE_DECHET + " (" +
                    COL_TYPE_ID    + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_TYPE_LABEL + " TEXT NOT NULL UNIQUE)");
            db.execSQL("INSERT OR IGNORE INTO " + TABLE_TYPE_DECHET +
                    " (" + COL_TYPE_LABEL + ") VALUES ('Plastique')");
            db.execSQL("INSERT OR IGNORE INTO " + TABLE_TYPE_DECHET +
                    " (" + COL_TYPE_LABEL + ") VALUES ('Non plastique')");
            createDechetTable(db);
            Log.d(TAG, "Migration v1→v2 effectuée");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Méthodes USERS
    // ═══════════════════════════════════════════════════════════════════════

    public long insertUser(String nom, String prenom, String email) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_USER_NOM,    nom);
        v.put(COL_USER_PRENOM, prenom);
        v.put(COL_USER_EMAIL,  email);
        long result = db.insertWithOnConflict(TABLE_USERS, null, v,
                SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
        return result;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Méthodes TYPE_DECHET
    // ═══════════════════════════════════════════════════════════════════════

    /** Retourne l'id local du type ("Plastique" → 1, "Non plastique" → 2) */
    public int getTypeDechetId(String label) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_TYPE_DECHET,
                new String[]{COL_TYPE_ID},
                COL_TYPE_LABEL + "=?", new String[]{label},
                null, null, null);
        int id = -1;
        if (c.moveToFirst()) id = c.getInt(0);
        c.close();
        db.close();
        return id;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Méthodes DECHET
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Insère un déchet localement.
     * @param typeLabel  "Plastique" ou "Non plastique"
     * @param userEmail  email de l'utilisateur connecté
     * @return id local inséré (ou -1 si erreur)
     */
    public long insertDechet(String typeLabel, String userEmail) {
        int typeId = getTypeDechetId(typeLabel);
        if (typeId == -1) {
            Log.e(TAG, "Type déchet introuvable : " + typeLabel);
            return -1;
        }
        String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                .format(new Date());

        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_DECHET_TYPE_FK,    typeId);
        v.put(COL_DECHET_DATE,       date);
        v.put(COL_DECHET_USER_EMAIL, userEmail);
        v.put(COL_DECHET_SYNCED,     0); // pas encore synchronisé
        long id = db.insert(TABLE_DECHET, null, v);
        db.close();
        Log.d(TAG, "Déchet inséré localement, id=" + id);
        return id;
    }

    /** Marque un déchet comme synchronisé avec le serveur */
    public void markDechetSynced(long localId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_DECHET_SYNCED, 1);
        db.update(TABLE_DECHET, v,
                COL_DECHET_ID + "=?", new String[]{String.valueOf(localId)});
        db.close();
    }

    /** Retourne tous les déchets non encore synchronisés (pour retry offline→online) */
    public List<Map<String, String>> getUnsyncedDechets() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_DECHET, null,
                COL_DECHET_SYNCED + "=0", null, null, null, null);
        List<Map<String, String>> list = new ArrayList<>();
        while (c.moveToNext()) {
            Map<String, String> row = new HashMap<>();
            row.put("id",         c.getString(c.getColumnIndexOrThrow(COL_DECHET_ID)));
            row.put("type_id",    c.getString(c.getColumnIndexOrThrow(COL_DECHET_TYPE_FK)));
            row.put("date",       c.getString(c.getColumnIndexOrThrow(COL_DECHET_DATE)));
            row.put("user_email", c.getString(c.getColumnIndexOrThrow(COL_DECHET_USER_EMAIL)));
            list.add(row);
        }
        c.close();
        db.close();
        return list;
    }

    /** Retourne les N derniers déchets d'un utilisateur (pour l'historique local) */
    public List<Map<String, String>> getDechetsByEmail(String email, int limit) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT d." + COL_DECHET_ID + ", t." + COL_TYPE_LABEL +
                        ", d." + COL_DECHET_DATE +
                        " FROM " + TABLE_DECHET + " d" +
                        " JOIN " + TABLE_TYPE_DECHET + " t ON d." + COL_DECHET_TYPE_FK +
                        " = t." + COL_TYPE_ID +
                        " WHERE d." + COL_DECHET_USER_EMAIL + "=?" +
                        " ORDER BY d." + COL_DECHET_ID + " DESC LIMIT ?",
                new String[]{email, String.valueOf(limit)});
        List<Map<String, String>> list = new ArrayList<>();
        while (c.moveToNext()) {
            Map<String, String> row = new HashMap<>();
            row.put("id",    c.getString(0));
            row.put("label", c.getString(1));
            row.put("date",  c.getString(2));
            list.add(row);
        }
        c.close();
        db.close();
        return list;
    }
}
package com.example.ecosort;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME    = "ecosort.db";
    private static final int    DATABASE_VERSION = 1;

    public static final String TABLE_USERS  = "users";
    public static final String COL_ID       = "id";
    public static final String COL_NOM      = "nom";
    public static final String COL_PRENOM   = "prenom";
    public static final String COL_EMAIL    = "email";

    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COL_ID     + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_NOM    + " TEXT NOT NULL, "                     +
                    COL_PRENOM + " TEXT NOT NULL, "                     +
                    COL_EMAIL  + " TEXT NOT NULL UNIQUE"                +
                    ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // Insère un utilisateur, retourne l'id inséré ou -1 si échec
    public long insertUser(String nom, String prenom, String email) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NOM,    nom);
        values.put(COL_PRENOM, prenom);
        values.put(COL_EMAIL,  email);
        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result;
    }
}
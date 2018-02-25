package unipu.oikt.djaranovic.etlak.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DBHelper extends SQLiteOpenHelper { // baza podataka vezana za dnevnik krvnog tlaka e-Tlak (početna, mobilna verzija)

    // podaci za bazu i tablicu
    public static final String DATABASE_NAME = "etlak.db";
    public static final String TABLE_NAME = "krvni_tlak";

    // stupci u tablici - konstante za lakše mijenjanje i dohvaćanje
    public static final String COL_ID = "id";
    public static final String COL_SISTOLICKI = "sistolicki";
    public static final String COL_DIJASTOLICKI = "dijastolicki";
    public static final String COL_PULS = "puls";
    public static final String COL_DATUM = "datum";
    public static final String COL_MASA = "masa";

    public DBHelper(Context context) {
        // defaultni konstruktor
        super(context, DATABASE_NAME, null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // stvaranje tablice
        db.execSQL("create table " + TABLE_NAME + " (id INTEGER PRIMARY KEY AUTOINCREMENT, sistolicki INTEGER, dijastolicki INTEGER, puls INTEGER, datum INTEGER, masa FLOAT)");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // kod nadogradnje baze - brisanje i ponovno stvaranje tablice
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }


    // javna metoda za unos novog zapisa u bazu podataka
    public boolean insertData(String sistolicki, String dijastolicki, String puls) {
        // klasa za formatiranje, normalizaciju datuma
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd.MM. HH:mm");
        String currentDate = sdfDate.format(new Date());

        Cursor res = getLast();

        // vezano za tjelesnu masu
        String masa_kg = "0";
        while (res.moveToNext()) {
            masa_kg = String.valueOf(res.getInt(5));
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_SISTOLICKI, sistolicki);
        contentValues.put(COL_DIJASTOLICKI, dijastolicki);
        contentValues.put(COL_PULS, puls);
        contentValues.put(COL_DATUM, currentDate);
        contentValues.put(COL_MASA, masa_kg);

        long result = db.insert(TABLE_NAME, null, contentValues);
        return result != -1;
    }


    // javna metoda vezana za ažuriranje tjelesne mase
    public boolean updateMasa(String masa_kg) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_MASA, masa_kg);

        Cursor res = getLast();

        String id = "";
        while (res.moveToNext()) {
            id = String.valueOf(res.getInt(0));
        }

        long result = -1;
        if (!id.equals("")) {
            result = db.update(TABLE_NAME, contentValues, "id = ?", new String[]{id});
        }
        return result != -1;
    }


    // javna metoda za brisanje unosa iz baze podataka
    public void deleteData(int id, int puls) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, "id=? and puls=?", new String[]{String.valueOf(id), String.valueOf(puls)});
    }


    // kursori
    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("select * from " + TABLE_NAME + " ORDER BY ID DESC", null);
    }

    public Cursor getAllDataByDiastolic() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("select * from " + TABLE_NAME + " ORDER BY dijastolicki ASC", null);
    }

    public Cursor getLastThree() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("select * from " + TABLE_NAME +" ORDER BY ID DESC LIMIT 3", null);
    }

    public Cursor getLast() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("select * from " + TABLE_NAME +" ORDER BY ID DESC LIMIT 1", null);
    }

    public Cursor getLastDate() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("select datum from " + TABLE_NAME +" ORDER BY ID DESC LIMIT 1", null);
    }

}
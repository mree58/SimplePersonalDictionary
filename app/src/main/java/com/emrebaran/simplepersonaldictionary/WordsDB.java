package com.emrebaran.simplepersonaldictionary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mree on 12.11.2016.
 */

public class WordsDB extends SQLiteOpenHelper {


    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "dbWords";
    public static final String TABLE_WORDS = "tbWords";

    //Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_WORD = "word";
    private static final String KEY_EXPLANATION = "explanation";


    public WordsDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_WORDS_TABLE = "CREATE TABLE " + TABLE_WORDS +
                "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_WORD + " TEXT,"
                + KEY_EXPLANATION + " TEXT"
                + ")";
        db.execSQL(CREATE_WORDS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORDS);
        onCreate(db);
    }


    // Adding new contact
    long addWord(WordsClass word) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_WORD, word.getWord());
        values.put(KEY_EXPLANATION, word.getExplanation());

        long inserted_id;

        // Inserting Row
        inserted_id = db.insert(TABLE_WORDS, null, values);

        db.close(); // Closing database connection

        return inserted_id;

    }


    // Getting All Words
    public List<WordsClass> getAllWords() {
        List<WordsClass> wordsList = new ArrayList<WordsClass>();
        // Select All Query Order By Days Left
        String selectQuery = "SELECT  * FROM " + TABLE_WORDS + " ORDER BY cast("+KEY_WORD +" AS TEXT)";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                WordsClass word = new WordsClass();
                word.setID(Integer.parseInt(cursor.getString(0)));
                word.setWord(cursor.getString(1));
                word.setExplanation(cursor.getString(2));

                // Adding people to list
                wordsList.add(word);
            } while (cursor.moveToNext());
        }


        db.close();

        return wordsList;
    }


    //get row count
    public int getRowCount() {

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_WORDS;
        Cursor cursor = db.rawQuery(selectQuery, null);

        int count = cursor.getCount();

        if(cursor != null && !cursor.isClosed()){
            cursor.close();
        }

        db.close();

        return count;
    }


    public int updateWord(int id, String explanation) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_EXPLANATION, explanation);

        // updating row
        return db.update(TABLE_WORDS, values, KEY_ID + " = ?", new String[] { String.valueOf(id) });
    }

    // Deleting single word
    public void deleteWord(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_WORDS, KEY_ID + " = ?", new String[] { String.valueOf(id) });
        db.close();
    }


    public WordsClass cursorToNote(Cursor cursor) {

        WordsClass w = new WordsClass();

        w.setID(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
        w.setWord(cursor.getString(cursor.getColumnIndex(KEY_WORD)));
        w.setExplanation(cursor.getString(cursor.getColumnIndex(KEY_EXPLANATION)));

        return w;
    }

    public long insertNote(WordsClass w) {
        return insert(TABLE_WORDS, noteToValues(w));
    }
    public long insert(String table, ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        long index = db.insert(table, null, values);
        db.close();
        return index;
    }

    private ContentValues noteToValues(WordsClass w) {
        ContentValues values = new ContentValues();
        values.put(KEY_WORD, w.getWord());
        values.put(KEY_EXPLANATION, w.getExplanation());

        return values;
    }


    public boolean deleteNote(String where) {
        return delete(TABLE_WORDS, where);
    }
    public boolean delete(String table, String where) {
        SQLiteDatabase db = this.getWritableDatabase();
        long index = db.delete(table, where, null);
        db.close();
        return index > 0;
    }

}
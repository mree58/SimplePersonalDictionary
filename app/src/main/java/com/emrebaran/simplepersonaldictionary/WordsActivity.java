package com.emrebaran.simplepersonaldictionary;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.emrebaran.simplepersonaldictionary.AlphabetListAdapter.Item;
import com.emrebaran.simplepersonaldictionary.AlphabetListAdapter.Row;
import com.emrebaran.simplepersonaldictionary.AlphabetListAdapter.Section;
import com.opencsv.CSVWriter;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;


/**
 * Created by mree on 10.11.2016.
 */

public class WordsActivity extends AppCompatActivity {//ListActivity {

    DisplayMetrics metrics;

    Integer[] array_ids;
    String[] array_words;
    String[] array_explanations;

    WordsDB db= new WordsDB(this);

    String dateFormat;

    private AlphabetListAdapter adapter = new AlphabetListAdapter();
    private GestureDetector mGestureDetector;
    private List<Object[]> alphabet = new ArrayList<Object[]>();
    private HashMap<String, Integer> sections = new HashMap<String, Integer>();
    private int sideIndexHeight;
    private static float sideIndexX;
    private static float sideIndexY;
    private int indexListSize;

    List<String> words;

    Locale locale = Locale.getDefault();

    private boolean doubleBackToExitPressedOnce;
    private Handler mHandler = new Handler();


    List<Integer> controlSectionId;

    ListView lv;

    class SideIndexGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            sideIndexX = sideIndexX - distanceX;
            sideIndexY = sideIndexY - distanceY;

            if (sideIndexX >= 0 && sideIndexY >= 0) {
                displayListItem();
            }

            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }


    //for not clicking the section
    public boolean contains(final List<Integer> array, final int key) {

        return array.contains(key);

    }

    //for getting real position of item -> all - section = real pos
    public int containse(final List<Integer> array, final int pos) {

        int i =0;

        while(i<=pos){

            Log.d("i", String.valueOf(i));
            Log.d("pos", String.valueOf(pos));


            i++;

            if(i>=array.size()) break;
            if(array.get(i)>=pos) break;


        }

        Log.d("containse", String.valueOf(i));

        return i;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        array_words = new String[0];

        //for popup depending on resolution
        Display display = ((WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        metrics = new DisplayMetrics();
        display.getMetrics(metrics);


        lv = (ListView)findViewById(R.id.list);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!contains(controlSectionId,position))
                    //Toast.makeText(getApplicationContext(),"Short: "+countries.get(position-containse(controlSectionId,position)),Toast.LENGTH_SHORT).show();
                    showPopupEdit(WordsActivity.this,words.get(position-containse(controlSectionId,position)),position-containse(controlSectionId,position));

            }
        });



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(WordsActivity.this);

            }
        });


        mGestureDetector = new GestureDetector(this, new WordsActivity.SideIndexGestureListener());

        load();

    }


    private void load() {


        int j = db.getRowCount();

        if(j>0) {

            List<WordsClass> contacts = db.getAllWords();

            array_ids = new Integer[j];
            array_words = new String[j];
            array_explanations = new String[j];

            int i = -1;
            for (WordsClass p : contacts) {
                i++;

                array_ids[i] = p.getID();
                array_words[i] = p.getWord();
                array_explanations[i] = p.getExplanation();
            }

            refresh(j);

        }
        else
        {
            refresh(0);
        }
    }


    private void refresh(int j) {

        words = new ArrayList<String>();

        if(array_words.length>0) {
            int i = array_words.length;
            Log.d("len",String.valueOf(i));
            for (; i > 0; i--) {
                words.add(array_words[i-1]);
            }
        }


        if(j==0) {

            List<Row> rows = new ArrayList<Row>();
            adapter.setRows(rows);
            lv.setAdapter(adapter);

            alphabet = new ArrayList<Object[]>();

            updateList();

        }
        else {


            Collator coll = Collator.getInstance(locale);
            coll.setStrength(Collator.PRIMARY);
            Collections.sort(words, coll);


            List<Row> rows = new ArrayList<Row>();
            int start = 0;
            int end = 0;
            String previousLetter = null;
            Object[] tmpIndexItem = null;
            Pattern numberPattern = Pattern.compile("[0-9]");

            controlSectionId = new ArrayList<Integer>();

            int controlId = 0;
            int control = 0;

            alphabet = new ArrayList<Object[]>();

            for (String word : words) {
                String firstLetter = word.substring(0, 1);


                // Group numbers together in the scroller
                if (numberPattern.matcher(firstLetter).matches()) {
                    firstLetter = "#";
                }

                // If we've changed to a new letter, add the previous letter to the alphabet scroller
                if (previousLetter != null && !firstLetter.equals(previousLetter)) {
                    end = rows.size() - 1;
                    tmpIndexItem = new Object[3];
                    // tmpIndexItem[0] = previousLetter.toUpperCase(Locale.getDefault());
                    tmpIndexItem[0] = previousLetter.toUpperCase(locale);
                    tmpIndexItem[1] = start;
                    tmpIndexItem[2] = end;
                    alphabet.add(tmpIndexItem);

                    Log.d("alphabet", String.valueOf(tmpIndexItem[0]));

                    start = end + 1;
                }

                // Check if we need to add a header row
                if (!firstLetter.equals(previousLetter)) {
                    rows.add(new Section(firstLetter));
                    sections.put(firstLetter, start);

                    controlSectionId.add(control + controlId);

                    //controlSectionId[controlId]=control+controlId;

                    Log.d("controlId", String.valueOf(controlId));

                    Log.d("controlSectionId", String.valueOf(control + controlId));
                    Log.d("control", String.valueOf(control));

                    controlId++;

                }

                // Add the country to the list
                rows.add(new Item(word));
                previousLetter = firstLetter;

                control++;

            }

            if (previousLetter != null) {
                // Save the last letter
                tmpIndexItem = new Object[3];
                tmpIndexItem[0] = previousLetter.toUpperCase(locale);
                tmpIndexItem[1] = start;
                tmpIndexItem[2] = rows.size() - 1;
                alphabet.add(tmpIndexItem);

                Log.d("alphabet2", String.valueOf(tmpIndexItem[0]));

            }


            Log.d("row", String.valueOf(rows.size()));

            adapter.setRows(rows);
            lv.setAdapter(adapter);

            updateList();
        }
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event)) {
            return true;
        } else {
            return false;
        }
    }

    public void updateList() {
        LinearLayout sideIndex = (LinearLayout) findViewById(R.id.sideIndex);
        sideIndex.removeAllViews();
        indexListSize = alphabet.size();

        Log.d("alphabet size",String.valueOf(alphabet.size()));

        if (indexListSize < 1) {

            displayListItem();
            return;
        }

        int indexMaxSize = (int) Math.floor(sideIndex.getHeight() / 20);
        int tmpIndexListSize = indexListSize;
        while (tmpIndexListSize > indexMaxSize) {
            tmpIndexListSize = tmpIndexListSize / 2;
        }
        double delta;
        if (tmpIndexListSize > 0) {
            delta = indexListSize / tmpIndexListSize;
        } else {
            delta = 1;
        }

        TextView tmpTV;
        for (double i = 1; i <= indexListSize; i = i + delta) {
            Object[] tmpIndexItem = alphabet.get((int) i - 1);
            String tmpLetter = tmpIndexItem[0].toString();

            tmpTV = new TextView(this);
            tmpTV.setText(tmpLetter);
            tmpTV.setGravity(Gravity.CENTER);
            tmpTV.setTextSize(15);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            tmpTV.setLayoutParams(params);
            sideIndex.addView(tmpTV);
        }

        sideIndexHeight = sideIndex.getHeight();

        sideIndex.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // now you know coordinates of touch
                sideIndexX = event.getX();
                sideIndexY = event.getY();

                // and can display a proper item it country list
                displayListItem();

                return false;
            }
        });
    }

    public void displayListItem() {
        LinearLayout sideIndex = (LinearLayout) findViewById(R.id.sideIndex);
        sideIndexHeight = sideIndex.getHeight();
        // compute number of pixels for every side index item
        double pixelPerIndexItem = (double) sideIndexHeight / indexListSize;

        // compute the item index for given event position belongs to
        int itemPosition = (int) (sideIndexY / pixelPerIndexItem);

        // get the item (we can do it since we know item index)
        if (itemPosition < alphabet.size()) {
            Object[] indexItem = alphabet.get(itemPosition);
            int subitemPosition = sections.get(indexItem[0]);

            //ListView listView = (ListView) findViewById(android.R.id.list);
            lv.setSelection(subitemPosition);

        }
    }





    private PopupWindow pw;
    private void showPopup(final Activity context) {
        try {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View layout = inflater.inflate(R.layout.layout_popup_add_new, (ViewGroup) findViewById(R.id.popup));

            float popupWidth = 350*metrics.scaledDensity;
            float popupHeight = 200*metrics.scaledDensity;

            pw = new PopupWindow(context);
            pw.setContentView(layout);
            pw.setWidth((int)popupWidth);
            pw.setHeight((int)popupHeight);
            pw.setFocusable(true);

            Point p = new Point();
            p.x = 50;
            p.y = 50;

            int OFFSET_X = -50;
            int OFFSET_Y = (int)(90*metrics.scaledDensity);


            pw.showAtLocation(layout, Gravity.TOP, p.x + OFFSET_X, p.y + OFFSET_Y);


            final EditText edtWord= (EditText) layout.findViewById(R.id.popup_edt_word);
            final EditText edtExplanation= (EditText) layout.findViewById(R.id.popup_edt_explanation);

            ImageButton close= (ImageButton) layout.findViewById(R.id.popup_btn_close);
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pw.dismiss();

                }
            });

            ImageButton save= (ImageButton) layout.findViewById(R.id.popup_btn_save);
            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    if(edtWord.getText().toString().length()<1)
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.warning_word),Toast.LENGTH_SHORT).show();
                    else if(edtExplanation.getText().toString().length()<1)
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.warning_explanation),Toast.LENGTH_SHORT).show();
                    else
                    {
                        long inserted_id;

                    inserted_id = db.addWord(new WordsClass(edtWord.getText().toString().substring(0, 1).toUpperCase() + edtWord.getText().toString().substring(1), edtExplanation.getText().toString()));

                    load();

                    pw.dismiss();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private PopupWindow pwe;
    private void showPopupEdit(final Activity context, String selectedWord, final int selectedNo) {
        try {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View layout = inflater.inflate(R.layout.layout_popup_edit, (ViewGroup) findViewById(R.id.popup));

            float popupWidth = 350*metrics.scaledDensity;
            float popupHeight = 200*metrics.scaledDensity;

            pwe = new PopupWindow(context);
            pwe.setContentView(layout);
            pwe.setWidth((int)popupWidth);
            pwe.setHeight((int)popupHeight);
            pwe.setFocusable(true);

            Point p = new Point();
            p.x = 50;
            p.y = 50;

            int OFFSET_X = -50;
            int OFFSET_Y = (int)(90*metrics.scaledDensity);


            pwe.showAtLocation(layout, Gravity.TOP, p.x + OFFSET_X, p.y + OFFSET_Y);


            final TextView txtWord= (TextView) layout.findViewById(R.id.popup_txt_word);
            final EditText edtExplanation= (EditText) layout.findViewById(R.id.popup_edt_explanation);

            txtWord.setText(selectedWord);
            edtExplanation.setText(array_explanations[selectedNo]);


            ImageButton close= (ImageButton) layout.findViewById(R.id.popup_btn_close);
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pwe.dismiss();

                }
            });

            ImageButton update= (ImageButton) layout.findViewById(R.id.popup_btn_update);
            update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(edtExplanation.getText().toString().length()<1)
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.warning_explanation),Toast.LENGTH_SHORT).show();
                    else
                    {

                        AlertDialog.Builder newDialog = new AlertDialog.Builder(WordsActivity.this);
                        newDialog.setMessage(getString(R.string.update_word));
                        newDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int which){
                                long inserted_id;
                                inserted_id = db.updateWord(array_ids[selectedNo],edtExplanation.getText().toString());
                                if(inserted_id!=0) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.update_word_succeded), Toast.LENGTH_SHORT).show();

                                    pwe.dismiss();
                                    load();
                                }
                                else
                                    Toast.makeText(getApplicationContext(),getString(R.string.update_word_failed),Toast.LENGTH_SHORT).show();
                            }
                        });
                        newDialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int which){
                                dialog.cancel();
                            }
                        });
                        newDialog.show();

                    }
                }
            });


            ImageButton delete= (ImageButton) layout.findViewById(R.id.popup_btn_delete);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AlertDialog.Builder newDialog = new AlertDialog.Builder(WordsActivity.this);
                    newDialog.setMessage(getString(R.string.delete_word));
                    newDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int which){
                            db.deleteWord(array_ids[selectedNo]);
                            load();
                            dialog.dismiss();
                            pwe.dismiss();
                        }
                    });
                    newDialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int which){
                            dialog.cancel();
                        }
                    });
                    newDialog.show();



                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private PopupWindow pwa;
    private void showPopupAbout(final Activity context) {
        try {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.layout_about, (ViewGroup) findViewById(R.id.popup_1));

            float popupWidth = 330*metrics.scaledDensity;
            float popupHeight = 440*metrics.scaledDensity;

            pwa = new PopupWindow(context);
            pwa.setContentView(layout);
            pwa.setWidth((int)popupWidth);
            pwa.setHeight((int)popupHeight);
            pwa.setFocusable(true);

            Point p = new Point();
            p.x = 50;
            p.y = 50;

            int OFFSET_X = -50;
            int OFFSET_Y = (int)(80*metrics.scaledDensity);


            pwa.showAtLocation(layout, Gravity.TOP, p.x + OFFSET_X, p.y + OFFSET_Y);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public class ExportDatabaseCSVTask extends AsyncTask<String, Void, Boolean>

    {

        private final ProgressDialog dialog = new ProgressDialog(WordsActivity.this);
        @Override

        protected void onPreExecute()
        {
            this.dialog.setMessage(getString(R.string.export_db));
            this.dialog.show();
        }


        protected Boolean doInBackground(final String... args)
        {
            WordsDB dbWords = new WordsDB(WordsActivity.this) ;
            File exportDir = new File(Environment.getExternalStorageDirectory(), "/Simple Personal Dictionary/");

            if (!exportDir.exists())

            {
                exportDir.mkdirs();
            }


            dateFormat = new SimpleDateFormat("ddMMyyyy_HHmmss", Locale.getDefault()).format(new Date());
            File file = new File(exportDir, "PersonalDictionary_"+dateFormat+".csv");


            try
            {
                CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
                SQLiteDatabase db = dbWords.getWritableDatabase();

                Cursor curCSV=db.rawQuery("SELECT * FROM " + "tbWords"+" ORDER BY word",null);
                csvWrite.writeNext(curCSV.getColumnNames());

                while(curCSV.moveToNext())
                {
                    String arrStr[] ={curCSV.getString(0),curCSV.getString(1),curCSV.getString(2)};
                    csvWrite.writeNext(arrStr);
                }

                csvWrite.close();
                curCSV.close();

                return true;
            }
            catch (IOException e)
            {
                Log.e("WordsActivity", e.getMessage(), e);
                return false;
            }
        }

        protected void onPostExecute(final Boolean success)
        {
            if (this.dialog.isShowing())
            {
                this.dialog.dismiss();
            }
            if (success)
            {
                Toast.makeText(WordsActivity.this, getString(R.string.export_succees_csv), Toast.LENGTH_SHORT).show();

                CSVToExcelConverter taskCSVToExcelConverter = new CSVToExcelConverter("PersonalDictionary_"+dateFormat);
                taskCSVToExcelConverter.execute();
            }
            else
            {
                Toast.makeText(WordsActivity.this, getString(R.string.export_fail_csv), Toast.LENGTH_SHORT).show();
            }
        }}



    public class CSVToExcelConverter extends AsyncTask<String, Void, Boolean> {

        private final ProgressDialog dialog = new ProgressDialog(WordsActivity.this);

        String csvName;
        public CSVToExcelConverter(String stg1) {
            csvName=stg1;
        }

        @Override
        protected void onPreExecute()
        {
            this.dialog.setMessage(getString(R.string.export_db));
            this.dialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            ArrayList arList=null;
            ArrayList al=null;


            String inFilePath = Environment.getExternalStorageDirectory().toString()+"/Simple Personal Dictionary/"+csvName+".csv";
            String outFilePath = Environment.getExternalStorageDirectory().toString()+"/Simple Personal Dictionary/"+csvName+".xls";
            String thisLine;
            int count=0;

            try {
                    Reader reader = new InputStreamReader(new FileInputStream(inFilePath), "UTF-8");
                    BufferedReader fin = new BufferedReader(reader);
                    String s;
                    int i=0;
                    arList = new ArrayList();
                    while ((s = fin.readLine()) != null) {

                        al = new ArrayList();
                        String strar[] = s.split(",");
                        for(int j=0;j<strar.length;j++)
                        {
                            al.add(strar[j]);
                            //Log.d("XLS COLUMN",strar[j]);
                        }
                        arList.add(al);
                        i++;
                    }
            } catch (Exception e) {
                Log.d("XLS","xls error");
            }


            try
            {
                HSSFWorkbook hwb = new HSSFWorkbook();
                HSSFSheet sheet = hwb.createSheet(getString(R.string.export_sheet));
                for(int k=0;k<arList.size();k++)
                {
                    ArrayList ardata = (ArrayList)arList.get(k);
                    HSSFRow row = sheet.createRow((short) 0+k);
                    for(int p=0;p<ardata.size();p++)
                    {
                        HSSFCell cell = row.createCell((short) p);
                        String data = ardata.get(p).toString();
                        if(data.startsWith("=")){
                            cell.setCellType(Cell.CELL_TYPE_STRING);
                            data=data.replaceAll("\"", "");
                            data=data.replaceAll("=", "");
                            cell.setCellValue(data);
                        }else if(data.startsWith("\"")){
                            data=data.replaceAll("\"", "");
                            cell.setCellType(Cell.CELL_TYPE_STRING);
                            cell.setCellValue(data);
                        }else{
                            data=data.replaceAll("\"", "");
                            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                            cell.setCellValue(data);
                        }
                        //*/
                        // cell.setCellValue(ardata.get(p).toString());
                    }
                    System.out.println();
                }
                FileOutputStream fileOut = new FileOutputStream(outFilePath);
                hwb.write(fileOut);
                fileOut.close();
                Log.d("XLS","Excel file has been generated");

            } catch ( Exception ex ) {
                ex.printStackTrace();
            }
            return true;
        }

        protected void onPostExecute(final Boolean success)
        {
            if (this.dialog.isShowing())
            {
                this.dialog.dismiss();
            }
            if (success)
            {
                Toast.makeText(WordsActivity.this, getString(R.string.export_succees_xls), Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(WordsActivity.this, getString(R.string.export_fail_xls), Toast.LENGTH_SHORT).show();
            }

        }


    }









    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        if (id == R.id.action_save) {

            ExportDatabaseCSVTask taskExportDatabaseCSVTask = new ExportDatabaseCSVTask();
            taskExportDatabaseCSVTask.execute();

            return true;
        }

        if (id == R.id.action_help) {

            Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.action_help_message), Toast.LENGTH_LONG);
            TextView vv = (TextView) toast.getView().findViewById(android.R.id.message);
            if( vv != null) vv.setGravity(Gravity.CENTER);
            toast.show();

            return true;
        }

        if (id == R.id.action_about) {
            showPopupAbout(WordsActivity.this);

            return true;
        }
        if (id == R.id.action_rate) {

            Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));
            }            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    //double click to exit
    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            doubleBackToExitPressedOnce = false;
        }
    };
    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if (mHandler != null) { mHandler.removeCallbacks(mRunnable); }
    }
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.app_exit, Toast.LENGTH_SHORT).show();

        mHandler.postDelayed(mRunnable, 1500);
    }

}
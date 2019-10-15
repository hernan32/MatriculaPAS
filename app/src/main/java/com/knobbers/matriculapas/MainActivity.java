package com.knobbers.matriculapas;

import android.Manifest;
import android.app.ActionBar;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.text.WordUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    private SearchView searchBox;
    private Switch switchSociety, switchDNI;
    private Boolean societySwitchState = false, DNISwitchState = false;
    private int[] columWithData;
    private String[] newFields;
    private int rowsCount;
    private Boolean saveToPNG = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //PERMISSION CHECK
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        //TABLE REFERENCES
        TableLayout dataTable = findViewById(R.id.datosTBL);
        rowsCount = dataTable.getChildCount();
        int[] columWithNames = new int[rowsCount];
        columWithData = new int[rowsCount];
        for (int i = 0; i < rowsCount; i++) {
            TableRow row = (TableRow) dataTable.getChildAt(i);
            columWithNames[i] = (row.getChildAt(0)).getId();
            columWithData[i] = (row.getChildAt(1)).getId();
        }

        //NEW FIELDS ARRAY
        newFields = new String[rowsCount];

        //GENERAL REFERENCES
        searchBox = findViewById(R.id.searchView);
        switchSociety = findViewById(R.id.switch1);
        switchDNI = findViewById(R.id.switch2);

        //FORCE - Search Animation
        searchBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBox.setIconified(false);
            }
        });

        //SWITCH "SOCIEDAD"
        switchSociety.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                societySwitchState = buttonView.isChecked();
                if (societySwitchState) {
                    switchDNI.setChecked(false);
                    DNISwitchState = false;
                    saveToPNG = false;
                    invalidateOptionsMenu();
                    clearForm();
                    searchBox.setQueryHint("Ingrese Nº de Sociedad");
                    saveToPNG = false;
                } else searchBox.setQueryHint("Ingrese Nº de Mat. de Matricula");
            }
        });



        //SWITCH "DNI"
        switchDNI.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DNISwitchState = buttonView.isChecked();
                if (DNISwitchState) {
                    switchSociety.setChecked(false);
                    societySwitchState = false;
                    clearForm();
                    searchBox.setQueryHint("Ingrese Nº de DNI");
                } else searchBox.setQueryHint("Ingrese Nº de Matricula");
            }
        });

        //CUSTOM FONT - Form (BETA)
        Typeface typeface = Typeface.createFromAsset(getAssets(), "Roboto-Regular.ttf");
        for (int i = 0; i < rowsCount; i++) {
            ((TextView) findViewById(columWithNames[i])).setTypeface(typeface);
        }

        //CUSTOM FONT - ActionBar (BETA)
        TextView tv = new TextView(getApplicationContext());
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(lp);
        tv.setText(R.string.app_name);
        tv.setTextSize(20);
        tv.setTextColor(Color.parseColor("#FFFFFF"));
        Typeface tf = Typeface.createFromAsset(getAssets(), "lovelo.otf");
        tv.setTypeface(tf);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(tv);

        //SEARCH VIEW - Widget
        searchBox.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.length() != 0) {
                    new setDataInTable().execute(query); //Start
                    searchBox.clearFocus(); // Clear Search View Focus (Close Keyboard)
                    return true;
                } else {
                    Toast dialogEmptySearchBox = Toast.makeText(getApplicationContext(), "Por favor ingrese un numero de matricula.", Toast.LENGTH_SHORT);
                    dialogEmptySearchBox.show();
                    return false;
                }
            }
        });
    }

    //SAVE INSTANCE STATE
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putStringArray("SAVEDDATA", newFields);
        super.onSaveInstanceState(savedInstanceState);
    }

    //RESTORE INSTANCE STATE
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        newFields = savedInstanceState.getStringArray("SAVEDDATA");
        if (!newFields[0].equals("flag")) {
            fillTable(newFields);
            //Need to refill Search Box
            searchBox.setQuery(newFields[0], false);
            searchBox.setIconified(false);
            searchBox.clearFocus();
        }
    }

    private void clearForm () {
        fillTable(new String[] {"","","","","","","","",""});
        searchBox.setQuery("",false);
        searchBox.setIconified(false);
        searchBox.clearFocus();
    }

    //EXIT - onBack
    @Override
    public void onBackPressed() {
        appExit();
    }

    //"THREE DOT" MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tdmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.generateCredential:
                try {
                    generateImage();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast toastDialogGeneratedCredential = Toast.makeText(getApplicationContext(), "Credencial generada dentro del almacenamiento.", Toast.LENGTH_SHORT);
                toastDialogGeneratedCredential.show();
                return true;
            case R.id.about:
                Intent act = new Intent(getApplicationContext(), Acerca.class);
                startActivity(act);
                overridePendingTransition(R.anim.left_in, R.anim.left_out);
                return true;
            case R.id.exit:
                appExit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //CREDENTIAL - Image Creation
    private void generateImage() throws IOException {
        Bitmap bitmap = BitmapFactory.decodeStream(getAssets().open("creden.png"));
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);// Fix - Immutable bitmap passed to Canvas constructor
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTypeface(Typeface.createFromAsset(getAssets(), "Trebuchet MS.ttf"));
        paint.setARGB(255, 0, 28, 121);
        paint.setTextSize(39);
        paint.setTextAlign(Paint.Align.CENTER); //SET AA IN CONSTRUCTOR - IF SET LATER NEED TO REDRAW
        int xCenter = 377;
        String[] AYN = newFields[1].split(",");
        canvas.drawText(WordUtils.capitalizeFully(AYN[0]), xCenter, 170, paint);
        canvas.drawText(WordUtils.capitalizeFully(AYN[1].trim()), xCenter, 212, paint);
        paint.setARGB(255, 107, 166, 218);
        paint.setTextSize(28);
        canvas.drawText("Productor Asesor de Seguros", xCenter, 250, paint);
        paint.setARGB(255, 82, 82, 82);
        paint.setTextSize(31);
        canvas.drawText("Matricula: " + newFields[0], xCenter, 314, paint);
        canvas.drawText(newFields[2], xCenter, 351, paint);
        File myDir = new File(Environment.getExternalStorageDirectory(), "/CredencialPAS-CIPAS");
        if (!myDir.exists()) myDir.mkdirs();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(new File(myDir, "Credencial-CIPAS-Mat-" + newFields[0] + ".png")));

    }

    //EXIT - Without killing process
    private void appExit() {
        this.finish();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    //SET DATA "Matricula" - Form
    private class setDataInTable extends AsyncTask<String, Object, String[]> {
        private String idPAS = "";
        private String DNI = "";

        @Override
        protected String[] doInBackground(String... toSearch) {
            if (DNISwitchState) {
                DNI = toSearch[0];
            } else {
                idPAS = toSearch[0];
            }
            ClientHttpURLConnection clientHUC = new ClientHttpURLConnection(idPAS, societySwitchState, DNISwitchState, DNI);
            try {
                newFields = clientHUC.getData();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return newFields;
        }

        @Override
        protected void onPostExecute(String[] fields) {
            fillTable(fields);
        }

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (saveToPNG) menu.getItem(0).setEnabled(true);
        return true;
    }

    //FORM FILLER
    private void fillTable(String[] fields) {
        if (fields[0].equals("flag")) {
            Toast toastDialogErrorIdPAS = Toast.makeText(getApplicationContext(), "ERROR: Matricula no encontrada.", Toast.LENGTH_SHORT);
            toastDialogErrorIdPAS.show();
        } else {
            //ENABLE SAVE CREDENTIAL
            saveToPNG = true;
            invalidateOptionsMenu();
            //FADE-IN SETUP
            final Animation in = new AlphaAnimation(0.0f, 1.0f);
            in.setDuration(500);
            //CHANGING TEXT
            TextView textData;
            for (int i = 0; i < rowsCount; i++) {
                textData = findViewById(columWithData[i]);
                textData.setText(fields[i]);
                textData.startAnimation(in);
            }
        }
    }
}
package com.knobbers.matriculapas;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ClientHttpURLConnection {

    private String matricula;
    private Boolean soc, dni;
    private String tipo = "PAS";
    private String docNro;

    public ClientHttpURLConnection(String mat, Boolean sociedad, Boolean dni, String nrodni) {
        matricula = mat;
        this.dni = dni;
        soc = sociedad;
        docNro = nrodni;
    }

    public String[] getData() throws IOException {
        //SETUP - HttpURLConnection (+ POST DATA)
        if (!dni && soc) {
            tipo = "SOC";
        }
        String urlParameters = "socpro=" + tipo + "&matricula=" + matricula + "&docNro=" + docNro;
        byte[] postData = urlParameters.getBytes(StandardCharsets.ISO_8859_1);
        int postDataLength = postData.length;
        String request = "http://www.ssn.gob.ar/storage/registros/productores/productoresactivos.asp";
        URL url = new URL(request);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("charset", "ISO_8859_1");
        conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
        conn.setUseCaches(false);
        try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
            wr.write(postData);
            wr.flush();

        }

        //READ WITH ENCODING - HttpURLConnection
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "ISO_8859_1"));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //RESPONSE
        String responseText = response.toString();

        //JSOUP - String (String/HTML) to Valid HTML Document
        Document HTML = Jsoup.parse(responseText);

        //CHECK ERROR
        boolean errorInResponse = ((HTML.select("[role=alert]")).size()) > 0;

        //ARRAY DATA - Form
        String[] newFields = new String[9];

        //PARSER - HTML 2.0
        if (!errorInResponse) {
            //Select Fields
            Elements fieldsFromHTML = HTML.select("span.destacadopdtores");
            //Remove Unused Field
            if (!soc) fieldsFromHTML.remove(2); //Remove "DNI"
            fieldsFromHTML.remove(7); //Remove "Codigo Postal"
            //Log.d("HOLA", Integer.toString(fieldsCount));
            for (int i = 0; i < 9; i++) {
                Element field = fieldsFromHTML.get(i);
                Log.d("Nombre Campo:", field.text());
                Log.d("Dato", field.parent().ownText());
                newFields[i] = field.parent().ownText(); // Data inside parent
            }
        } else newFields[0] = "flag";

        return newFields;

    }
}

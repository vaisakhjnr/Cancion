package com.cancion.ui.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.cancion.R;
import com.cancion.model.Playlist;
import com.cancion.ui.fragments.HomeFragment;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MainActivity extends AppCompatActivity {

    public ArrayList<Playlist> playlists = new ArrayList<>();
    public String mood;
    private int fetchedCount = 0;
    private String title1;
    private String title2;
    private String title3;
    private String title4;
    private String title5;
    private String title6;
    private String title7;
    private String title8;

    public static String getCharacterDataFromElement(Element e) {
        Node child = e.getFirstChild();
        if (child instanceof CharacterData) {
            CharacterData cd = (CharacterData) child;
            return cd.getData();
        }
        return "";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private void parseXML(final String apiUrl, final String title, final int noTracks) {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Playlist playlist = new Playlist();
                    playlist.playlistTitle = title;
                    playlist.noOfTracks = noTracks;
                    HttpGet uri = new HttpGet(apiUrl);
                    DefaultHttpClient client = new DefaultHttpClient();
                    HttpResponse resp = client.execute(uri);

                    StatusLine status = resp.getStatusLine();
                    if (status.getStatusCode() != 200) {
                        Log.d("MainActivity", "HTTP error, invalid server status code: " + resp.getStatusLine());
                    }

                    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    Document doc = builder.parse(resp.getEntity().getContent());

                    NodeList nodes = doc.getElementsByTagName("track");
                    for (int i = 0; i < nodes.getLength(); i++) {
                        Element element = (Element) nodes.item(i);
                        NodeList title = element.getElementsByTagName("title");
                        Element line = (Element) title.item(0);
                        playlist.tracks.add(getCharacterDataFromElement(line));
                    }
                    nodes = doc.getElementsByTagName("artist");
                    for (int i = 0; i < nodes.getLength(); i++) {
                        Element element = (Element) nodes.item(i);
                        NodeList title = element.getElementsByTagName("name");
                        Element line = (Element) title.item(0);
                        playlist.artists.add(getCharacterDataFromElement(line));
                    }
                    playlists.add(playlist);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                fetchedCount++;
                if (fetchedCount == 8) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getSupportFragmentManager().beginTransaction().add(R.id.main_frag_container, new HomeFragment()).commit();
                        }
                    });
                }
            }
        });
        thread.start();
    }
}
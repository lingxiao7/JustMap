package com.example.lx.justmap;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

/**
 * Created by lx on 2016/10/16.
 * class :JSONSerializer
 * This class can save diaries or load diaries in the file named by mFilename.
 */
public class PersonJSONSerializer {
    private static final String TAG = "JustMapJSONSerializer";


    private Context mContext;
    private String mFileName;

    public PersonJSONSerializer(Context c, String f) {
        mContext = c;
        mFileName = f;
    }

    public ArrayList<Person> loadFriends() throws IOException, JSONException {
        ArrayList<Person> persons = new ArrayList<Person>();
        BufferedReader reader = null;
        try {
            //
            InputStream in = mContext.openFileInput(mFileName);
            reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder jsonString = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                //
                jsonString.append(line);
            }
            //
            JSONArray array = (JSONArray) new JSONTokener(jsonString.toString())
                    .nextValue();
            //
            for (int i = 0; i < array.length(); i++) {
                persons.add(new Person(array.getJSONObject(i)));
            }
        } catch (FileNotFoundException e) {
            //
        } finally {
            if (reader != null)
                reader.close();
        }
        return persons;
    }

    public void saveFriends(ArrayList<Person> persons) throws JSONException, IOException {
        // Build an array in JSON
        JSONArray array = new JSONArray();
        for (Person f : persons)
            array.put(f.toJSON());

        // Write the file to disk
        Writer writer = null;
        try {
            OutputStream out = mContext
                    .openFileOutput(mFileName, Context.MODE_PRIVATE);
            writer = new OutputStreamWriter(out);
            writer.write(array.toString());
            Log.d(TAG, "persons saved to file");

        } finally {
            if (writer != null)
                writer.close();
        }
    }
}

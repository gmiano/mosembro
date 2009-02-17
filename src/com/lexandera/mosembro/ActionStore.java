package com.lexandera.mosembro;


import java.util.HashMap;

import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.lexandera.mosembro.util.MosembroUtil;

public class ActionStore extends SQLiteOpenHelper
{
    private static final String TYPE_MICROFORMAT = "microformat";
    private static final int DB_VERSION = 2;
    
    private Mosembro browser;
    private HashMap<String, Bitmap> iconCache = new HashMap<String, Bitmap>();
    private Bitmap defaultActionBitmap;
    
    public ActionStore(Mosembro context)
    {
        super(context, "mosembro", null, DB_VERSION);
        this.browser = context;
        
        byte[] defaultBytes = MosembroUtil.readRawByteArray(browser.getResources(), R.raw.mf_list_no_icon);
        defaultActionBitmap = BitmapFactory.decodeByteArray(defaultBytes, 0, defaultBytes.length);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(MosembroUtil.readRawString(browser.getResources(), R.raw.db_create));
        updateBuiltInActions();
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        switch (oldVersion) {
            case 1:
                db.execSQL("ALTER TABLE actions ADD type TEXT;");
        }
    }
    
    /* (re)installs built-in actions */
    public void updateBuiltInActions()
    {
        Resources res = browser.getResources();
        
        installAction("com.lexandera.scripts.AddressToGMap", 
                "Show address on map",
                TYPE_MICROFORMAT, "adr", 
                MosembroUtil.readRawString(res, R.raw.adr_to_gmap),
                MosembroUtil.readRawByteArray(res, R.raw.mf_list_map));
        
        installAction("com.lexandera.scripts.LondonJourneyPlanner", 
                "London journey planner",
                TYPE_MICROFORMAT, "adr", 
                MosembroUtil.readRawString(res, R.raw.adr_journeyplanner),
                MosembroUtil.readRawByteArray(res, R.raw.mf_list_journeyplanner));
        
        installAction("com.lexandera.scripts.BayAreaTripPlanner", 
                "Bay area trip planner",
                TYPE_MICROFORMAT, "adr", 
                MosembroUtil.readRawString(res, R.raw.adr_bayarea_tripplanner),
                MosembroUtil.readRawByteArray(res, R.raw.mf_list_bayarea_tripplanner));
        
        installAction("com.lexandera.scripts.AddressCopyToClipboard",
                "Copy address to clipboard",
                TYPE_MICROFORMAT, "adr", 
                MosembroUtil.readRawString(res, R.raw.adr_copy),
                MosembroUtil.readRawByteArray(res, R.raw.mf_list_copy));
        
        installAction("com.lexandera.scripts.EventToGCal", 
                "Add event to Google calendar",
                TYPE_MICROFORMAT, "vevent", 
                MosembroUtil.readRawString(res, R.raw.event_to_gcal),
                MosembroUtil.readRawByteArray(res, R.raw.mf_list_calendar));
    }
    
    public void installAction(String actionId, String name, String type, String handles, String script, String iconURL)
    {
        byte[] icon = new byte[] {};
        installAction(actionId, name, type, handles, script, icon);
    }
    
    public void installAction(String actionId, String name, String type, String handles, String script, byte[] icon)
    {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM actions WHERE action_id = ?;", new String[] { actionId });
        
        ContentValues vals = new ContentValues();
        vals.put("action_id", actionId);
        vals.put("name", name);
        vals.put("type", type);
        vals.put("handles", handles);
        vals.put("script", script);
        vals.put("icon", icon);
        db.insert("actions", null, vals);
    }
    
    public void deleteAction(String actionId)
    {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM actions WHERE action_id = ?;", new String[] { actionId });
    }
    
    public String[] getStriptsForMicroformatActions(String microformat)
    {
        SQLiteDatabase db = getReadableDatabase();
        Cursor data = db.rawQuery("SELECT script " +
        		                  "FROM actions " +
        		                  "WHERE type = ? " +
        		                  "AND handles = ?", new String[] {TYPE_MICROFORMAT, microformat });
        
        String[] out = new String[data.getCount()];
        int i = 0;
        while (data.moveToNext()) {
            out[i] = data.getString(0);
            ++i;
        }
        data.close();
        
        return out;
    }
    
    public Bitmap getIconForAction(String actionId)
    {
        // TODO: clear icon cache on script reinstall!
        
        if (!iconCache.containsKey(actionId)) {
            SQLiteDatabase db = getReadableDatabase();
            Cursor data = db.rawQuery("SELECT icon FROM actions WHERE action_id = ?", new String[] { actionId });
            Bitmap bm = null;
            
            if (data.moveToFirst()) {
                byte[] bytes = data.getBlob(0);
                bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            }
            data.close();
            
            iconCache.put(actionId, bm);
        }
        
        if (iconCache.get(actionId) == null) {
            return defaultActionBitmap;
        }
        
        return iconCache.get(actionId);
    }
}

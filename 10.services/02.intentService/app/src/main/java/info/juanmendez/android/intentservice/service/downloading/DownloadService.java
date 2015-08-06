package info.juanmendez.android.intentservice.service.downloading;

import android.app.Activity;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.inject.Inject;

import info.juanmendez.android.intentservice.BuildConfig;
import info.juanmendez.android.intentservice.MagazineApp;
import info.juanmendez.android.intentservice.R;
import info.juanmendez.android.intentservice.helper.MagazineParser;
import info.juanmendez.android.intentservice.model.Magazine;
import info.juanmendez.android.intentservice.model.MagazineStatus;
import info.juanmendez.android.intentservice.service.provider.MagazineProvider;
import info.juanmendez.android.intentservice.service.provider.SQLGlobals;
import info.juanmendez.android.intentservice.service.provider.SQLMagazine;
import info.juanmendez.android.intentservice.service.provider.SQLPage;

/**
 * The DownloadService is in charge of downloading a zip and extracting
 * files to a specific directory.
 *
 * It's going to also notify ContentProvider for latest zip downloaded, and files extracted.
 */
public class DownloadService extends IntentService
{
    @Inject
    Magazine lastMagazine;

    public DownloadService()
    {
        super("download-zip");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        MagazineApp app = ((MagazineApp) getApplication());
        app.inject(this);

        ResultReceiver rec = intent.getParcelableExtra( "receiver" );
        Bundle bundle = new Bundle();
        bundle.putString( "message", "nothing happened");
        int result = Activity.RESULT_CANCELED;

        if( lastMagazine != null )
        {
            File downloads = new File( getFilesDir(), "magazines" );
            downloads.mkdir();
            File target = new File( downloads, "target.zip" );


            if( download( target, MagazineParser.getMagazineURL( app.getLocalhost(), lastMagazine ))){
                File unzipDir = new File( getFilesDir(), "version_" + lastMagazine.getIssue());

                if( unzipDir.exists() )
                    unzipDir.delete();

                unzipDir.mkdir();

                List<String> files = unzip( target, unzipDir );

                if( files.size() > 0 ){

                    int lastMagazineID = storeMagazine();

                    if( lastMagazineID >= 0 ){

                        storePages(files, lastMagazineID);
                        result = Activity.RESULT_OK;
                        bundle.putString("message", "zip was downloaded and decompressed!");
                    }

                    bundle.putString("message", "couldn't store first magazine " + lastMagazine.getIssue() );

                }else{

                    bundle.putString("message", "couldn't unzip");
                }

            }else{

                bundle.putString("message", "couldn't download");
            }

        }

        rec.send(result, bundle);
    }

    private Boolean download( File target, URL url ){

        FileOutputStream fos = null;
        InputStream is = null;

        if( target.exists() )
            target.delete();

        try {

            fos = new FileOutputStream( target.getPath() );
            is = url.openStream();
            url.openConnection();
            IOUtils.copy(is, fos);

            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(fos);

            return true;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private List<String> unzip( File zip, File directory ){

        FileOutputStream fos = null;
        InputStream is = null;
        File entryDestination = null;
        ZipFile zipFile = null;

        List<String> files = new ArrayList<String>();

        //reading the zipEntry is appending the zip file name. We want to ignore that level.
        String pattern = "\\w+\\/(.*)";
        try{
            zipFile = new ZipFile( zip );
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while( entries.hasMoreElements()){
                ZipEntry entry = entries.nextElement();

                entryDestination = new File( directory, entry.getName().replaceAll( pattern, "$1") );

                if( entry.isDirectory() ){
                    entryDestination.mkdir();
                }else{
                    is = zipFile.getInputStream( entry );
                    fos = new FileOutputStream( entryDestination );

                    IOUtils.copy( is, fos );
                    IOUtils.closeQuietly(is);
                    IOUtils.closeQuietly( fos );
                }
            }
            zipFile.close();

            File[] listOfFiles = directory.listFiles();


            for( File file: listOfFiles ){

                if( !file.isDirectory()){
                    files.add( file.getName() );
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ZipException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return files;
    }

    private int storeMagazine(){

        Magazine cloneMagazine = MagazineParser.clone(lastMagazine);
        cloneMagazine.setFileLocation( "download_" + lastMagazine.getIssue()  );
        cloneMagazine.setStatus( MagazineStatus.DOWNLOADED );

        MagazineApp app = ((MagazineApp) getApplication());
        Uri magazineURI = Uri.parse("content://" + BuildConfig.APPLICATION_ID + ".service.provider.MagazineProvider"+ "/magazines/" + cloneMagazine.getId());

        ContentResolver cr = getContentResolver();
        ContentValues c = MagazineParser.toContentValues( cloneMagazine );

        int itemsModified = cr.update(magazineURI, c, null, null );

        if( itemsModified > 0 )
        {
            MagazineParser.overwrite( cloneMagazine, lastMagazine );
        }

        return itemsModified;
    }

    private void storePages( List<String> files, int lastMagazineID ){

        MagazineApp app = ((MagazineApp) getApplication());
        Uri pagesURI = Uri.parse("content://" + BuildConfig.APPLICATION_ID + ".service.provider.MagazineProvider" + "/pages");
        ContentResolver cr = getContentResolver();
        ContentValues c;
        for( String file: files ){

            c = new ContentValues();
            c.put(SQLPage.MAG_ID, lastMagazineID );
            c.put(SQLPage.NAME, file );
            //cr.insert( pagesURI, c );
        }
    }
}
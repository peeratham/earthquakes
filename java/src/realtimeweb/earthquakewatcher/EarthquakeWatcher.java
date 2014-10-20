package realtimeweb.earthquakewatcher;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import realtimeweb.earthquakewatcher.domain.*;
import realtimeweb.stickyweb.EditableCache;
import realtimeweb.stickyweb.StickyWeb;
import realtimeweb.stickyweb.StickyWebRequest;
import realtimeweb.stickyweb.StickyWebResponse;
import realtimeweb.stickyweb.exceptions.StickyWebDataSourceNotFoundException;
import realtimeweb.stickyweb.exceptions.StickyWebDataSourceParseException;
import realtimeweb.stickyweb.exceptions.StickyWebInternetException;
import realtimeweb.stickyweb.exceptions.StickyWebInvalidPostArguments;
import realtimeweb.stickyweb.exceptions.StickyWebInvalidQueryString;
import realtimeweb.stickyweb.exceptions.StickyWebJsonResponseParseException;
import realtimeweb.stickyweb.exceptions.StickyWebLoadDataSourceException;
import realtimeweb.stickyweb.exceptions.StickyWebNotInCacheException;

/**
 * Get the latest information about earthquakes around the world.
 */
public class EarthquakeWatcher {
    private StickyWeb connection;
	private boolean online;
    
    public static void main(String[] args) {
        EarthquakeWatcher earthquakeWatcher = new EarthquakeWatcher();
        /*
         * http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/1.0_hour.geojson
         */
        Report rp = earthquakeWatcher.getEarthquakes("1.0", "hour");
        System.out.println("Report Title:"+rp.getTitle()+"\nBBox:"+rp.getArea());

        for(Earthquake eq:rp.getEarthquakes()){
        	System.out.println("Place:"+eq.getLocationDescription());
        	System.out.println("Magnitude:"+eq.getMagnitude());
        	System.out.println("Time:"+eq.getTime());
        	System.out.println("Coordinates:"+eq.getLocation());
        	System.out.println();
        }
        
        
        //Test load data from cache
        earthquakeWatcher = new EarthquakeWatcher("cache.json");
        rp = earthquakeWatcher.getEarthquakes("1.0", "hour");
        System.out.println("-----------------From Cache----------------");
        System.out.println("Report Title:"+rp.getTitle()+"\nBBox:"+rp.getArea());

        for(Earthquake eq:rp.getEarthquakes()){
        	System.out.println("Place:"+eq.getLocationDescription());
        	System.out.println("Magnitude:"+eq.getMagnitude());
        	System.out.println("Time:"+eq.getTime());
        	System.out.println("Coordinates:"+eq.getLocation());
        	System.out.println();
        }
        
        
//        /*
        // The following pre-generated code demonstrates how you can
		// use StickyWeb's EditableCache to create data files.
		try {
            // First, you create a new EditableCache, possibly passing in an FileInputStream to an existing cache
			EditableCache recording = new EditableCache();
			
			String[][] requests = {{"1.0","hour"},{"1.0","day"}};
			for (String[] request : requests) {
				System.out.println("threshold:"+request[0]+" time_range:"+request[1]);
				recording.addData(earthquakeWatcher.getEarthquakesRequest(request[0], request[1]));			
			}
            // Then you can save the expanded cache, possibly over the original
			recording.saveToStream(new FileOutputStream("cache.json"));
			
            // You can add a Request object directly to the cache.
			// recording.addData(earthquakeWatcher.getEarthquakesRequest(...));
            // Then you can save the expanded cache, possibly over the original
			recording.saveToStream(new FileOutputStream("cache.json"));
		} catch (StickyWebDataSourceNotFoundException e) {
			System.err.println("The given FileStream was not able to be found.");
		} catch (StickyWebDataSourceParseException e) {
			System.err.println("The given FileStream could not be parsed; possibly the structure is incorrect.");
		} catch (StickyWebLoadDataSourceException e) {
			System.err.println("The given data source could not be loaded.");
		} catch (FileNotFoundException e) {
			System.err.println("The given cache.json file was not found, or could not be opened.");
		}
        // ** End of how to use the EditableCache
        // ** End of how to use the EditableCache
		catch (StickyWebNotInCacheException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StickyWebInternetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StickyWebInvalidQueryString e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StickyWebInvalidPostArguments e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		*/
    }
	
    /**
     * Create a new, online connection to the service
     */
	public EarthquakeWatcher() {
        this.online = true;
		try {
			this.connection = new StickyWeb(null);
		} catch (StickyWebDataSourceNotFoundException e) {
			System.err.println("The given datastream could not be loaded.");
		} catch (StickyWebDataSourceParseException e) {
			System.err.println("The given datastream could not be parsed");
		} catch (StickyWebLoadDataSourceException e) {
			System.err.println("The given data source could not be loaded");
		}
	}
	
    /**
     * Create a new, offline connection to the service.
     * @param cache The filename of the cache to be used.
     */
	public EarthquakeWatcher(String cache) {
        // TODO: You might consider putting the cache directly into the jar file,
        // and not even exposing filenames!
        try {
            this.online = false;
            this.connection = new StickyWeb(new FileInputStream(cache));
        } catch (StickyWebDataSourceNotFoundException e) {
			System.err.println("The given data source could not be found.");
            System.exit(1);
		} catch (StickyWebDataSourceParseException e) {
			System.err.println("Could not read the data source. Perhaps its format is incorrect?");
            System.exit(1);
		} catch (StickyWebLoadDataSourceException e) {
			System.err.println("The given data source could not be read.");
			System.exit(1);
		} catch (FileNotFoundException e) {
			System.err.println("The given cache file could not be found. Make sure it is in the right folder.");
			System.exit(1);
		}
	}
    
    
    /**
     * Retrieves information about earthquakes around the world.
     *
     * This version of the function meant for instructors to capture a
     * StickyWebRequest object which can be put into an EditableCache and then
     * stored to a "cache.json" file.
     * 
     * @param threshold A string indicating what kind of earthquakes to report. Must be either "significant" (only significant earthquakes), "all" (all earthquakes, regardless of significance), "4.5", "2.5", or "1.0". Note that for the last three, all earthquakes at and above that level will be reported.
     * @return a StickyWebRequest
     * @param time A string indicating the time range of earthquakes to report. Must be either "hour" (only earthquakes in the past hour), "day" (only earthquakes that happened today), "week" (only earthquakes that happened in the past 7 days), or "month" (only earthquakes that happened in the past 30 days).
     * @return a StickyWebRequest
     */
    private StickyWebRequest getEarthquakesRequest(String threshold, String time) {
        try {
            /*
            * Perform any user parameter validation here. E.g.,
            * if the first argument can't be zero, or they give an empty string.
            */
            
            // Build up query string
            final String url = String.format("http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/%s_%s.geojson", String.valueOf(threshold), String.valueOf(time));
            
            // Build up the query arguments that will be sent to the server
            HashMap<String, String> parameters = new HashMap<String, String>();
            
            // Build up the list of actual arguments that should be used to
            // create the local cache hash key
            ArrayList<String> indexList = new ArrayList<String>();
            
            
            // Build and return the connection object.
            return connection.get(url, parameters)
                            .setOnline(online)
                            .setIndexes(indexList);
        
        } catch (StickyWebDataSourceNotFoundException e) {
			System.err.println("Could not find the data source.");
		}
        return null;
    }
    
    /**
     * Retrieves information about earthquakes around the world.
    
     * @param threshold A string indicating what kind of earthquakes to report. Must be either "significant" (only significant earthquakes), "all" (all earthquakes, regardless of significance), "4.5", "2.5", or "1.0". Note that for the last three, all earthquakes at and above that level will be reported.
     * @param time A string indicating the time range of earthquakes to report. Must be either "hour" (only earthquakes in the past hour), "day" (only earthquakes that happened today), "week" (only earthquakes that happened in the past 7 days), or "month" (only earthquakes that happened in the past 30 days).
     * @return a Report
     */
	public Report getEarthquakes(String threshold, String time) {
        
        // Should really make these enums
        try {
			StickyWebRequest request =  getEarthquakesRequest(threshold, time);
            return new Report((Map<String, Object>)request.execute().asJSON());
		} catch (StickyWebNotInCacheException e) {
			System.err.println("There is no query in the cache for the given inputs. Perhaps something was mispelled?");
		} catch (StickyWebInternetException e) {
			System.err.println("Could not connect to the web service. It might be your internet connection, or a problem with the web service.");
		} catch (StickyWebInvalidQueryString e) {
			System.err.println("The given arguments were invalid, and could not be turned into a query.");
		} catch (StickyWebInvalidPostArguments e) {
			System.err.println("The given arguments were invalid, and could not be turned into a query.");
        
        } catch (StickyWebJsonResponseParseException e) {
            System.err.println("The response from the server couldn't be understood.");
        
		}
		return null;
	}
    
}
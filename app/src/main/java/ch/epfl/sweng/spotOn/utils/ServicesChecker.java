
package ch.epfl.sweng.spotOn.utils;

import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import ch.epfl.sweng.spotOn.FirebaseConnectionTracker.FirebaseConnectionListener;
import ch.epfl.sweng.spotOn.FirebaseConnectionTracker.ConcreteFirebaseConnectionTracker;
import ch.epfl.sweng.spotOn.FirebaseConnectionTracker.FirebaseConnectionTracker;
import ch.epfl.sweng.spotOn.localObjects.LocalDatabase;
import ch.epfl.sweng.spotOn.localisation.LocationTracker;
import ch.epfl.sweng.spotOn.localisation.LocationTrackerListener;
import ch.epfl.sweng.spotOn.user.UserListener;
import ch.epfl.sweng.spotOn.user.UserManager;

/**
 * Created by quentin on 17.11.16.
 */

public class ServicesChecker implements LocationTrackerListener, UserListener, FirebaseConnectionListener {

    private static ServicesChecker mSingleInstance=null;

    private LocationTracker mLocationTrackerRef;
    private UserManager mUserManagerRef;
    private FirebaseConnectionTracker mFbCoTrackerRef;

    private boolean mAllowedToDisplayToasts;

    // need to keep track of the previous state of a service to detect change in the service availability ( available -> available should not trigger anything, while unavailable -> available should)
    private boolean locationIsValid;
    private boolean userIsLoggedIn;
    private boolean databaseIsConnected;




// INITIALIZE AND CONSTRUCTOR
    public static void initialize(LocationTracker ltref, LocalDatabase ldbref, UserManager userRef, FirebaseConnectionTracker fbCoTrackerRef){
        mSingleInstance = new ServicesChecker(ltref, ldbref, userRef, fbCoTrackerRef);

        mSingleInstance.mLocationTrackerRef.addListener(mSingleInstance);
        mSingleInstance.mUserManagerRef.addListener(mSingleInstance);
        mSingleInstance.mFbCoTrackerRef.addListener(mSingleInstance);
    }

    private ServicesChecker(LocationTracker ltref, LocalDatabase ldbref, UserManager userRef, FirebaseConnectionTracker fbCoTrackerRef){
        if( ltref==null || ldbref==null|| userRef==null){
            // test to enforce that all required singletons are instantiated
            throw new IllegalStateException("Must initialize LocationTracker, LocalDatabase and UserManager first");
        }
        // we keep the LocalDatabase reference in the method prototype, to enforce that ServicesChecker relies on an existing instance of LocalDatabase
        mLocationTrackerRef = ltref;
        mUserManagerRef = userRef;
        mFbCoTrackerRef = fbCoTrackerRef;
        mAllowedToDisplayToasts = true;
        databaseIsConnected = fbCoTrackerRef.isConnected();
        locationIsValid = ltref.hasValidLocation();
        userIsLoggedIn = mUserManagerRef.userIsLoggedIn();
    }




// PUBLIC METHODS
    public static  boolean instanceExists(){
        return mSingleInstance!=null;
    }

    public static ServicesChecker getInstance() {
        if (!instanceExists()) {
            throw new IllegalStateException("ServicesChecker hasn't been initialized yet");
        }
        return mSingleInstance;
    }

    public boolean allServicesOk(){
        // duplicates allowedToPost for new, but I'd like to keep it that way (1) for the abstraction and (2) because it might change later and I'd like to keep the same name
        return databaseIsConnected && mLocationTrackerRef.hasValidLocation() && mUserManagerRef.userIsLoggedIn();
    }

    public void allowDisplayingToasts(boolean allowToDisplayToasts){
        mAllowedToDisplayToasts = allowToDisplayToasts;
    }

    public boolean databaseConnected(){
        return databaseIsConnected;
    }

    public String provideErrorMessage(){
        String errorMessage = "";
        if( ! databaseIsConnected ){
            errorMessage += "Can't connect to the database\n";
        }
        if( ! mLocationTrackerRef.hasValidLocation() ){
            errorMessage += "Can't localize your device\n";
        }
        if( ! mUserManagerRef.userIsLoggedIn() ){
            if( mUserManagerRef.retrievingUserFromDatebase()){
                errorMessage+= "We're processing your login informations\n";
            }else {
                errorMessage += "You're not logged in\n";
            }
        }
        if(!allServicesOk()) {
            errorMessage += "--  Some features will be restricted  --";
        }
        return errorMessage;
    }

    /** provides only the "most important" error message : internet connection > retrieving user information > userLoggedIn
     */
    public String provideLoginErrorMessage(){
        if( ! databaseIsConnected ){
            return "Can't connect to the database";
        }else if( ! mUserManagerRef.userIsLoggedIn() ){
            if( ! mUserManagerRef.getUser().getIsRetrievedFromDB()){
                return "We're processing your login informations";
            }else {
                 return "You're not logged in";
            }
        }
        return "";
    }



// LISTENER METHODS

    @Override
    public void firebaseDatabaseConnected() {
        Log.d("ServicesChecker","database connected");
        if(!databaseIsConnected){ // disconnected -> connected
            databaseIsConnected = true;
            if(mAllowedToDisplayToasts) {
                if (allServicesOk()) {
                    ToastProvider.printOverCurrent("All services are now OK", Toast.LENGTH_SHORT);
                } else {
                    ToastProvider.printOverCurrent(provideErrorMessage(), Toast.LENGTH_LONG);
                }
            }
        }
    }

    @Override
    public void firebaseDatabaseDisconnected() {
        Log.d("ServicesChecker","database disconnected");
        if(databaseIsConnected){ // connected -> connected
            databaseIsConnected = false;
            if(mAllowedToDisplayToasts) {
                ToastProvider.printOverCurrent(provideErrorMessage(), Toast.LENGTH_LONG);
            }
        }
    }

    @Override
    public void updateLocation(Location newLocation) {
        if( ! locationIsValid){         // check for bad -> good transition
            Log.d("ServicesChecker","location status changed : listeners notified");
            locationIsValid = true;
            if(mAllowedToDisplayToasts) {
                printOkMessage();
            }
        }
    }

    @Override
    public void locationTimedOut(Location old) {
        if(locationIsValid){            // check for good -> bad transition
            Log.d("ServicesChecker","location timedout : listeners notified");
            locationIsValid = false;
            if(mAllowedToDisplayToasts) {
                ToastProvider.printOverCurrent(provideErrorMessage(), Toast.LENGTH_LONG);
            }
        }
    }

    @Override
    public void userConnected() {
        if( !userIsLoggedIn ){          // check for bad -> good transition
            Log.d("ServicesChecker","user logged in : listeners notified");
            userIsLoggedIn=true;
            if(mAllowedToDisplayToasts) {
                printOkMessage();
            }
        }
    }

    @Override
    public void userDisconnected() {
        if( userIsLoggedIn ){           // check for good -> bad transition
            Log.d("ServicesChecker","user logged out : listeners notified");
            userIsLoggedIn=false;
            if(mAllowedToDisplayToasts) {
                ToastProvider.printOverCurrent(provideErrorMessage(), Toast.LENGTH_LONG);
            }
        }
    }


// PRIVATE HELPERS
    private void printOkMessage(){
        if(allServicesOk()){
            ToastProvider.printOverCurrent("All services are now OK", Toast.LENGTH_SHORT);
        }
    }
}

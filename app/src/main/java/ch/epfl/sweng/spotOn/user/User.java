package ch.epfl.sweng.spotOn.user;

import android.util.Log;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import ch.epfl.sweng.spotOn.gui.UserProfileActivity;
import ch.epfl.sweng.spotOn.singletonReferences.DatabaseRef;

/*
 * This class corresponds to a User
 * It contains methods to create the user in the database and get the user from the database
 */

public class User {

    private final int INITIAL_KARMA = 0;

    private String mFirstName;
    private String mLastName;
    private String mUserId;
    private long mKarma;


    public User(){} // needed for use of firebase database


    //constructor only used from UserProfile
    public User(String userId, UserProfileActivity userProfile){
        mUserId = userId;

        try {
            getUser(userProfile);
        } catch(AssertionError a){
            a.printStackTrace();
        }
    }


    // constructor used from MainActivity during the login phase
    public User(String firstName, String lastName, String userId) {

        mFirstName = firstName;
        mLastName = lastName;
        mUserId = userId;

        UserId singletonUserId = UserId.getInstance();
        singletonUserId.setUserId(userId);

        checkUser();
    }


    /* Add a new user in the database with its karma instanciated to a arbitrary value*/
    private void createUserInDB(){
        DatabaseReference DBRef = DatabaseRef.getUsersDirectory();
        mKarma = INITIAL_KARMA;
        DBRef.child(mUserId).setValue(this);
    }


    /* Method to check if the user is already defined in the database and if not it creates it */
    private void checkUser(){
        DatabaseReference DBRef = DatabaseRef.getUsersDirectory();
        Query userQuery = DBRef.orderByChild("userId").equalTo(mUserId);

        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot userToRetrieve = dataSnapshot.child(mUserId);  // important
                if (!userToRetrieve.exists()) {
                    createUserInDB();
                }
                else {
                    User retrievedUser = userToRetrieve.getValue(User.class);

                    if (retrievedUser == null) {
                        Log.e("UserProfile Error", "retrievedUser is null");
                    } else {
                        // We can set the fields of User
                        mFirstName = retrievedUser.getFirstName();
                        mLastName = retrievedUser.getLastName();
                        mKarma = retrievedUser.getKarma();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //
                Log.e("Firebase", "error in checkUser", databaseError.toException());
            }
        };

        userQuery.addListenerForSingleValueEvent(userListener);
    }


    /* Method to get the user already defined in the database */
    private void getUser(final UserProfileActivity userProfile) throws AssertionError{
        DatabaseReference DBRef = DatabaseRef.getUsersDirectory();
        Query userQuery = DBRef.orderByChild("userId").equalTo(mUserId);

        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(mUserId == null)
                {
                    throw new AssertionError("User.mUserId is null");
                }
                else {
                    DataSnapshot userToRetrieve = dataSnapshot.child(mUserId);
                    if (!userToRetrieve.exists()) {
                        throw new AssertionError("UserId doesn't exist in the database " + mUserId);
                    } else {
                        User retrievedUser = userToRetrieve.getValue(User.class);

                        if (retrievedUser == null) {
                            Log.e("UserError", "retrievedUser is null");
                        } else {
                            // We can set the fields of User
                            mFirstName = retrievedUser.getFirstName();
                            mLastName = retrievedUser.getLastName();
                            mKarma = retrievedUser.getKarma();
                        }

                        if (userProfile == null) {
                            Log.e("UserError", "userProfile is null");
                        } else {
                            userProfile.fillInFields();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //
                Log.e("Firebase", "error in getUser with UserProfile", databaseError.toException());
            }
        };

        userQuery.addListenerForSingleValueEvent(userListener);

    }


    //PUBLIC GETTERS
    public String getFirstName(){ return mFirstName; }
    public String getLastName(){ return mLastName; }
    public String getUserId(){ return mUserId; }
    public long getKarma() { return mKarma; }


    //PUBLIC SETTERS
    public void setFirstName(String firstName){ mFirstName = firstName; }
    public void setLastName(String lastName){ mLastName = lastName; }
    public void setUserId(String userId){ mUserId = userId; }
    public void setKarma(long karma){ mKarma = karma; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (!mFirstName.equals(user.mFirstName)) return false;
        if (!mLastName.equals(user.mLastName)) return false;
        if (mKarma != user.getKarma()) return false;
        return mUserId.equals(user.mUserId);

    }
}
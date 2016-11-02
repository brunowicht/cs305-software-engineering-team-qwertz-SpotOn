package ch.epfl.sweng.spotOn.test;

import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.junit.Test;
import org.junit.runner.RunWith;

<<<<<<< HEAD
=======
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
>>>>>>> c93d9145c97e3849001d8647feaf89a19e87e5ab
import java.util.NoSuchElementException;
import java.util.Objects;

import ch.epfl.sweng.spotOn.localObjects.LocalDatabase;
import ch.epfl.sweng.spotOn.media.PhotoObject;
import ch.epfl.sweng.spotOn.media.PhotoObjectStoredInDatabase;
import ch.epfl.sweng.spotOn.test.util.PhotoObjectUtils;

import static ch.epfl.sweng.spotOn.test.util.PhotoObjectUtils.areEquals;
import static ch.epfl.sweng.spotOn.test.util.PhotoObjectUtils.getRandomPhotoObject;

/** test the database behaviour with
 *  @author quentin
 */
@RunWith(AndroidJUnit4.class)
public class DatabaseIOTest {

    private final Object lock = new Object();
    private boolean tester = false;

    @Test(expected=AssertionError.class)
    public void objectIsSent_testWaitsForSentCompleted(final PhotoObject testOBject1) throws AssertionError {

        final String testObjectId = testOBject1.getPictureId();
        final boolean testifyListenerCodeWasExecuted = false;
        final DatabaseIOTest refToLock = this;

        testOBject1.upload(true, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                tester=true;
                synchronized(refToLock) {
                    refToLock.notify();
                }
            }
        });

        synchronized (this) {
            this.wait();
        }

        if(testifyListenerCodeWasExecuted) {
            throw new AssertionError("Test made it this far - test succeeded !");
        }
    }

    @Test
    public void gettersWorkCorrectly() throws Exception {
        String imageLink = "bad_link";
        String pictureId = "key1";
        String author = "author1";
        String photoName = "name1";
        long createdDate = 1;
        long expireDate = 1000;
        double latitude = 4.4;
        double longitude = 6.6;
        int radius = 99;
        int votes = 0;
        List<String> voters = new ArrayList<String>();
        voters.add(author);
        PhotoObject photo1 = new PhotoObject(imageLink, null, "key1", author, photoName, createdDate,
                expireDate, latitude, longitude, radius, votes, voters);
        if (photo1.getLongitude() != longitude) {
            throw new AssertionError("longitude wrongly get");
        }
        if (photo1.getLatitude() != latitude) {
            throw new AssertionError("latitude wrongly get");
        }
        if (photo1.getCreatedDate().getTime() != createdDate) {
            throw new AssertionError("created date wrongly get");
        }
        if (photo1.getExpireDate().getTime() != expireDate) {
            throw new AssertionError("expire date wrongly get");
        }
        if (photo1.getRadius() != radius) {
            throw new AssertionError("radius wrongly get");
        }
        if (!photo1.getAuthorId().equals(author)) {
            throw new AssertionError("author wrongly get");
        }
        if (!photo1.getPhotoName().equals(photoName)) {
            throw new AssertionError("photo name wrongly get");
        }
        if (!photo1.getFullsizeImageLink().equals(imageLink)) {
            throw new AssertionError("image link wrongly get");
        }
        if (!photo1.getPictureId().equals(pictureId)) {
            throw new AssertionError("image id wrongly get");
        }
        if (!(photo1.getVotes() == votes)) {
            throw new AssertionError("votes wrongly get");
        }
        if (!(photo1.getVoters().equals(voters))) {
            throw new AssertionError("voters wrongly get");
        }

    }

    /*
    @Test (expected=AssertionError.class)
    public void objectWithWrongPictureIsRejected() throws Exception {
        final PhotoObject testOBject1 = PhotoObjectUtils.paulVanDykPO();
        final String testObjectId = testOBject1.getPictureId();
        testOBject1.upload();

        final PhotoObject testOBject_wrong = PhotoObjectUtils.germaynDeryckePO();

        PhotoObjectUtils.DBref.child(testObjectId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    throw new NoSuchElementException("datasnapshot doesn't exist");
                }
                PhotoObject receivedPhotoOBject = dataSnapshot.getValue(PhotoObjectStoredInDatabase.class).convertToPhotoObject();
                if(!areEquals(receivedPhotoOBject, testOBject_wrong)){
                    throw new AssertionError("testObjects are equal, should be different");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // nada
            }
        });
    */

}

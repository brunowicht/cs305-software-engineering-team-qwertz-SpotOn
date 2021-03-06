package ch.epfl.sweng.spotOn.gui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import ch.epfl.sweng.spotOn.R;
import ch.epfl.sweng.spotOn.localObjects.LocalDatabase;
import ch.epfl.sweng.spotOn.media.PhotoObject;
import ch.epfl.sweng.spotOn.user.UserManager;
import ch.epfl.sweng.spotOn.utils.ToastProvider;

/**
 * Created by Alexis Dewaele on 08/11/2016.
 * Adapter pattern to show fullscreen images that could be swiped like page
 */

public class FullScreenImageAdapter extends PagerAdapter {
    private Activity mActivity;

    private ImageAdapter mRefToImageAdapter;

    private int voteSum=0;
    private TextView mTextView;
    private PhotoObject mCurrentPicture;

    private final static int RESOURCE_IMAGE_DOWNLOADING = R.mipmap.image_downloading;
    private final static int RESOURCE_IMAGE_FAILURE =  R.mipmap.image_failure;
    private final static int RESOURCE_IMAGE_DELETED =  R.mipmap.image_deleted;


    public FullScreenImageAdapter(Activity activity) {
        mActivity = activity;
        mRefToImageAdapter = SeePicturesFragment.getImageAdapter();
        mTextView = (TextView) mActivity.findViewById(R.id.UpvoteTextView);
    }

    @Override
    public int getCount() {
        return mRefToImageAdapter.getCount();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.layout_fullscreen_image, container, false);
        ImageView mViewToSet = (ImageView) viewLayout.findViewById(R.id.fullSizeImageView);
        mViewToSet.setImageResource(RESOURCE_IMAGE_DOWNLOADING);

        if(position >= mRefToImageAdapter.size()){
            throw new ArrayIndexOutOfBoundsException();
        }

        String wantedPicId = mRefToImageAdapter.getIdAtPosition(position);
        if(!LocalDatabase.getInstance().hasKey(wantedPicId)){
            Log.d("FullScreenImageAdapter","Image was deleted from database while viewing, displaying error tile");
            mViewToSet.setImageResource(RESOURCE_IMAGE_DELETED);
        } else {
            PhotoObject mDisplayedMedia = LocalDatabase.getInstance().get(wantedPicId);

            if (mDisplayedMedia.hasFullSizeImage()) {
                Bitmap imageToDisplay = mDisplayedMedia.getFullSizeImage();
                mViewToSet.setImageBitmap(imageToDisplay);
            } else {
                // want these final variable, because the fields of the class may change if we swipe
                final ImageView currentView = mViewToSet;
                final String currentPicId = wantedPicId;
                mDisplayedMedia.retrieveFullsizeImage(true, new OnCompleteListener<byte[]>() {
                    @Override
                    public void onComplete(@NonNull Task<byte[]> retrieveFullSizePicTask) {
                        if (retrieveFullSizePicTask.getException() != null) {
                            currentView.setImageResource(RESOURCE_IMAGE_FAILURE);
                            Log.d("FullScreenImageAdapter", "ERROR : couldn't get fullSizeImage for picture " + currentPicId);
                        } else {
                            Bitmap obtainedImage = BitmapFactory.decodeByteArray(retrieveFullSizePicTask.getResult(), 0, retrieveFullSizePicTask.getResult().length);
                            currentView.setImageBitmap(obtainedImage);
                        }
                    }
                });
            }
            //upvotes
            if (mCurrentPicture != null) {
                voteSum = mCurrentPicture.getUpvotes() - mCurrentPicture.getDownvotes();
            }
        }
        container.addView(viewLayout);
        return viewLayout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
    }

    /**
     * Useful for ViewFullSizeImageActivity to check if the picture the user is looking at is still
     * in the local database
     */
    public String getPicIdAtPosition(int position){
        return mRefToImageAdapter.getIdAtPosition(position);
    }

    public String getPicId(){
        if(mCurrentPicture != null){
            return mCurrentPicture.getPictureId();
        } else {
            throw new NullPointerException("Picture not instantiated, you can't obtain its id");
        }
    }

    public void refreshVoteTextView(int position){
        String wantedPicId = mRefToImageAdapter.getIdAtPosition(position);
        PhotoObject mDisplayedMedia = LocalDatabase.getInstance().get(wantedPicId);
        int votes = mDisplayedMedia.getUpvotes() - mDisplayedMedia.getDownvotes();
        //create a temp String is the ONLY way if you want to correct the lint error
        String textToShow = "" + votes;
        mTextView.setText(textToShow);
    }

    @SuppressWarnings("UnusedParameters")
    public void recordUpvote(View view){
        if(alreadyUpvoted(UserManager.getInstance().getUser().getUserId())){
            vote(0);
        }
        else {
            vote(1);
        }
    }

    @SuppressWarnings("UnusedParameters")
    public void recordDownvote(View view){
        if(alreadyDownvoted(UserManager.getInstance().getUser().getUserId())){
            vote(0);
        }
        else{
            vote(-1);
        }
    }


    private void vote(int vote){
        if(mCurrentPicture==null) {
            throw new NullPointerException("FullScreenImageAdapter : trying to vote on a null media");
        }else{
            String userId = UserManager.getInstance().getUser().getUserId();
            voteSum = mCurrentPicture.getUpvotes() - mCurrentPicture.getDownvotes();
            //fake vote method to have more responsive interface
            Log.d("XD","1: "+voteSum);
            if(vote==1 && !mCurrentPicture.getAuthorId().equals(userId) && !alreadyUpvoted(userId)){
                voteSum++;
                if(alreadyDownvoted(userId)){
                    voteSum++;
                }
            }

            else if(vote==-1 && !mCurrentPicture.getAuthorId().equals(userId) && !alreadyDownvoted(userId)){
                voteSum--;
                if(alreadyUpvoted(userId)){
                    voteSum--;
                }
            }
            else if(vote == 0) {
                if(alreadyUpvoted(userId)){
                    voteSum--;
                }
                else{
                    voteSum++;
                }
            }
            Log.d("XD","2: "+voteSum);
            if(!mCurrentPicture.getAuthorId().equals(userId)) {
                //create a temp String is the ONLY way if you want to correct the lint error
                String textToShow = ""+voteSum;
                mTextView.setText(textToShow);
            }

            String toastMessage = mCurrentPicture.processVote(vote, userId);
            ToastProvider.printOverCurrent(toastMessage, Toast.LENGTH_SHORT);
        }
    }


    public void reportOffensivePicture(){
        if(mCurrentPicture == null) {
            Log.e("FullScreenImageAdapter","reportOffensivePicture mDisplayedMedia is null");
        }else{
            String userId = UserManager.getInstance().getUser().getUserId();
            String toastMessage = mCurrentPicture.processReport(userId);
            ToastProvider.printOverCurrent(toastMessage, Toast.LENGTH_SHORT);
        }
    }

    public void setCurrentMedia(int position) {
        Log.d("Current media position", "" + position);
        String wantedPicId = mRefToImageAdapter.getIdAtPosition(position);
        mCurrentPicture = LocalDatabase.getInstance().get(wantedPicId);
    }

    /**
     * Checks if the user has upvoted the displayed picture
     * @param userID the user ID
     */
    public boolean alreadyUpvoted(String userID){
        if(mCurrentPicture != null) {
            return mCurrentPicture.getUpvotersList().contains(userID);
        } else {
            throw new NullPointerException("The photoObject is null");
        }
    }

    /**
     * Checks if the user has downvoted the displayed picture
     * @param userID the user ID
     */
    public boolean alreadyDownvoted(String userID){
        if(mCurrentPicture != null){
            return mCurrentPicture.getDownvotersList().contains(userID);
        } else {
            throw new NullPointerException("The photoObject is null");
        }
    }

    /**
     * Method useful for the ViewFullSizeImageActivity to make sure the buttons do not change color
     * when the user votes for his own picture
     * @return the author of the displayed picture
     */
    public String getAuthorOfDisplayedPicture(){
        if(mCurrentPicture == null){
            throw new NullPointerException("The photoObject is null: can't retrieve the author ID");
        } else {
            return mCurrentPicture.getAuthorId();
        }
    }
}

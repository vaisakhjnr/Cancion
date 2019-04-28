package com.cancion.ui.activities;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.cancion.R;
import com.cancion.model.Playlist;
import com.cancion.ui.fragments.CameraFragment;
import com.cancion.ui.fragments.HomeFragment;
import com.cancion.ui.fragments.PlayerFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.common.modeldownload.FirebaseRemoteModel;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelOptions;
import com.google.firebase.ml.custom.FirebaseModelOutputs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import cat.lafosca.facecropper.FaceCropper;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "cancion.homefragment";

    public ArrayList<Playlist> playlists = new ArrayList<>();

    public String currentEmotion = null;
    public Playlist currentPlaylist;

    private HomeFragment homeFragment;
    private PlayerFragment playerFragment;
    private Bitmap bitmap;
    public Bitmap rawColorBitmap;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private CameraFragment cameraFragment;
    private FirebaseModelInterpreter firebaseInterpreter;
    private FirebaseModelInputOutputOptions inputOutputOptions;
    private ProgressDialog progressDialog;

    public boolean inPlayerFragment = false;
    public boolean inHomeFragment = false;
    public boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onPause() {
        super.onPause();
        playerFragment.mediaPlayer.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            playerFragment.mediaPlayer.stop();
            playerFragment.mediaPlayer.reset();
            playerFragment.animationView.setVisibility(View.GONE);
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (inPlayerFragment) {
                playerFragment.mediaPlayer.start();
                playerFragment.animationView.setVisibility(View.VISIBLE);
            } else {
                playerFragment.mediaPlayer.reset();
                playerFragment.animationView.setVisibility(View.GONE);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onBackPressed() {
        if (inPlayerFragment) {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_frag_container, homeFragment).commit();
            playerFragment.mediaPlayer.stop();
            inPlayerFragment = false;
        } else if (inHomeFragment) {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_frag_container, cameraFragment).commit();
            inHomeFragment = false;
        } else if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        } else {
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

    public void onPlaylistSelected(Playlist playlist) {
        getSupportFragmentManager().beginTransaction().replace(R.id.main_frag_container, playerFragment).commit();
        this.currentPlaylist = playlist;
    }

    private Bitmap getCroppedFace(Bitmap source) {
        FaceCropper mFaceCropper = new FaceCropper(1f);
        //storeImage(mFaceCropper.getCroppedImage(source));
        mFaceCropper.setMaxFaces(1);
        mFaceCropper.setFaceMinSize(0);
        mFaceCropper.setEyeDistanceFactorMargin(0.1f);
        mFaceCropper.setFaceMarginPx(0);
        return mFaceCropper.getCroppedImage(source);
    }

    private void storeImage(Bitmap image) {
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            Log.d(TAG,
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
    }

    private File getOutputMediaFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + getApplicationContext().getPackageName()
                + "/Files");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName = "MI_" + timeStamp + ".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }



    public void bitmapReady(Bitmap bm) {
        rawColorBitmap = bm;
        bitmap = getCroppedFace(Bitmap.createScaledBitmap(bm, bm.getWidth(), bm.getHeight(), false));
        storeImage(bitmap);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Processing Image");
        progressDialog.show();

        loadModel();
        try {
            createInterpreter();
            float[][][][] input = createBitmap(toGrayscale(bitmap));
            FirebaseModelInputs inputs = new FirebaseModelInputs.Builder()
                    .add(input)  // add() as many input arrays as your model requires
                    .build();
            firebaseInterpreter.run(inputs, inputOutputOptions)
                    .addOnSuccessListener(
                            new OnSuccessListener<FirebaseModelOutputs>() {
                                @Override
                                public void onSuccess(FirebaseModelOutputs result) {
                                    float[][] output = result.getOutput(0);
                                    float[] probabilities = output[0];
                                    float[] percentages = probabilities;
                                    String[] labels = {"angry", "disgust", "fear", "happy", "sad", "surprise", "neutral"};
                                    for (int i = 0; i < probabilities.length; i++) {
                                        String label = labels[i];
                                        String fl = String.format("%1.4f", probabilities[i]);
                                        Log.i("MLKit", String.format("%s: %1.4f", label, probabilities[i]));
                                        percentages[i] = Float.parseFloat(fl);
                                    }
                                    if (percentages[0] > percentages[3] && percentages[0] > percentages[4] && percentages[0] > percentages[6]) {
                                        currentEmotion = "Angry";
                                    } else if (percentages[3] > percentages[0] && percentages[3] > percentages[4] && percentages[3] > percentages[6]) {
                                        currentEmotion = "Happy";
                                    } else if (percentages[4] > percentages[0] && percentages[4] > percentages[3] && percentages[4] > percentages[6]) {
                                        currentEmotion = "Sad";
                                    } else if (percentages[6] > percentages[0] && percentages[6] > percentages[4] && percentages[6] > percentages[3]) {
                                        currentEmotion = "Calm";
                                    } else {
                                        currentEmotion = "None";
                                    }
                                    progressDialog.dismiss();
                                    getSupportFragmentManager().beginTransaction().replace(R.id.main_frag_container, homeFragment).commit();
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    e.printStackTrace();
                                }
                            });

        } catch (FirebaseMLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.setStatusBarColor(Color.WHITE);
        }

        mAuth = FirebaseAuth.getInstance();
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInAnonymously:success");
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            user = currentUser;
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInAnonymously:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            user = null;
                        }
                    }
                });

        homeFragment = new HomeFragment();
        playerFragment = new PlayerFragment();
        cameraFragment = new CameraFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.main_frag_container, cameraFragment).commit();
    }

    public Bitmap convertToBitmap(Drawable drawable, int widthPixels, int heightPixels) {
        Bitmap mutableBitmap = Bitmap.createBitmap(widthPixels, heightPixels, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mutableBitmap);
        drawable.setBounds(0, 0, widthPixels, heightPixels);
        drawable.draw(canvas);

        return mutableBitmap;
    }

    private float[][][][] createBitmap(Bitmap bm) {
        Bitmap bitmap = bm;
        //bitmap = Bitmap.createScaledBitmap(bitmap, 48, 48, true);

        int batchNum = 0;
        float[][][][] input = new float[1][48][48][1];
        for (int x = 0; x < 48; x++) {
            for (int y = 0; y < 48; y++) {
                int pixel = bitmap.getPixel(x, y);

                int R = Color.red(pixel);
                int G = Color.green(pixel);
                int B = Color.blue(pixel);
                int gray = (int) (0.2989 * R + 0.5870 * G + 0.1140 * B);
                input[batchNum][x][y][0] = (Color.blue(pixel)) - 127 / 128.0f;
                Log.d("LUM", "" + ((Color.blue(pixel)) - 127 / 128.0f));
            }
        }
        return input;
    }

    private void createInterpreter() throws FirebaseMLException {
        FirebaseModelOptions options = new FirebaseModelOptions.Builder()
                .setRemoteModelName("expression-detector")
                .build();
        firebaseInterpreter = FirebaseModelInterpreter.getInstance(options);

        inputOutputOptions = new FirebaseModelInputOutputOptions.Builder()
                .setInputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 48, 48, 1})
                .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 7})
                .build();
    }

    private void loadModel() {
        FirebaseModelDownloadConditions.Builder conditionsBuilder =
                new FirebaseModelDownloadConditions.Builder();
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            conditionsBuilder = conditionsBuilder
                    .requireCharging()
                    .requireDeviceIdle();
        }*/
        FirebaseModelDownloadConditions conditions = conditionsBuilder.build();
        FirebaseRemoteModel cloudSource = new FirebaseRemoteModel.Builder("expression-detector")
                .enableModelUpdates(true)
                .setInitialDownloadConditions(conditions)
                .setUpdatesDownloadConditions(conditions)
                .build();
        FirebaseModelManager.getInstance().registerRemoteModel(cloudSource);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        user = currentUser;
    }

    public Bitmap toGrayscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }
}
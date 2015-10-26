package com.google.android.glass.sample.apidemo.cards.imotion;

/**
 * Created by Hector on 10/11/15.
 */

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.sample.apidemo.card.CardAdapter;
import com.google.android.glass.view.WindowUtils;
import com.google.android.glass.widget.CardScrollView;


public final class iMotionActivity extends Activity{
    private CardScrollView mCardScroller;
    private boolean mVoiceMenuEnabled = true;
    private boolean mEyeEnabled = true;
    Fragment eyeControlFragment;

    // Index of the gesture detector demos.
    private static final int DISCRETE = 0;
    private static final int CONTINUOUS = 1;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        //initialize eyeControlFragment
        eyeControlFragment = new EyeControlFragment();



        // Ensure screen stays on during demo.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Sets up a singleton card scroller as content of this activity. Clicking
        // on the card toggles the eyecontrol menu on and off.
        mCardScroller = new CardScrollView(this);
        mCardScroller.setAdapter(new CardAdapter(createCards(this)));
        mCardScroller.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Plays sound.
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                am.playSoundEffect(Sounds.TAP);
                // Toggles eye motion. Invalidates menu to flag change.
                mEyeEnabled = !mEyeEnabled;
                //display coordinates from glass.
                getWindow().invalidatePanelMenu(WindowUtils.FEATURE_VOICE_COMMANDS);
            }
        });
        setContentView(mCardScroller);
    }




}

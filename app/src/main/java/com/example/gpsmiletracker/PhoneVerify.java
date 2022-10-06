package com.example.gpsmiletracker;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class PhoneVerify extends AppCompatActivity {

    TextView postcode;

    TextView verify_click_text, my_number, next_button;

    LinearLayout next, go_to_email, continue_button, verify_buton, verify, phoneinfo, gray_button;

    EditText telephonenum;

    String verficationcodee = "";

    String defaultCountry = "";

    FirebaseAuth aut;
    FirebaseAuth mAuth;

    DatabaseReference databaseReference;

    String verificationID;

    String userCountry = "";

    String userAddress = "";

    Location gps_loc;
    Location network_loc;
    Location final_loc;
    double longitude;
    double latitude;

    private String location = "North America";


    Animation animateo, animationo;

    String searchUp = "";
    String verifyText = "";
    String tryAgain = "";
    String wrongNum = "";

    int disabled = 0;

    private static final int REQ_USER_CONSENT = 200;
    private String mVerificationId = "";
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String postcode_num = "+1";
    String prefernceLang = "";
    private String defaultvar = "";


    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences phoneInfo = getSharedPreferences("shared_preferences", MODE_PRIVATE);

        if(phoneInfo.contains("Country") && phoneInfo.contains("PostCode")) {

            updatePostalCode();

        }

        else {

            SharedPreferences phoneinfo = getSharedPreferences("shared_preferences", MODE_PRIVATE);

            SharedPreferences.Editor ed;

            ed = phoneinfo.edit();

            ed.putString("Country", "United States");

            ed.putString("PostCode", "+1");

            ed.commit();


            updatePostalCode();

        }

    }

    private void updatePostalCode() {


        SharedPreferences phoneInfo = getSharedPreferences("shared_preferences", MODE_PRIVATE);

        if(phoneInfo.contains("Country") && phoneInfo.contains("PostCode")) {

            String countryText = phoneInfo.getString("Country", "");

            defaultCountry = countryText;

            String postcodeText = phoneInfo.getString("PostCode", "");

            String[] words = countryText.split("\\s+");

            String abs = "";

            if(words.length > 1) {

                for(String a : words) {

                    if(a.length() > 1) {

                        abs = abs+a.substring(0, 1);

                    }

                }

            }

            else {

                if(countryText.length() > 2) {

                    abs = abs+countryText.substring(0, 2);

                }

            }


            String cap = abs.toUpperCase();

            postcode.setText(cap+""+postcodeText+"");

            postcode_num = postcodeText;



            telephonenum.setText(postcode_num);
            telephonenum.setSelection(postcode_num.length());


            telephonenum.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    if(charSequence.length() > 0)  {

                        gray_button.setVisibility(View.GONE);
                        verify_buton.setVisibility(View.GONE);
                        continue_button.setVisibility(View.VISIBLE);

                        if(charSequence.length() < postcode_num.length()) {

                            telephonenum.setText(postcode_num);

                            telephonenum.setSelection(postcode_num.length());

                        }

                    }

                    else {

                        continue_button.setVisibility(View.GONE);
                        gray_button.setVisibility(View.VISIBLE);

                    }

                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });


            telephonenum.addTextChangedListener(new PhoneNumberFormattingTextWatcher());


        }



    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.phone_verify);

        aut = FirebaseAuth.getInstance();

        postcode = (TextView) findViewById(R.id.postcodes);

        next = (LinearLayout) findViewById(R.id.on);

        go_to_email = (LinearLayout) findViewById(R.id.to_email_class);


        ImageView backout = (ImageView) findViewById(R.id.backOut);

        backout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(disabled == 0) {

                    finish();

                }

            }
        });



        continue_button = (LinearLayout) findViewById(R.id.on);

        telephonenum = (EditText) findViewById(R.id.numbertext);

        telephonenum.setFocusable(true);
        telephonenum.setFocusableInTouchMode(true);
        telephonenum.requestFocus();




        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        verify_buton = (LinearLayout) findViewById(R.id.verify_num);

        my_number = (TextView) findViewById(R.id.my_number);

        verify_click_text = (TextView) findViewById(R.id.verify_click);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        SharedPreferences languagePreference = getSharedPreferences("shared_preferences", MODE_PRIVATE);

        if(languagePreference.contains("Language")) {

            String lang = languagePreference.getString("Language", "");

            if(!prefernceLang.equals(lang)) {

                translateTextViews(lang);

                prefernceLang = lang;
            }

        }

        verify = (LinearLayout) findViewById(R.id.verify);

        gray_button = (LinearLayout) findViewById(R.id.off);


        verify.setVisibility(View.GONE);


        phoneinfo = (LinearLayout) findViewById(R.id.numberinfo);

        phoneinfo.setVisibility(View.VISIBLE);




        next_button = (TextView) findViewById(R.id.onbuttonn);

        next_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                InputMethodManager imm = (InputMethodManager) telephonenum.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(telephonenum.getWindowToken(), 0);


                String userphonenum =  telephonenum.getText().toString();

                sendVerificationCode(userphonenum);

                disabled = 1;

                continue_button.setVisibility(View.GONE);

                verify_buton.setVisibility(View.VISIBLE);

                start_bounce_animation(verify_click_text);


            }
        });




        LinearLayout code = (LinearLayout) findViewById(R.id.code);

        code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(PhoneVerify.this, ListOfPostCodes.class));

            }
        });



        mAuth = FirebaseAuth.getInstance();





    }




    private void askPermission() {


        try {


            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


            if (ActivityCompat.checkSelfPermission(PhoneVerify.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(PhoneVerify.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(PhoneVerify.this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {

                return;
            }

            try {

                gps_loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                network_loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (gps_loc != null) {
                final_loc = gps_loc;
                latitude = final_loc.getLatitude();
                longitude = final_loc.getLongitude();
            }
            else if (network_loc != null) {
                final_loc = network_loc;
                latitude = final_loc.getLatitude();
                longitude = final_loc.getLongitude();
            }
            else {
                latitude = 0.0;
                longitude = 0.0;
            }


            verify_click_text.clearAnimation();

            animationo = null;


            // Intent toThenNext = new Intent(MainActivity.this, .class);
            // toThenNext.putExtra("type", "emailVerification");

            // startActivity(toThenNext);


        }


        catch (Exception e) {

            e.printStackTrace();

        }


    }



    private void translateTextViews(String lang) {

        if(lang.equals("Bengali/Bangla")) {

            lang = "BENGALI";

        }

        else if(lang.equals("Chinese (Simplified)")) {

            lang = "CHINESE_SIMPLIFIED";

        }

        else if(lang.equals("Chinese (Traditional)")) {

            lang = "CHINESE_TRADITIONAL";

        }

        else {

            lang = lang.toUpperCase();

        }


        if(!lang.equals("ENGLISH")) {

            databaseReference.child("Languages").child("PhonePage").child(lang+"").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if(snapshot.exists()) {

                        for(DataSnapshot translations : snapshot.getChildren()) {

                            switch(translations.getKey().toString()) {

                                case "0":

                                    String whatNum = translations.getValue().toString();

                                    TextView yourNum = (TextView) findViewById(R.id.whatNum);

                                    yourNum.setText(whatNum);


                                    break;

                                case "1":

                                    String infoOnText = translations.getValue().toString();

                                    TextView infoText = (TextView) findViewById(R.id.textInfo);

                                    infoText.setText(infoOnText);


                                    break;

                                case "2":

                                    searchUp = translations.getValue().toString();


                                    break;

                                case "3":

                                    String continueText = translations.getValue().toString();


                                    TextView onbuttonnText = (TextView) findViewById(R.id.onbuttonn);

                                    onbuttonnText.setText(continueText);


                                    TextView offbuttontext = (TextView) findViewById(R.id.off_button);

                                    offbuttontext.setText(continueText);

                                    break;

                                case "4":

                                    verifyText = translations.getValue().toString();

                                    verify_click_text.setText(verifyText+"");

                                    break;

                                case "5":

                                    tryAgain = translations.getValue().toString();

                                    break;

                                case "6":

                                    wrongNum = translations.getValue().toString();

                                    break;

                            }

                        }



                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        }

        else {


            TextView yourNum = (TextView) findViewById(R.id.whatNum);

            yourNum.setText("What's Your Number?");


            TextView infoText = (TextView) findViewById(R.id.textInfo);

            infoText.setText("The next step is to send a text message with a verification code. Message and data rates may apply. The verified phone number can be used to login.");


            TextView onbuttonnText = (TextView) findViewById(R.id.onbuttonn);

            onbuttonnText.setText("Continue");


            TextView offbuttontext = (TextView) findViewById(R.id.off_button);

            offbuttontext.setText("Continue");

            verify_click_text.setText("Verifying..");


        }

    }

    private void sendVerificationCode(String s) {

        try {


            PhoneAuthOptions optionsz = PhoneAuthOptions.newBuilder(aut)
                    .setPhoneNumber(s+"")       // Phone number to verify
                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                    .setActivity(this)                 // Activity (for callback binding)
                    .setCallbacks(mCallback)
                    .build();
            PhoneAuthProvider.verifyPhoneNumber(optionsz);



        }

        catch(Exception e) {


        }

    }


    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

            final String code = phoneAuthCredential.getSmsCode();

            if(code != null) {

                verifyCode(code);

            }

        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {

            if(e.getLocalizedMessage().toString().equals("We have blocked all requests from this device due to unusual activity. Try again later.") || e.getLocalizedMessage().toString().equals("This project's quota for this operation has been exceeded. [ Exceeded per phone number quota for sending verification codes. ]")) {


            }

            else {

                if(wrongNum.equals("")) {

                    Toast.makeText(PhoneVerify.this, "WRONG NUMBER.", Toast.LENGTH_LONG).show();

                   // Toast.makeText(PhoneVerify.this, e.getLocalizedMessage()+"", Toast.LENGTH_LONG).show();

                }
                else {

                    Toast.makeText(PhoneVerify.this, wrongNum+"", Toast.LENGTH_LONG).show();

                }




            }



            verify_click_text.clearAnimation();

            animationo = null;


            verify_buton.setVisibility(View.GONE);

            continue_button.setVisibility(View.VISIBLE);


            disabled = 0;

            if(tryAgain.equals("")) {

                next_button.setText("Try Again.");


            }

            else {

                next_button.setText(tryAgain);

            }


            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(telephonenum, InputMethodManager.SHOW_IMPLICIT);


        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            verificationID = s;

        }
    };



    private void verifyCode(String codez) {

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationID, codez);


        signInByCredentials(credential, codez);

    }


    private void signInByCredentials(PhoneAuthCredential credential, String codez) {

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()) {


                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    stop_bounce_animation(next_button);

                    next_button.setText("Continue");

                    Toast.makeText(PhoneVerify.this, "Your User ID is:  "+aut.getUid().toString(), Toast.LENGTH_SHORT).show();

                    verify_click_text.setText("Complete");

                    databaseReference.child("User").child(aut.getUid().toString()).child("id").setValue(aut.getUid()+"");

                    askPermission();


                }

                else {


                }


            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {


            }
        })
        ;



    }




    private void printCode(String codez) {


        // Creating array of string length
        char[] ch = new char[codez.length()];

        // Copy character by character into array
        for (int i = 0; i < codez.length(); i++) {
            ch[i] = codez.charAt(i);
        }


        TextView code1, code2, code3, code4, code5, code6;

        code1 = (TextView) findViewById(R.id.code1);
        code2 = (TextView) findViewById(R.id.code2);
        code3 = (TextView) findViewById(R.id.code3);
        code4 = (TextView) findViewById(R.id.code4);
        code5 = (TextView) findViewById(R.id.code5);
        code6 = (TextView) findViewById(R.id.code6);


        for(int index = 0; index < ch.length; index++) {

            char code = ch[index];

            switch(index) {

                case 0:

                    code1.setText(code+"");

                    break;

                case 1:

                    code2.setText(code+"");

                    break;

                case 2:

                    code3.setText(code+"");

                    break;

                case 3:

                    code4.setText(code+"");

                    break;

                case 4:

                    code5.setText(code+"");

                    break;

                case 5:

                    code6.setText(code+"");

                    break;


            }

        }



    }


    public void start_bounce_animation(TextView textView) {

        animationo = AnimationUtils.loadAnimation(PhoneVerify.this, R.anim.bouncingg);

        MyBounceinterpolator myBounceinterpolator = new MyBounceinterpolator(0.2, 20);

        animationo.setInterpolator(myBounceinterpolator);


        if(animationo != null) {

            textView.startAnimation(animationo);

        }


    }


    public void stop_bounce_animation(TextView textView) {


        textView.clearAnimation();

        animationo = null;


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 200) {

            if((resultCode == RESULT_OK) && data != null) {


                String message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE);

                getOtpFromMessage(message);



            }

        }

    }



    private void getOtpFromMessage(String message) {


        Pattern optPattern = Pattern.compile("(|^)\\d(6)");

        Matcher matcher = optPattern.matcher(message);

        if(matcher.find()) {


        }

    }
}

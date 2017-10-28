package com.bytasaur.smartaffix;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class SigninActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText idBox;
    private EditText phoneBox;
    private Button verify;
    private String deviceId;
    private ProgressBar spinner;
    private DatabaseReference reference;
    private Handler handler=new Handler();
    private Runnable runnable=new Runnable() {
        @Override
        public void run() {
            reference.removeEventListener(checkId);
            enableBoxes();
            Toast.makeText(getApplicationContext(), "Timed Out!", Toast.LENGTH_SHORT).show();
            handler.removeCallbacks(runnable);
        }
    };
    private ValueEventListener checkId=new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if(dataSnapshot.exists()) {
                Toast.makeText(getApplicationContext(), "Signed in to "+deviceId, Toast.LENGTH_SHORT).show();
                handler.removeCallbacks(runnable);
                MainActivity.device=deviceId;
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
            else {
                auth.signOut();
                Toast.makeText(getApplicationContext(), deviceId+" not found", Toast.LENGTH_LONG).show();
                handler.removeCallbacks(runnable);
                enableBoxes();
            }
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {
//                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            Toast.makeText(getApplicationContext(), "Database Error", Toast.LENGTH_SHORT).show();
            handler.removeCallbacks(runnable);
            enableBoxes();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        spinner=(ProgressBar)findViewById(R.id.loadingSpinner);
        spinner.setWillNotDraw(true);

        auth=FirebaseAuth.getInstance();
        FirebaseUser tmp=auth.getCurrentUser();
        deviceId=getPreferences(0).getString("d_id", null);
        idBox =(EditText)findViewById(R.id.id_box);
        phoneBox =(EditText)findViewById(R.id.phone_box);
        phoneBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i==EditorInfo.IME_ACTION_DONE) {
                    verify(phoneBox);
                    return true;
                }
                return false;
            }
        });
        verify=(Button)findViewById(R.id.btn_verify);

        if(tmp!=null) {
            if(deviceId!=null) {
                idBox.setText(deviceId);
                disableBoxes();
                goToMain();
            }
            else {
                auth.signOut();
                phoneBox.setText(tmp.getPhoneNumber());
            }
        }
        else {
            if(deviceId!=null) {
                idBox.setText(deviceId);
            }
            String p=getPreferences(0).getString("p_id", null);
            if(p!=null) {
                phoneBox.setText(p);
            }
        }
    }

    private void goToMain() {
        Toast.makeText(getApplicationContext(), "Checking connection with "+deviceId+"...", Toast.LENGTH_SHORT).show(); // Considering replacing Toasts with Snackbars
        reference=FirebaseDatabase.getInstance().getReference(deviceId);
        reference.addListenerForSingleValueEvent(checkId);
        handler.postDelayed(runnable, 13000);
    }

    private void signIn(PhoneAuthCredential credential) {
        disableBoxes();
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    //Toast.makeText(getApplicationContext(), FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber(), Toast.LENGTH_LONG).show();
                    goToMain();
                }
                else {
                    enableBoxes();
                    Toast.makeText(getApplicationContext(), task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void verify(final View v) {
        disableBoxes();
        if(TextUtils.isEmpty(idBox.getText().toString())) {
            idBox.setError("* Required");
            return;
        }
        deviceId=idBox.getText().toString();
        getPreferences(0).edit().putString("d_id", deviceId).apply();
        String phone=phoneBox.getText().toString();
        if(TextUtils.isEmpty(phone)) {
            phoneBox.setError("* Required");
            return;
        }
        getPreferences(0).edit().putString("p_id", phone).apply();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phone, 60, TimeUnit.SECONDS, this, new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            AlertDialog alertDialog;
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                Toast.makeText(getApplicationContext(), "Auto-Captured code", Toast.LENGTH_LONG).show();
                signIn(phoneAuthCredential);
//                if(alertDialog!=null)
                // Set edit text with code
                alertDialog.dismiss();
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCodeSent(final String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                enableBoxes();
                Toast.makeText(getApplicationContext(), "Code sent", Toast.LENGTH_SHORT).show();
                final EditText codeBox=new EditText(getApplicationContext());
                alertDialog=new AlertDialog.Builder(v.getContext()).setTitle("Enter the x-digit code").setView(codeBox).setCancelable(false)
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Verify", null).create();
                alertDialog.show();
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String code=codeBox.getText().toString();
                        if(TextUtils.isEmpty(code)) {
                            codeBox.setError("* Required");
                            return;
                        }
                        PhoneAuthCredential credential=PhoneAuthProvider.getCredential(verificationId, code);
                        signIn(credential);
                        alertDialog.dismiss();
                    }
                });
            }
        });
    }

    private void enableBoxes() {
        spinner.setWillNotDraw(true);
        verify.setEnabled(true);
        phoneBox.setEnabled(true);
        idBox.setEnabled(true);
    }

    private void disableBoxes() {
        spinner.setWillNotDraw(false);
        verify.setEnabled(false);
        phoneBox.setEnabled(false);
        idBox.setEnabled(false);
    }

//    private void setBoxes(boolean enable) {   // Using seperate functions instead for potential(maybe?) compiler optimisation(inlining)
//        phoneBox.setEnabled(enable);
//        verify.setEnabled(false);
//        pwdBox.setEnabled(enable);
//        idBox.setEnabled(enable);
//    }
}

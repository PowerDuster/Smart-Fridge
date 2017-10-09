package com.bytasaur.smartaffix;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SigninActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText idBox;
    private EditText emailBox;
    private EditText pwdBox;
    private String deviceId;
    private ProgressBar spinner;
//    private Task<AuthResult> task;

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
        emailBox =(EditText)findViewById(R.id.email_box);
        pwdBox =(EditText)findViewById(R.id.pwd_box);

        if(tmp!=null) {
            if(deviceId!=null) {
                if(tmp.isEmailVerified()) {
                    emailBox.setText(tmp.getEmail());
                    idBox.setText(deviceId);
                    goToMain();
                    Toast.makeText(getApplicationContext(), "Checking connection with "+deviceId+"...", Toast.LENGTH_SHORT).show();
                }
                else {
                    auth.signOut();
                    Toast.makeText(getApplicationContext(), "Email not verified!", Toast.LENGTH_LONG).show();
                }
            }
            else {
                auth.signOut();
                emailBox.setText(tmp.getEmail());
            }
        }
        else if(deviceId!=null) {
            idBox.setText(deviceId);
        }
    }

    private void goToMain() {
        disableBoxes();
        FirebaseDatabase.getInstance().getReference(deviceId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    Toast.makeText(getApplicationContext(), "Signed in to "+deviceId, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class).putExtra("devid", deviceId));
                    finish();
                }
                else {
                    auth.signOut();
                    Toast.makeText(getApplicationContext(), deviceId+" not found", Toast.LENGTH_LONG).show();
                    enableBoxes();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                enableBoxes();
            }
        });
    }

    public void signIn(View v) {
        if(TextUtils.isEmpty(idBox.getText().toString())) {
            idBox.setError("* Required");
            return;
        }
        deviceId=idBox.getText().toString();
        getPreferences(0).edit().putString("d_id", deviceId).apply();
        if(TextUtils.isEmpty(emailBox.getText().toString())) {
            emailBox.setError("* Required");
            return;
        }
        if(TextUtils.isEmpty(pwdBox.getText().toString())) {
            pwdBox.setError("* Required");
            return;
        }
        auth.signOut();
        disableBoxes();
        auth.signInWithEmailAndPassword(emailBox.getText().toString(), pwdBox.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    if(auth.getCurrentUser().isEmailVerified()) {
                        goToMain();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Email not verified!", Toast.LENGTH_LONG).show();
                        auth.signOut();
                        enableBoxes();
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    enableBoxes();
                }
            }
        });
    }


    public void registerClicked(View v) {
        // CHECK IF COMPILER WOULD INLINE IT AND MAKE A FUNCTION!!
        if(TextUtils.isEmpty(idBox.getText().toString())) {
            idBox.setError("* Required");
            return;
        }
        deviceId=idBox.getText().toString();
        getPreferences(0).edit().putString("d_id", deviceId).apply();
        final String email=emailBox.getText().toString();
        if(TextUtils.isEmpty(email)) {
            emailBox.setError("* Required");
            return;
        }
        final String passwd=pwdBox.getText().toString();
        if(TextUtils.isEmpty(passwd)) {
            pwdBox.setError("* Required");
            return;
        }
        final EditText confirmBox=new EditText(this);
        confirmBox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        final AlertDialog alertDialog=new AlertDialog.Builder(this).setTitle("Register").setView(confirmBox)
                .setPositiveButton("Confirm", null).setNegativeButton("Cancel", null).create();
        alertDialog.show(); // Slight chance of completing show before ClickListener is overridden, don't matter
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dialText=confirmBox.getText().toString();
                if(TextUtils.isEmpty(dialText)) {
                    confirmBox.setError("* Required");
                }
                else if(dialText.equals(passwd)) {
                    register(email, passwd);
                    alertDialog.dismiss();
                }
                else {
                    confirmBox.setError("* Passwords don't match");
                }
            }
        });
    }


    private void register(String email, String passwd) {
        auth.signOut();
        disableBoxes();
        auth.createUserWithEmailAndPassword(email, passwd).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                enableBoxes();
                if(task.isSuccessful()) {
                    auth.getCurrentUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getApplicationContext(), "Verification email sent!", Toast.LENGTH_LONG).show();
                            auth.signOut();
                        }
                    });
                }
                else {
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void enableBoxes() {
        spinner.setWillNotDraw(true);
        emailBox.setEnabled(true);
        pwdBox.setEnabled(true);
        idBox.setEnabled(true);
    }

    private void disableBoxes() {
        spinner.setWillNotDraw(false);
        emailBox.setEnabled(false);
        pwdBox.setEnabled(false);
        idBox.setEnabled(false);
    }

//    private void setBoxes(boolean enable) {   // Using seperate functions for potential(maybe?) compiler optimisation
//        emailBox.setEnabled(enable);
//        pwdBox.setEnabled(enable);
//        idBox.setEnabled(enable);
//    }
}

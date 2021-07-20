package com.example.projecteesa;

import android.content.Context;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projecteesa.ProfileSection.Profile;
import com.example.projecteesa.utils.ActivityProgressDialog;
import com.example.projecteesa.utils.Constants;
import com.example.projecteesa.utils.MotionToastUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;

public class SignUpActivity extends AppCompatActivity {
    EditText mName;
    EditText mPassword;
    EditText mEmail;
    EditText mPhoneNum;
    Button mCreate;
    FirebaseAuth mAuth;
    CheckBox visibleSignIn;
    private Spinner branchSpinner;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference users = db.collection("Users");

    private Context mContext = this;
    private ActivityProgressDialog progressDialog;

    private EditText linkedinProfileEdit;
    private EditText passingYearEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        getSupportActionBar().hide();
        progressDialog = new ActivityProgressDialog(mContext);
        progressDialog.setCancelable(false);
        mAuth = FirebaseAuth.getInstance();
        mName = (EditText) findViewById(R.id.create_name);
        mEmail = (EditText) findViewById(R.id.create_email);
        visibleSignIn = (CheckBox) findViewById(R.id.sign_up_checkbox);
        mPassword = (EditText) findViewById(R.id.create_password);
        mPhoneNum = (EditText) findViewById(R.id.create_phone_num);
        mCreate = (Button) findViewById(R.id.create);
        branchSpinner = findViewById(R.id.branch_spinner);
        linkedinProfileEdit = findViewById(R.id.linkedin_profile_url_edit);
        passingYearEdit = findViewById(R.id.passing_year_edit);
        ArrayAdapter<String> branchAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Constants.BRANCH_ARRAY);
        branchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        branchSpinner.setAdapter(branchAdapter);
        visibleSignIn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    mPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                } else {
                    mPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });

        mCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        final String name = mName.getText().toString().trim();
        final String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();
        final String phoneNum = mPhoneNum.getText().toString().trim();
        final String linkedinUrl = linkedinProfileEdit.getText().toString().trim();
        final String passingYear = passingYearEdit.getText().toString().trim();
        Date date = new Date();
        int currentYear = date.getYear() + 1900;
        if (name.equals("")) {
            MotionToastUtils.showErrorToast(mContext, "Email required", "Please enter your email address");
        } else if (email.isEmpty()) {
            MotionToastUtils.showErrorToast(mContext, "Email required", "Please enter your email address");
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            MotionToastUtils.showErrorToast(mContext, "Invalid email", "Please enter a valid email address");
        } else if (password.isEmpty()) {
            MotionToastUtils.showErrorToast(mContext, "Password is empty", "Please enter a valid password");
        } else if (password.length() < 6) {
            MotionToastUtils.showErrorToast(mContext, "Password too short", "Please enter a password of length greater than 6");
        } else if (phoneNum.isEmpty()) {
            MotionToastUtils.showErrorToast(mContext, "Phone number empty", "Please enter your phone number");
        } else if (!Patterns.PHONE.matcher(phoneNum).matches()) {
            MotionToastUtils.showErrorToast(mContext, "Invalid phone number", "Please enter a valid phone number");
        } else if (passingYear.length() < 4) {
            MotionToastUtils.showErrorToast(mContext, "Invalid passing year", "Please enter a passing year");
        } else if (currentYear + 4 < Integer.parseInt(passingYear)) {
            MotionToastUtils.showErrorToast(mContext, "Invalid passing year", "Please enter a valid passing year");
        } else {
            progressDialog.setTitle("Creating account");
            progressDialog.setMessage("Please wait while we sign you up");
            progressDialog.showDialog();
            mAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    progressDialog.setTitle("Saving data");
                    progressDialog.setMessage("Please wait while we upload your data");
                    FirebaseUser user = authResult.getUser();


                    user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            add(name, email, phoneNum, user, passingYear, linkedinUrl);
                            progressDialog.hideDialog();
                            MotionToastUtils.showInfoToast(mContext, "Verify your email", "Signup was successful but please make sure to verify your email");
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            MotionToastUtils.showErrorToast(mContext, "Error occurred", "Some error occurred during signup");
                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.hideDialog();
                    MotionToastUtils.showErrorToast(mContext, "Error occurred", "Some error occurred during signup");
                    e.printStackTrace();
                }
            });
        }

    }

    void add(String name, String email, String phoneNum, FirebaseUser user, String passingYear, String linkedinUrl) {
        ArrayList<String> savedPosts = new ArrayList<>();
        String branch = Constants.BRANCH_ARRAY[branchSpinner.getSelectedItemPosition()];
        Profile profile = new Profile(name, "Member of NITP Family", phoneNum, savedPosts, Integer.parseInt(passingYear), branch, linkedinUrl, user.getUid(), email);
        String uid = user.getUid();
        users.document(uid + "").set(profile).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(SignUpActivity.this, "data added", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Toast.makeText(SignUpActivity.this, "error!!!!" + e, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
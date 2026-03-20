package com.moodtune.app.auth;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.moodtune.app.R;
import com.moodtune.app.home.HomePageActivity;
import com.moodtune.app.models.UserModel;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseFirestore db;
    private ActivityResultLauncher<Intent> signInLauncher;
    private static final String PREFS_NAME = "MoodTunePrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    Button btnGoogleSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Check if user is already signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is already signed in, navigate directly to home page
            Log.d("LoginActivity", "User already signed in, navigating to home page");
            navigateToHomePage();
            return;
        }

        // Check SharedPreferences for login persistence
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);
        if (isLoggedIn && currentUser != null) {
            navigateToHomePage();
            return;
        }

        // Initialize Activity Result Launcher
        signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Log.d("LoginActivity", "Sign-in result received. Result code: " + result.getResultCode());
                    Intent data = result.getData();
                    
                    // Only process if we have data and result is OK
                    if (result.getResultCode() == RESULT_OK && data != null) {
                        Log.d("LoginActivity", "Processing Google Sign-In result");
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        
                        // Use addOnCompleteListener to handle both success and failure
                        task.addOnCompleteListener(this, completedTask -> {
                            if (completedTask.isSuccessful()) {
                                try {
                                    GoogleSignInAccount account = completedTask.getResult(ApiException.class);
                                    if (account != null) {
                                        Log.d("LoginActivity", "Google Sign-In successful for: " + account.getEmail());
                                        if (account.getIdToken() != null) {
                                            firebaseAuthWithGoogle(account.getIdToken());
                                        } else {
                                            Log.e("LoginActivity", "ID Token is null");
                                            Toast.makeText(this, "Google sign-in failed: ID token is null", Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Log.e("LoginActivity", "GoogleSignInAccount is null");
                                        Toast.makeText(this, "Google sign-in failed: Account is null", Toast.LENGTH_LONG).show();
                                    }
                                } catch (ApiException e) {
                                    Log.e("LoginActivity", "Error getting account from result", e);
                                    handleSignInError(e);
                                }
                            } else {
                                // Task failed - check if it's an ApiException
                                Exception exception = completedTask.getException();
                                if (exception instanceof ApiException) {
                                    handleSignInError((ApiException) exception);
                                } else {
                                    Log.e("LoginActivity", "Sign-in failed with unknown error", exception);
                                    Toast.makeText(this, "Google sign-in failed: " + (exception != null ? exception.getMessage() : "Unknown error"), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    } else {
                        // Result code is not OK or data is null
                        if (result.getResultCode() == RESULT_CANCELED) {
                            Log.d("LoginActivity", "User cancelled the sign-in");
                            // Don't show toast for user cancellation
                        } else {
                            Log.e("LoginActivity", "Sign-in failed. Result code: " + result.getResultCode() + ", Data: " + (data != null ? "present" : "null"));
                            Toast.makeText(this, "Google sign-in failed. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Configure Google Sign-In
        try {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
            
            btnGoogleSignIn.setOnClickListener(v -> signIn());
            
            Log.d("LoginActivity", "Google Sign-In configured successfully");
        } catch (Exception e) {
            Log.e("LoginActivity", "Error configuring Google Sign-In", e);
            Toast.makeText(this, "Error setting up Google Sign-In: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void signIn() {
        try {
            if (mGoogleSignInClient != null) {
                Log.d("LoginActivity", "Starting Google Sign-In");
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                signInLauncher.launch(signInIntent);
            } else {
                Log.e("LoginActivity", "GoogleSignInClient is null");
                Toast.makeText(this, "Google Sign-In not properly configured", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e("LoginActivity", "Error starting Google Sign-In", e);
            Toast.makeText(this, "Error starting Google Sign-In: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        Log.d("LoginActivity", "Starting Firebase authentication with Google");
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user == null) {
                            Log.e("LoginActivity", "Firebase user is null after successful sign-in");
                            Toast.makeText(this, "Authentication failed: User is null", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Log.d("LoginActivity", "Firebase authentication successful for user: " + user.getEmail());

                        // Save login state
                        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, true).apply();

                        // Check if user exists in Firestore
                        db.collection("users").document(user.getUid())
                                .get()
                                .addOnCompleteListener(userTask -> {
                                    if (userTask.isSuccessful()) {
                                        DocumentSnapshot document = userTask.getResult();
                                        if (document.exists()) {
                                            // Existing user - go to home page
                                            Log.d("LoginActivity", "Existing user found, navigating to home page");
                                            navigateToHomePage();
                                        } else {
                                            // New user - create profile and go to home page
                                            Log.d("LoginActivity", "New user, creating profile");
                                            UserModel userModel = new UserModel(user.getUid(), user.getDisplayName(), user.getEmail());
                                            db.collection("users").document(user.getUid())
                                                    .set(userModel)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Log.d("LoginActivity", "User profile created successfully");
                                                        Toast.makeText(this, "Welcome " + user.getDisplayName() + "!", Toast.LENGTH_SHORT).show();
                                                        navigateToHomePage();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.e("LoginActivity", "Failed to create user profile", e);
                                                        // Still navigate to home page even if profile creation fails
                                                        Toast.makeText(this, "Profile creation failed, but you can still use the app", Toast.LENGTH_SHORT).show();
                                                        navigateToHomePage();
                                                    });
                                        }
                                    } else {
                                        // Firestore check failed - navigate anyway since Firebase Auth succeeded
                                        Log.e("LoginActivity", "Failed to check user in Firestore", userTask.getException());
                                        Toast.makeText(this, "Could not verify user profile, but you can still use the app", Toast.LENGTH_SHORT).show();
                                        navigateToHomePage();
                                    }
                                });
                    } else {
                        Log.e("LoginActivity", "Firebase authentication failed", task.getException());
                        String errorMessage = "Authentication Failed.";
                        if (task.getException() != null) {
                            errorMessage += " " + task.getException().getMessage();
                        }
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void handleSignInError(ApiException e) {
        Log.e("GoogleSignIn", "signInResult:failed code=" + e.getStatusCode(), e);
        String errorMessage = "Google sign-in failed";
        int statusCode = e.getStatusCode();
        
        if (statusCode == GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
            errorMessage = "Sign-in was cancelled";
            // Don't show toast for cancellation
            return;
        } else if (statusCode == GoogleSignInStatusCodes.SIGN_IN_FAILED) {
            errorMessage = "Sign-in failed. Please try again";
        } else if (statusCode == GoogleSignInStatusCodes.SIGN_IN_CURRENTLY_IN_PROGRESS) {
            errorMessage = "Sign-in is already in progress";
        } else if (statusCode == 10) {
            // DEVELOPER_ERROR - usually means SHA-1 fingerprint not registered or OAuth client misconfigured
            errorMessage = "Configuration error. Please check Firebase setup and SHA-1 fingerprint.";
            Log.e("LoginActivity", "DEVELOPER_ERROR: Check that SHA-1 fingerprint is registered in Firebase Console");
        } else {
            errorMessage = "Google sign-in failed (Error " + statusCode + ")";
        }
        
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private void navigateToHomePage() {
        Log.d("LoginActivity", "Navigating to HomePageActivity");
        try {
            Intent intent = new Intent(LoginActivity.this, HomePageActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            Log.d("LoginActivity", "Successfully navigated to HomePageActivity");
        } catch (Exception e) {
            Log.e("LoginActivity", "Error navigating to HomePageActivity", e);
            Toast.makeText(this, "Error navigating to home page: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}


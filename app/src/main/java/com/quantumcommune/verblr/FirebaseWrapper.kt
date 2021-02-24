package com.quantumcommune.verblr

import android.app.Activity
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class FirebaseWrapper(private val activity: Activity) {

    private val auth = Firebase.auth

    fun ensureUser(completion : (user : FirebaseUser?, err : Exception?) -> Unit)
    {
        if(auth.currentUser != null)
        {
            completion(auth.currentUser, null)
        }
        else
        {
            signInAnon(completion)
        }
    }

    fun signInAnon(completion : (user : FirebaseUser?, err : Exception?) -> Unit)
    {
        auth.signInAnonymously().addOnCompleteListener(activity) { task ->
            if(task.isSuccessful)
            {
                val user = auth.currentUser
                completion(user, null)
            }
            else
            {
                completion(null, Exception(task.exception?.localizedMessage ?: "Something went wrong"))
            }
        }
    }

    fun signInWithToken(token : String, completion : (user : FirebaseUser?, err : Exception?) -> Unit)
    {
        auth.signInWithCustomToken(token).addOnCompleteListener(activity) { task ->
            if(task.isSuccessful)
            {
                val user = auth.currentUser
                completion(user, null)
            }
            else
            {
                completion(null, Exception(task.exception?.localizedMessage ?: "Something went wrong"))
            }
        }
    }

    fun signOut(completion : (err : Exception?) -> Unit)
    {
        try {
            auth.signOut()
        }
        catch(e : Exception)
        {
            completion(e)
            return
        }

        completion(null)
    }
}
package com.example.mojaaplikacjaver2

import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var auth:FirebaseAuth
    private lateinit var user: User
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val todayText = findViewById<TextView>(R.id.textToday)

        auth=FirebaseAuth.getInstance()
        val firebaseUser=auth.currentUser
        val uid=firebaseUser?.uid   // odczytujemy jakie id ma zalogowany user

        val db= Firebase.firestore  // inicjowanie CloudFirestore (z dokumentacji firebase)
        val document=uid?.let { db.collection("users").document(it) }
        document?.let {doc->
            GlobalScope.launch(Dispatchers.IO) {
                val userDocument=doc.get().await().toObject(User::class.java)    // konwersja pobranego z firestore dokumentu do odpowiadającej mu classy
                withContext(Dispatchers.Main){
                    if (userDocument!=null){
                        user=userDocument
                         Log.d("userData", user.toString())
                    }
                }
            }
        }
        val today = Calendar.getInstance().time
        val df = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val dateString = df.format(today)
        todayText.text = dateString
    }
}
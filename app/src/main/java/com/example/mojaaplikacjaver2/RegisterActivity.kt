package com.example.mojaaplikacjaver2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth                     // klient uwierzytelniania
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val textEmail=findViewById<EditText>(R.id.RegisterEmail)
        val textPassword=findViewById<EditText>(R.id.RegisterPassword)
        val textPasswordRepeat=findViewById<EditText>(R.id.RegisterPasswordRepeat)
        val buttonRegister=findViewById<Button>(R.id.ButtonRegister)
        auth=FirebaseAuth.getInstance()                         // połączenie do firebase, odpowiedzialne za uwierzytelnianie użytkownika
        buttonRegister.setOnClickListener{              // nasłuchiwanie kliknięcia przycisku rejestracji
            val email=textEmail.text.toString()                 // odczyty wartosci pól tekstowych
            val password=textPassword.text.toString()
            val repeatPassword=textPasswordRepeat.text.toString()
            if (email.isNotEmpty()&&password.isNotEmpty()&&repeatPassword.isNotEmpty()){  // sprawdzam czy podane sa wszystkie dane
                if (password==repeatPassword){
                    auth.createUserWithEmailAndPassword(email, password)                   // zlecenie do firebase stworzenie użytkownika
                        .addOnCompleteListener(this){                   // oczekiwanie az firebase stworzy użytkownika
                          if (it.isSuccessful){
                              Toast.makeText(
                                  baseContext,
                                  "Rejestracja udana",
                                  Toast.LENGTH_SHORT
                              ).show()
                              val db = Firebase.firestore
                              auth.currentUser?.let { it1 -> db.collection("users").document(it1.uid).set(
                                  User(
                                      listOf()
                                  )
                              ) }  // tworzymy dokument o id ktore przypisane jest do użytkownika podczas rejestracji
                          } else {
                              Toast.makeText(
                                  baseContext,
                                  "Rejestracja nieudana",
                                  Toast.LENGTH_SHORT
                              ).show()
                          }
                        }
                } else{
                    Toast.makeText(
                        baseContext,
                        "Powtórzone hasło nie jest takie samo jak hasło",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    baseContext,
                    "Nie podano wszystkich danych",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    public override fun onBackPressed() {
        startActivity(Intent(this, LoginActivity::class.java))  // cofnięcie do logowania
        super.onBackPressed()
    }
}
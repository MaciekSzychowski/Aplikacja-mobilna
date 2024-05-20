package com.example.mojaaplikacjaver2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var auth:FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val textEmail=findViewById<EditText>(R.id.LoginEmail)
        val textPassword=findViewById<EditText>(R.id.LoginPassword)
        val buttonLogin=findViewById<Button>(R.id.ButtonLogin)
        val goToRegister=findViewById<TextView>(R.id.GoToRegister)
        goToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))  // uruchonie tryby rejestracji i wyłaczenie logowania
            finish()
        }
        auth=FirebaseAuth.getInstance()
        buttonLogin.setOnClickListener {        // jak klikniemy przycisk logowania odczytuje wartosci
            val email=textEmail.text.toString()
            val password= textPassword.text.toString()
            if (email.isNotEmpty()&&password.isNotEmpty()){
                auth.signInWithEmailAndPassword(email, password)        // proba logowania z firebase
                    .addOnCompleteListener(this){
                        if (it.isSuccessful){
                            startActivity(Intent(this, MainActivity::class.java))  //
                            finish()

                        }else{
                            Toast.makeText(
                                baseContext,
                                "Logowanie nie udane",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    }
            }else{
                Toast.makeText(
                    baseContext,
                    "Podaj email i hasło",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
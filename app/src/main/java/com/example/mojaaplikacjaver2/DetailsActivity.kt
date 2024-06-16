package com.example.mojaaplikacjaver2

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DetailsActivity : AppCompatActivity() {               // aktywnosc wywietlajaca podsumowanie dawek
    override fun onCreate(savedInstanceState: Bundle?) {        //
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val data = intent.extras        // nasze dodatkowe wartosci
        if(data!= null){
            val totalDose = data.getDouble("totalDose")         // odczytywanie wartowsci przekazanych podczas uruchomienia aktywnosci
            val predictedDate = data.getString("predictedDate")
            val totalDoseField = findViewById<TextView>(R.id.doseTotal)     // dostęp do elementow interfejsu
            val predictedDateField = findViewById<TextView>(R.id.predictedDate)
            totalDoseField.text = totalDose.toString()                      // ustawienie textu na ustawione wartosci
            predictedDateField.text = predictedDate
        }else {
            startActivity(Intent(this, MainActivity::class.java))       // zabezpieczenie, na wypadek gdyby nie przekazano danych do tej aktywnosci odpalamy ponownie main activity
            finish()
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java))       // zabezpieczenie, na wypadek gdyby nie przekazano danych do tej aktywnosci odpalamy ponownie main activity
        finish()
        super.onBackPressed()
    }
}
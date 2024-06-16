package com.example.mojaaplikacjaver2

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
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
    private lateinit var auth:FirebaseAuth //pole do ktorego przypiszemy obiekt obslugujący uwierzytelnianie za pomoća firebase (linijka 38 - pozyskiwanie za pomocą metody get instance)
    private lateinit var user: User       // pole do przechowywanie danych aktualnie zalogowanego użytkownika ( od 45 linikji), lateinit, pozwala tymczasowo wstrzymać z deklaracją zmiennych
    private lateinit var recyclerView: RecyclerView  // pole recyckler
    private lateinit var adapterClass: AdapterClass // pole dla adapeterclass
    @RequiresApi(Build.VERSION_CODES.O)             // nakaz zeby urzadzenie mobilne mialo wersje androida przynajmniej 8.0 - automatycznie dodane
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        createNotificationChannel()                // wywolanie funkcji tworzacych kanal dostarczania powiadomien
        scheduleNotification()                     // wywolanie funkcji kiedy powinno wyskoczyc powiadomienie
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val todayText = findViewById<TextView>(R.id.textToday) // odnajdywanie pola z dzisiejszą datą
        val textDoseDate= findViewById<EditText>(R.id.fieldDoseDate) // odnajddywanie pola z data przyjecia dawki
        val textDoseValue = findViewById<EditText>(R.id.fieldDoseValue) // odnajdywanie pola z wartosci dawki
        val buttonAddDose = findViewById<Button>(R.id.buttonAddDose )  // znajdywanie przycisku dodawania dawki
        val buttonDetails = findViewById<Button>(R.id.GoToDetails)      // guzik do podsumowania
        recyclerView = findViewById(R.id.recyvlerView)
        recyclerView.setHasFixedSize(true)              // scrolowanie
        recyclerView.layoutManager = LinearLayoutManager(this)  // https://developer.android.com/develop/ui/views/layout/recyclerview?hl=pl


        auth=FirebaseAuth.getInstance()                 // odczytujemy jakie id ma zalogowany user (dokumenty w firebase maja takie samo id jak zalogowany uzytkownik)
        val firebaseUser=auth.currentUser
        val uid=firebaseUser?.uid

        val db= Firebase.firestore  // inicjowanie CloudFirestore (z dokumentacji firebase) (db = database)
        val document=uid?.let { db.collection("users").document(it) }  // jak dostać się do naszego dokumentu w firestore , zabezpieczenie automatycznie proponowane przez android
        document?.let {doc->                                                          // czyli jesli document nie jest nullem to zostanie wykonana funkcja w klamrze i nasz dokument bedzie sie nazwyal doc
            GlobalScope.launch(Dispatchers.IO) {
                val userDocument=doc.get().await().toObject(User::class.java)    // konwersja pobranego z firestore dokumentu do odpowiadającej mu classy
                withContext(Dispatchers.Main){                  // to wykona sie dopiero gdy zostanie wykonana linijka powyzej
                    if (userDocument!=null){                                    // sprawdzamy czy pobrany dok nie jest nullem, czyli czy pobraly sioe jakies sensowne dane
                        user=userDocument
                        adapterClass = AdapterClass(user.dawki as ArrayList<Dawka>)     // tworzymy adapter na podstawie listy dawek zaladowanych z firebase
                        adapterClass.onDeleClick = {
                            val newList = (user.dawki as ArrayList<Dawka>).filter { dawka -> !dawka.equals(it)  }  // do nowej listy trafi kazda dawka z dotychczasowej listy ktora nie jest rowna kliknietnej dawce z kafelka  https://www.baeldung.com/kotlin/finding-element-in-list
                            doc.update("dawki", newList).addOnCompleteListener {
                                if(it.isSuccessful){
                                    Toast.makeText(baseContext, "Poprawnie usunięto element", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(baseContext, MainActivity::class.java))        // odpalanie ponowne main acivity w celu odswiezenia widoku po zmianach
                                    finish()
                                } else{
                                    Toast.makeText(baseContext, "Nie udało się usunąć elementu", Toast.LENGTH_SHORT).show()
                                }

                            }
                        }
                        recyclerView.adapter = adapterClass         // podpiecie adaptera do recyclera

                    }
                }
            }
        }
        val today = Calendar.getInstance().time
        val dateFormat= SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())         // wyswietlanie aktualnej daty
        val dateString = dateFormat.format(today)        // https://stackoverflow.com/questions/4590957/how-to-set-text-in-an-edittext
        todayText.text = dateString
        textDoseDate.setText(dateString) // wartosc domyslna pola daty otrzymania dawki, automatycznie przypisuje dzisiejsza date

        textDoseDate.setOnClickListener {       // klikajac na pole textowe, klikamy i otwieramy pole wyboru daty
            val c = Calendar.getInstance()              // https://www.geeksforgeeks.org/datepicker-in-android/
            val day = c.get(Calendar.DAY_OF_MONTH)
            val month = c.get(Calendar.MONTH)
            val year = c.get(Calendar.YEAR)
            val datePickerDialog = DatePickerDialog(
                this,
                {view,year,monthOfYear, dayOfMonth->
                    textDoseDate.setText(dayOfMonth.toString()+"."+(monthOfYear+1).toString()+"."+year.toString()) // konwersja dnia miesiąca do stringa
                },
                year,
                month,
                day
            )
            datePickerDialog.show()         // wyswietlanie okienka z kalendarzem do wyboru

        }

        buttonAddDose.setOnClickListener {
            val dose = textDoseValue.text.toString().toDouble()         //odczytywanie dawki z naszego pola wpisania dawki i daty
            val date = textDoseDate.text.toString()
            val sdf = SimpleDateFormat("dd.MM.yyyy")
            val dateObject = sdf.parse(date)        // ze stringa tworzymy date wg formatu z linijki wyzej
            val timestamp = Timestamp(dateObject)   // przekonwertowanie obiektu do formatu firebase, https://firebase.google.com/docs/reference/kotlin/com/google/firebase/Timestamp
            val doseObject = Dawka(timestamp, dose)  // obiekt ktory chcemy dodac do listy w firebase
            val id = auth.currentUser!!.uid         // !! nie jest nullem
            val db = Firebase.firestore
            val userRef = db.collection("users").document(id)       // wybranie dokumentu zalogowanego uzytkownika
            userRef.update("dawki", FieldValue.arrayUnion(doseObject)).addOnCompleteListener{  // arrayunion dodanie do istniejacej w firestore listy nowego elementu
                if (it.isSuccessful){
                    startActivity(Intent(this, MainActivity::class.java))  // restart ekranu zeby bylo widac nasza zmiane
                    finish()
                } else{
                    Toast.makeText(this, "Dodanie dawki nie udane", Toast.LENGTH_SHORT).show()
                }
            }
        }

        buttonDetails.setOnClickListener {                                      // obliczasnie sumarycznej dawki
            val intent = Intent(this, DetailsActivity::class.java)
            var totalDose =0.0
            for (dawka in user.dawki!!){
                totalDose+= dawka.dawka     // += dodanie do dotychczasowej wartosci zmiennej

            }
            val date = Calendar.getInstance().time
            var dateFormat= SimpleDateFormat("DDD", Locale.getDefault())   // , odczytywanie ile dni w roku minelo, https://docs.oracle.com/javase%2F8%2Fdocs%2Fapi%2F%2F/java/text/SimpleDateFormat.html
            val daysElapsed = Integer.parseInt(dateFormat.format(date))     // ze stringa reprezentującego ile uplynelo dni od poczatku roku robimy liczbe calkowita
            val daysPredicted = (20*daysElapsed/totalDose).toInt()                   // po ilu dniach roku otrzymamy dawke graniczna + konwersja do liczby calkowitej
            val difference = daysPredicted-daysElapsed                  // roznica w dniach, ktore zajmie nam osiadniecie dawki granicznej
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DATE, difference)                         // do odebcnej daty dodajemy roznice w dniach
            dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val dateString = dateFormat.format(calendar.time)
            intent.putExtra("totalDose", totalDose)             // dodatkowe wartosci przekazane do nowego ekranu
            intent.putExtra("predictedDate", dateString)
            startActivity(intent)
            finish()



        }

        
    }

    @RequiresApi(Build.VERSION_CODES.O)     // chat gpt
    fun createNotificationChannel(){
        var name = "monthlyReminder"
        var description = "Co miesięczne przypomnienie o wpisaniu dawek do aplikacji"
        var importance = NotificationManager.IMPORTANCE_HIGH
        var channel = NotificationChannel(name, name, importance)
        channel.description = description
        var manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
    fun sendNotification(){
        val builder = NotificationCompat.Builder(
            this, "monthlyReminder").setContentTitle("Przypomnienie").setContentText("Wprowadź dawki otrzymane w tym miesiącu").setPriority(NotificationCompat.PRIORITY_DEFAULT)

       val notificationManager = NotificationManagerCompat.from(this)  // tworzenie obiektu managera powiadowmien
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notificationManager.notify(1001, builder.build())
    }

    fun scheduleNotification(){
        var intent = Intent(this, AlarmReceiver::class.java)
        var pending = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        var alarmMenager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 14)
        calendar.set(Calendar.HOUR_OF_DAY, 22)
        calendar.set(Calendar.MINUTE, 20)
        calendar.set(Calendar.SECOND,0)

        val triggerAt = calendar.timeInMillis

        if(calendar.before(Calendar.getInstance())){
            calendar.add(Calendar.MONTH, 1)
        }
        alarmMenager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAt, AlarmManager.INTERVAL_DAY *30, pending)
    }

}


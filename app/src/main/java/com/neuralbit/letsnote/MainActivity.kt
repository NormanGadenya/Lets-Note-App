package com.neuralbit.letsnote

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.neuralbit.letsnote.databinding.ActivityMainBinding
import com.neuralbit.letsnote.ui.allNotes.AllNotesViewModel
import com.neuralbit.letsnote.ui.archived.ArchivedViewModel
import com.neuralbit.letsnote.ui.tag.TagViewModel
import com.neuralbit.letsnote.utilities.AlertReceiver
import com.neuralbit.letsnote.utilities.DatabaseBackUp
import java.io.File
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val TAG = "MainActivity"
    private val allNotesViewModal : AllNotesViewModel by viewModels()
    private val archivedViewModel : ArchivedViewModel by viewModels()
    private val tagViewModel : TagViewModel by viewModels()
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mStorageReference : StorageReference
    var firebaseUserID : String? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        firebaseUserID= mAuth.currentUser?.uid
        if (firebaseUserID == null) {

            val intent = Intent(applicationContext, SignInActivity::class.java)
            startActivity(intent)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_arch , R.id.nav_tags ,R.id.nav_labels , R.id.nav_deleted
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onStart() {
        super.onStart()
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, DatabaseBackUp::class.java)
        intent.putExtra("fUserID",firebaseUserID)
        val c = Calendar.getInstance()
//        c[Calendar.HOUR_OF_DAY] = 3
        val pendingIntent = PendingIntent.getBroadcast(this, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.setRepeating(AlarmManager.RTC,c.timeInMillis,AlarmManager.INTERVAL_FIFTEEN_MINUTES,pendingIntent)
//        if (firebaseUser != null){
//            val dbPath = resources.getString(R.string.dbFileLoc)
//            val dbPathShm = resources.getString(R.string.dbShmFileLoc)
//            val dbPathWal = resources.getString(R.string.dbWalFileLoc)
//
//            val dbFile: Uri = Uri.fromFile(File(dbPath))
//            val dbShmFile = Uri.fromFile(File(dbPathShm))
//            val dbWalFile = Uri.fromFile(File(dbPathWal))
//            val firebaseStorage = FirebaseStorage.getInstance()
//            mStorageReference = firebaseUser?.let { firebaseStorage.reference.child(it) }!!
//
//            val dbFileRef = mStorageReference.child("letsNoteDB1")
//            val dbShmFileRef = mStorageReference.child("letsNoteDB1-shm")
//            val dbWalFileRef = mStorageReference.child("letsNoteDB1-wal")
//            dbFileRef.putFile(dbFile).addOnProgressListener{
//                Log.d(TAG, "onCreate: ${it.bytesTransferred}")
//            }
//            dbShmFileRef.putFile(dbShmFile).addOnProgressListener{
//                Log.d(TAG, "onCreate: ${it.bytesTransferred}")
//            }
//            dbWalFileRef.putFile(dbWalFile).addOnProgressListener{
//                Log.d(TAG, "onCreate: ${it.bytesTransferred}")
//            }
//        }


    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.main_activity2, menu)
        val searchViewMenuItem = menu.findItem(R.id.search)
        val searchView = searchViewMenuItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                if (p0 != null) {
                    allNotesViewModal.searchQuery.value = p0
                    archivedViewModel.searchQuery.value = p0
                    tagViewModel.searchQuery.value = p0
                }
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                if (p0 != null) {
                    allNotesViewModal.searchQuery.value = p0
                    archivedViewModel.searchQuery.value = p0
                    tagViewModel.searchQuery.value = p0

                }
                return false
            }
        })

        return true
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
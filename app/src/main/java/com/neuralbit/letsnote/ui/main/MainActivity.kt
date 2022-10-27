package com.neuralbit.letsnote.ui.main

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.ads.MobileAds
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.databinding.ActivityMainBinding
import com.neuralbit.letsnote.firebase.entities.NoteFire
import com.neuralbit.letsnote.receivers.AlertReceiver
import com.neuralbit.letsnote.ui.allNotes.AllNotesViewModel
import com.neuralbit.letsnote.ui.archived.ArchivedViewModel
import com.neuralbit.letsnote.ui.deletedNotes.DeletedNotesViewModel
import com.neuralbit.letsnote.ui.label.LabelViewModel
import com.neuralbit.letsnote.ui.signIn.SignInActivity
import com.neuralbit.letsnote.ui.tag.TagViewModel


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val allNotesViewModal : AllNotesViewModel by viewModels()
    private val archivedViewModel : ArchivedViewModel by viewModels()
    private val deleteVieModel : DeletedNotesViewModel by viewModels()
    private lateinit var viewModal : MainActivityViewModel
    private val tagViewModel : TagViewModel by viewModels()
    private val labelViewModel : LabelViewModel by viewModels()
    private lateinit var mAuth: FirebaseAuth
    private var actionMode : ActionMode? = null
    private var fUser : FirebaseUser? = null
    private lateinit var settingsPref: SharedPreferences
    private val lifecycleOwner = this
    private var useLocalStorage = false
    val TAG = "MAIN"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        fUser= mAuth.currentUser
        settingsPref =  getSharedPreferences("Settings", MODE_PRIVATE)
        useLocalStorage = settingsPref.getBoolean("useLocalStorage",false)
        if (fUser == null && !useLocalStorage ) {
            val intent = Intent(applicationContext, SignInActivity::class.java)
            startActivity(intent)
        }
        Log.d(TAG, "onCreate: $useLocalStorage")
        MobileAds.initialize(this@MainActivity)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_todo,
                R.id.nav_arch,
                R.id.nav_tags,
                R.id.nav_labels,
                R.id.nav_deleted,
                R.id.nav_settings
            ), drawerLayout
        )
        viewModal = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[MainActivityViewModel::class.java]
        viewModal.useLocalStorage = useLocalStorage
        allNotesViewModal.useLocalStorage = useLocalStorage
        deleteVieModel.useLocalStorage = useLocalStorage
        archivedViewModel.useLocalStorage = useLocalStorage
        tagViewModel.useLocalStorage = useLocalStorage
        labelViewModel.useLocalStorage = useLocalStorage
        viewModal.refresh.value = true

        allNotesViewModal.itemSelectEnabled.observe(this){
            if (it){
                actionMode = startSupportActionMode(MActionModeCallBack())
            }else{
                actionMode?.finish()

            }
        }

        // set reminders when a user has been signed in since alarms are cancelled when signed out


        val signedIn = intent.getBooleanExtra("Signed in",false)
        allNotesViewModal.signedIn = signedIn
        if (signedIn){
            lifecycleScope.launchWhenStarted {

                allNotesViewModal.getAllFireNotes().observe(lifecycleOwner){

                    allNotesViewModal.allFireNotes.value = it

                    for (noteFire in it) {

                        if (!noteFire.archived && noteFire.deletedDate==(0).toLong() && noteFire.reminderDate> System.currentTimeMillis()){
                            startAlarm(noteFire, noteFire.reminderDate.toInt())
                        }

                    }


                }
            }
        }





        val emptyTrashImmediately = settingsPref.getBoolean("EmptyTrashImmediately",false)
        if (emptyTrashImmediately){
            allNotesViewModal.notesToDelete.observe(lifecycleOwner){
                Log.d(TAG, "onCreate: ")
                it.noteUid?.let { uid -> viewModal.deleteNote(uid,it.label,it.tags) }
            }
            allNotesViewModal.selectedNotes.clear()
            viewModal.refresh.value = true

        }
        archivedViewModel.notesToRestore.observe(lifecycleOwner){
            val update = HashMap<String,Any>()
            update["title"] = it.title
            update["description"] = it.description
            update["label"] = it.label
            update["pinned"] = it.pinned
            update["reminderDate"] = it.reminderDate
            update["protected"] = it.protected
            update["archived"] = false
            update["timeStamp"] = System.currentTimeMillis()
            it.noteUid?.let { it1 -> viewModal.updateFireNote(update, it1) }
            viewModal.refresh.value = true
        }

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val profileUrl = fUser?.photoUrl
        val headerLayout = navView.getHeaderView(0)
        val profileIV = headerLayout.findViewById<ImageView>(R.id.profilePic)
        val nameTV = headerLayout.findViewById<TextView>(R.id.accountName)
        val emailTV = headerLayout.findViewById<TextView>(R.id.emailAddress)
        val detailsGroup = headerLayout.findViewById<View>(R.id.detailsGroup)
        val backUpStatusIV = headerLayout.findViewById<ImageView>(R.id.backupStatusIcon)
        val backUpStatusTV = headerLayout.findViewById<TextView>(R.id.backupStatusTV)
        if(profileUrl != null){
            Glide.with(applicationContext).load(profileUrl).into(profileIV)
        }


        val name = fUser?.displayName
        if (name != null){
            nameTV.text = name
        }

        val emailAdd = fUser?.email
        if (emailAdd != null){
            emailTV.text = emailAdd
        }
        if (fUser?.isAnonymous == true || useLocalStorage){
            detailsGroup.visibility = GONE
            backUpStatusTV.text = resources.getString(R.string.back_up_deactivated)
        }else{
            detailsGroup.visibility = VISIBLE
            backUpStatusTV.text = resources.getString(R.string.back_up_active)
            backUpStatusIV.setImageResource(R.drawable.ic_baseline_backup_24)
        }

        mAuth.addAuthStateListener {
            val currentUser = it.currentUser
            if (currentUser?.isAnonymous == false){
                detailsGroup.visibility = VISIBLE
                emailTV.text = currentUser.email
                nameTV.text = currentUser.displayName
                viewModal.useLocalStorage = false
                allNotesViewModal.useLocalStorage = false
                archivedViewModel.useLocalStorage = false
                deleteVieModel.useLocalStorage = false
                tagViewModel.useLocalStorage = false
                labelViewModel.useLocalStorage = false
                backUpStatusIV.setImageResource(R.drawable.ic_baseline_backup_24)
                backUpStatusTV.text = resources.getString(R.string.back_up_active)
                if (currentUser.photoUrl !=null){
                    Glide.with(applicationContext).load(currentUser.photoUrl).into(profileIV)
                }
            }
        }

    }




    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_activity2, menu)
        val searchViewMenuItem = menu.findItem(R.id.search)
        val layoutViewBtn = menu.findItem(R.id.layoutStyle)
        val signOutButton = menu.findItem(R.id.signOut)
        val deleteButton = menu.findItem(R.id.trash)
        val searchView = searchViewMenuItem.actionView as SearchView
        val deleteAndSignOut = menu.findItem(R.id.deleteAndSignOut)
        val searchIcon = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_button)
        searchIcon.setImageDrawable(ContextCompat.getDrawable(applicationContext,
            R.drawable.ic_baseline_search_24
        ))
        if (fUser == null){
            deleteAndSignOut.isVisible = false
            signOutButton.isVisible = false
        }else{
            deleteAndSignOut.isVisible = true
            signOutButton.isVisible = true
        }
        mAuth.addAuthStateListener {
            val currentUser = it.currentUser
            if (currentUser?.isAnonymous == false){
                deleteAndSignOut.isVisible = true
                signOutButton.isVisible = true
            }
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                if (p0 != null) {
                    allNotesViewModal.searchQuery.value = p0
                    archivedViewModel.searchQuery.value = p0
                    tagViewModel.searchQuery.value = p0
                    labelViewModel.searchQuery.value = p0
                }
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                if (p0 != null) {
                    allNotesViewModal.searchQuery.value = p0
                    archivedViewModel.searchQuery.value = p0
                    tagViewModel.searchQuery.value = p0
                    labelViewModel.searchQuery.value = p0

                }
                return false
            }
        })


        deleteButton.setOnMenuItemClickListener {
            if (deleteVieModel.deletedNotes.isNotEmpty()){
                val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
                alertDialog.setTitle(resources.getString(R.string.general_warning_message))
                alertDialog.setPositiveButton(resources.getString(R.string.yes)
                ) { _, _ ->
                    deleteVieModel.clearTrash.value = true
                    viewModal.refresh.value = true

                }
                alertDialog.setNegativeButton(resources.getString(R.string.cancel)
                ) { dialog, _ -> dialog.cancel() }
                alertDialog.show()
            }
            return@setOnMenuItemClickListener true
        }

        layoutViewBtn.setOnMenuItemClickListener {
            allNotesViewModal.staggeredView.value = !allNotesViewModal.staggeredView.value!!
            return@setOnMenuItemClickListener true
        }


        signOutButton.setOnMenuItemClickListener {
            val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
            if(fUser?.isAnonymous == true){
                alertDialog.setTitle(resources.getString(R.string.logout_anonymous_message))
            }else{
                alertDialog.setTitle(resources.getString(R.string.general_warning_message))
            }
            alertDialog.setPositiveButton(resources.getString(R.string.yes)
            ) { _, _ ->

                AuthUI.getInstance()
                    .signOut(this@MainActivity)
                    .addOnCompleteListener{
                        val intent = Intent(this@MainActivity, SignInActivity::class.java)
                        startActivity(intent)
                    }
            }

            alertDialog.setNegativeButton(resources.getString(R.string.cancel)
            ) { dialog, _ -> dialog.cancel() }
            alertDialog.show()
            return@setOnMenuItemClickListener true
        }
        deleteAndSignOut.setOnMenuItemClickListener {
            val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
            alertDialog.setTitle(resources.getString(R.string.delete_and_sign_out_alert))
            alertDialog.setPositiveButton(resources.getString(R.string.yes)
            ) { _, _ ->
                val editor : SharedPreferences.Editor= settingsPref.edit()
                editor.clear()
                editor.apply()
                viewModal.deleteUserDataContent()
                Toast.makeText(applicationContext,resources.getString(R.string.delete_and_sign_out_message), Toast.LENGTH_SHORT).show()
                AuthUI.getInstance()
                    .signOut(this@MainActivity)
                    .addOnCompleteListener{
                        val intent = Intent(this@MainActivity, SignInActivity::class.java)
                        startActivity(intent)
                    }
            }

            alertDialog.setNegativeButton(resources.getString(R.string.cancel)
            ) { dialog, _ -> dialog.cancel() }
            alertDialog.show()
            return@setOnMenuItemClickListener true
        }
        allNotesViewModal.staggeredView.observe(this){
            if (layoutViewBtn != null){
                if (it){
                    layoutViewBtn.setIcon(R.drawable.ic_baseline_table_rows_24)
                    layoutViewBtn.title = resources.getString(R.string.list_layout)
                }else{
                    layoutViewBtn.setIcon(R.drawable.baseline_grid_view_24)
                    layoutViewBtn.title = resources.getString(R.string.grid_layout)

                }
            }
        }


        return true
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val a = Intent(Intent.ACTION_MAIN)
        a.addCategory(Intent.CATEGORY_HOME)
        a.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(a)
    }

    private fun startAlarm(note : NoteFire, requestCode: Int) {

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this@MainActivity, AlertReceiver::class.java)
        intent.putExtra("noteTitle",note.title)
        intent.putExtra("noteUid",note.noteUid)
        intent.putExtra("noteDesc", note.description)
        intent.putExtra("timeStamp",System.currentTimeMillis())
        intent.putExtra("labelColor",note.label)
        intent.putExtra("pinned",note.pinned)
        intent.putExtra("archieved",false)
        intent.putExtra("protected",note.protected)
        val tags = ArrayList<String>()
        tags.addAll(note.tags)
        intent.putStringArrayListExtra("tagList", tags)
        intent.putExtra("noteType","Edit")

        val pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, note.reminderDate, pendingIntent)
    }

    private inner class MActionModeCallBack : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode?.menuInflater?.inflate(R.menu.action_menu,menu)
            mode?.title = resources.getString(R.string.delete_or_archive_notes)
            if(allNotesViewModal.deleteFrag.value == true){
                mode?.title = resources.getString(R.string.delete_or_restore_notes)
            }
            else if(allNotesViewModal.archiveFrag){
                mode?.title = resources.getString(R.string.delete_or_restore_notes)
            }
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            val restoreItem = menu?.findItem(R.id.restore)
            val archiveItem = menu?.findItem(R.id.archive)

            if (allNotesViewModal.deleteFrag.value == true){
                archiveItem?.isVisible = false
                restoreItem?.isVisible = true

            }
            else if (allNotesViewModal.archiveFrag){
                archiveItem?.isVisible = false
                restoreItem?.isVisible = true

            }else{
                archiveItem?.isVisible = true
                restoreItem?.isVisible = false
            }


            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            when (item?.itemId) {
                R.id.archive -> {
                    allNotesViewModal.itemArchiveClicked.value = true
                    mode?.finish()
                    return true
                }
                R.id.restore -> {
                    if (allNotesViewModal.deleteFrag.value == true){
                        deleteVieModel.itemRestoreClicked.value = true
                    }else{
                        archivedViewModel.itemRestoreClicked.value = true

                    }
                    mode?.finish()
                    return true
                }
                R.id.delete -> {
                    if(allNotesViewModal.deleteFrag.value == true){
                        deleteVieModel.itemDeleteClicked.value = true
                    }else if (allNotesViewModal.archiveFrag){
                        archivedViewModel.itemDeleteClicked.value = true
                    } else{
                        allNotesViewModal.itemDeleteClicked.value = true
                    }


                    mode?.finish()
                    return true
                }
                else -> return false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            allNotesViewModal.itemSelectEnabled.value = false
            allNotesViewModal.selectedNotes.clear()
            actionMode = null
        }

    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
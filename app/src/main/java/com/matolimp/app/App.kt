package com.matolimp.app

import android.app.Application
import android.content.Context
import com.matolimp.app.data.AppDatabase
import com.matolimp.app.data.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class App : Application() {

    lateinit var repository: Repository
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.get(this)
        repository = Repository(db.contentDao(), db.progressDao(), this)
        appScope.launch {
            runCatching { repository.seedIfNeeded() }
                .onFailure { android.util.Log.e("MatOlimp", "Seed failed", it) }
        }
    }

    companion object {
        fun repo(context: Context): Repository =
            (context.applicationContext as App).repository
    }
}

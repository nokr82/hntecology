package hntecology.ecology.base

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import hntecology.ecology.R

class LoadingActivity : RootActivity() {

    internal var hideLoadingReceiver: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            finish()
            overridePendingTransition(0, 0);
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_loading)

        val filter1 = IntentFilter("HIDE_LOADING")
        registerReceiver(hideLoadingReceiver, filter1)

    }

    override fun onDestroy() {
        super.onDestroy()

        if(hideLoadingReceiver != null) {
            unregisterReceiver(hideLoadingReceiver)
        }
    }
}

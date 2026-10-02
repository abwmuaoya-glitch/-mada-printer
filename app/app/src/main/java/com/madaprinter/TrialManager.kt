
package com.madaprinter

import android.content.Context

class TrialManager(context: Context) {

    private val prefs = context.getSharedPreferences(
        "mada_trial",
        Context.MODE_PRIVATE
    )

    fun used(): Int {
        return prefs.getInt("used_jobs", 0)
    }

    fun canPrint(): Boolean {
        return used() < 10
    }

    fun recordSuccessfulPrint() {
        prefs.edit()
            .putInt("used_jobs", used() + 1)
            .apply()
    }
}

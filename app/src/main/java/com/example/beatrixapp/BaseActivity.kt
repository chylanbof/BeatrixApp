package com.example.beatrixapp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.example.beatrixapp.utils.LocaleHelper

open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val idioma = LocaleHelper.getIdiomaGuardado(newBase)
        val context = LocaleHelper.setLocale(newBase, idioma)
        super.attachBaseContext(context)
    }
}

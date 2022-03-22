package es.alvarorodriguez.blogappfinal.core

import android.view.View

// Estas funciones nos sirven para hacer nuestras propias extensiones, de esta manera simplificamos el codigo

fun View.hide() {
    this.visibility = View.GONE
}

fun View.show() {
    this.visibility = View.VISIBLE
}
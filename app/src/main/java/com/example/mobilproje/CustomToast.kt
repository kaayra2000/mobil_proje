package com.example.mobilproje

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.mobilproje.R
import kotlinx.android.synthetic.main.toast_message.view.*


class CustomToast() {
    private var message: String? = null
    private var color = Color.GREEN
    private var textColor = Color.WHITE
    private var context: Context? = null
    private var vectorDrawable : Int = R.drawable.baseline_check_circle_24

    constructor(context: Context?) : this() {
        this.context = context
    }

    fun setMessage(message: String?) {
        this.message = message
    }
    fun setBackgroundColor(color: Int){
        this.color = color
    }

    fun setTextColor(color: Int){
        this.textColor = color
    }

    fun setFailureColor(){
        setTextColor(Color.WHITE)
        setBackgroundColor(Color.GRAY)
    }

    fun setSuccessColor(){
        setTextColor(Color.BLACK)
        setBackgroundColor(Color.WHITE)
    }

    fun showMessage(m: String, isSuccess: Boolean){
        if(!isSuccess){
            vectorDrawable = R.drawable.baseline_cancel_24
            setFailureColor()
        }
        else{
            vectorDrawable = R.drawable.baseline_check_circle_24
            setSuccessColor()
        }
        setMessage(m)
        show()
    }

    fun show() {
        val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = inflater.inflate(R.layout.toast_message, null)
        val textView = layout.findViewById<TextView>(R.id.text)
        val custom_toast_container = layout.findViewById<LinearLayout>(R.id.custom_toast_container)
        custom_toast_container.setBackgroundColor(color)
        custom_toast_container.imageViewToast.setImageResource(vectorDrawable)
        textView.text = message
        textView.setTextColor(textColor)
        val toast = Toast(context)
        toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 0)
        toast.duration = Toast.LENGTH_LONG
        toast.view = layout
        toast.show()
    }
}

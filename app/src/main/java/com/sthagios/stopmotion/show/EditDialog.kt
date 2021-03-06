package com.sthagios.stopmotion.show

import android.app.Activity
import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.InputType
import com.afollestad.materialdialogs.MaterialDialog
import com.sthagios.stopmotion.R

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   10.06.16
 */
class EditDialog() : DialogFragment() {


    companion object {
        val BUNDLE_VALUE = "BUNDLE_VALUE"

        fun newInstance(value: String): EditDialog {
            val fragment = EditDialog()
            val args = Bundle();
            args.putString(BUNDLE_VALUE, value);
            fragment.arguments = args
            return fragment
        }
    }

    interface Callback {
        fun onOk(name: String)
    }

    lateinit var mListener: Callback

    var mValue = ""

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is Callback) {
            mListener = context
        } else
            throw Exception("${context.toString()} must implement Callback")
    }

    @Suppress("DEPRECATION")
    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (activity is EditDialog.Callback) {
                mListener = activity
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        if (arguments != null) {
            mValue = arguments.getString(BUNDLE_VALUE, "");
        }

        if (savedInstanceState != null) {
            mValue = savedInstanceState.getString(BUNDLE_VALUE, "");
        }

        return MaterialDialog.Builder(activity)
                .title(R.string.rename_dialog_title)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .alwaysCallInputCallback()
                .input(getString(R.string.edit_text_hint_gif_name), null, { dialog, input ->
                    mValue = input.toString()
                })
                .onPositive { materialDialog, dialogAction ->
                    mListener.onOk(mValue.toString())
                }
                .negativeText(R.string.cancel_button)
                .build()
    }
}
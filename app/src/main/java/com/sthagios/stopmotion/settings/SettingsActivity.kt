package com.sthagios.stopmotion.settings


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.app.NavUtils
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget
import com.sthagios.stopmotion.BuildConfig
import com.sthagios.stopmotion.R
import com.sthagios.stopmotion.settings.dialogs.LicensesDialog
import com.sthagios.stopmotion.tracking.getFirebaseInstance
import com.sthagios.stopmotion.tracking.logSettingsEvent
import com.sthagios.stopmotion.tracking.setUserProperty
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.toolbar.*
import rx.Observable
import rx.subjects.PublishSubject


class SettingsActivity : AppCompatActivity(), SettingsView {

    private val MY_PERMISSIONS_REQUEST_STORAGE: Int = 123

    private val mOnPermissionResult: PublishSubject<Boolean> = PublishSubject.create()
    override fun noStoragePermissionGranted() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                MY_PERMISSIONS_REQUEST_STORAGE)
    }

    override fun showMovingLoading(show: Boolean) {
        //TODO
    }

    private val mOnStorageOptionChanged: PublishSubject<Void> = PublishSubject.create()

    private val mOnThumbsInListChanged: PublishSubject<Void> = PublishSubject.create()

    override fun onThumbsInListChanged(): Observable<Void> = mOnThumbsInListChanged.asObservable()

    override fun onPermissionResult(): Observable<Boolean> = mOnPermissionResult.asObservable()

    override fun onStorageOptionChanged(): Observable<Void> = mOnStorageOptionChanged.asObservable()

    override fun onError(throwable: Throwable) {
        Snackbar.make(icon_image_view, getString(R.string.snackbar_error_occurred),
                Snackbar.LENGTH_LONG).show()
    }

    override fun onStart() {
        super.onStart()
        if (!mPresenter.hasView()) {
            mPresenter.attachView(this)
            mPresenter.onStart()
        }
    }

    override fun onStop() {
        super.onStop()
        mPresenter.detachView()
    }

    private lateinit var mPresenter: SettingsPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        mPresenter = SettingsPresenter(baseContext)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.settings_title)

        use_thumbs.onCheckChanged { b ->
            mOnThumbsInListChanged.onNext(null)
        }

        licenses.setOnClickListener({
            logSettingsEvent("license")
            val dialog = LicensesDialog()
            dialog.show(fragmentManager, "LicensesDialog")
        })

        feedback.setOnClickListener {
            logSettingsEvent("feedback")
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.type = "message/rfc822"
            emailIntent.data = Uri.parse("mailto:" + "stopmotion@sthagios.com")
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.rating_feedback_email_title))
            try {
                startActivity(Intent.createChooser(emailIntent,
                        getString(R.string.rating_email_choose_text)))
            } catch (ex: android.content.ActivityNotFoundException) {
                //TODO might add toast
            }
        }

        store_options.onClickListener {
            mOnStorageOptionChanged.onNext(null)
        }

        icon_image_view.setOnClickListener {
            if (mImageClickCount++ > 6) {
                ViewCompat.animate(icon_image_view)
                        .scaleY(0.toFloat())
                        .scaleX(0.toFloat())
                        .setDuration(1000)
                        .setInterpolator(DecelerateInterpolator()).start()

                ViewCompat.animate(icon_image_view2)
                        .setStartDelay(1000)
                        .scaleY(1.toFloat())
                        .scaleX(1.toFloat())
                        .setDuration(1000)
                        .setInterpolator(DecelerateInterpolator()).start()

                val bundle = Bundle()
                bundle.putString("wee", "wee")
                getFirebaseInstance().logEvent("easter_egg", bundle)
                setUserProperty("easter_egg", "found")
            }
        }

        val uri = Uri.parse("file:///android_asset/gif.gif")
        val target = GlideDrawableImageViewTarget(icon_image_view2)
        Glide.with(this).load(uri).into(target)

        setUpVersionInfo()
    }

    override fun onPermissionNotGranted() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),
                MY_PERMISSIONS_REQUEST_STORAGE)
    }

    override fun setStorageOption(isSet: Boolean) {
        store_options.setChecked(isSet)
    }

    override fun setThumbsInList(isSet: Boolean) {
        use_thumbs.setChecked(isSet)
    }

    private var mImageClickCount = 0

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setUpVersionInfo() {
        version_text.text = getString(R.string.settings_version, BuildConfig.VERSION_NAME)

        if (BuildConfig.DEBUG) {
            debug_info.visibility = View.VISIBLE
            version_code.text = "Version Code: ${BuildConfig.VERSION_CODE}"
            git_sha.text = "Git sha: ${BuildConfig.GIT_SHA}"
            build_time.text = "Build time: ${BuildConfig.BUILD_TIME}"
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //TODO track
                    mOnPermissionResult.onNext(true)
                } else {
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (!shouldShowRequestPermissionRationale(
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            showPermissionDenyInfo()
                        }
                    }
                    store_options.setChecked(false)
                    mOnPermissionResult.onNext(false)
                }
                return
            }
        }
    }

    private fun showPermissionDenyInfo() {

        button_next.setOnClickListener({
            val i = Intent()
            i.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            i.addCategory(Intent.CATEGORY_DEFAULT)
            i.data = Uri.parse("package:" + packageName)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            startActivity(i)
        })
    }

}


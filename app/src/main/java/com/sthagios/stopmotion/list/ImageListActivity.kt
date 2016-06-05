package com.sthagios.stopmotion.list

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import com.sthagios.stopmotion.R
import com.sthagios.stopmotion.create.CreateNewImage
import com.sthagios.stopmotion.image.database.Gif
import com.sthagios.stopmotion.image.database.getRealmInstance
import com.sthagios.stopmotion.utils.startActivity
import kotlinx.android.synthetic.main.activity_image_list.*

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   30.04.16
 */
class ImageListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_list)

        recyclerViewImageList.setHasFixedSize(true)
        recyclerViewImageList.layoutManager = GridLayoutManager(this, 2)

        val realm = getRealmInstance()

        val adapter = ImageListAdapter(this, realm.where(Gif::class.java).findAllAsync())

        recyclerViewImageList.adapter = adapter

        recyclerViewImageList.addItemDecoration(ItemDecorator())

        fab.setOnClickListener({ view -> createNewImage() })

    }

    private fun createNewImage() {
        startActivity<CreateNewImage>()
    }

}
package hntecology.ecology.activities

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.view.View
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.GridView
import hntecology.ecology.R
import hntecology.ecology.adapter.ImageAdapter
import hntecology.ecology.base.BackPressCloseHandler
import hntecology.ecology.base.ImageLoader
import hntecology.ecology.base.Utils
import hntecology.ecology.model.PhotoData
import kotlinx.android.synthetic.main.activity_write_album.*
import java.io.File
import java.io.IOException
import java.util.*

class WriteAlbumActivity : Activity() , AdapterView.OnItemClickListener {

    private val photoList = ArrayList<PhotoData>()
    private val selected = LinkedList<String>()
    private var imageUri: Uri? = null

    private var context: Context? = null
    private var progressDialog: ProgressDialog? = null

    private val backPressCloseHandler: BackPressCloseHandler? = null

    private val FROM_CAMERA = 100
    private val REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 1
    private val REQUEST_PERMISSION_CAMERA = 2


    private var imagePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_album)
        this.context = this
        progressDialog = ProgressDialog(context)

//        Utils.setStatusBarColor(this)

        val resolver = contentResolver
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.ORIENTATION, MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val idx = IntArray(proj.size)

            cursor = MediaStore.Images.Media.query(resolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC")
            if (cursor != null && cursor.moveToFirst()) {
                idx[0] = cursor.getColumnIndex(proj[0])
                idx[1] = cursor.getColumnIndex(proj[1])
                idx[2] = cursor.getColumnIndex(proj[2])
                idx[3] = cursor.getColumnIndex(proj[3])
                idx[4] = cursor.getColumnIndex(proj[4])

                var photo = PhotoData(null,null,null,null)
                photo.photoID = -1
//                photoList.add(photo)

                do {
                    val photoID = cursor.getInt(idx[0])
                    val photoPath = cursor.getString(idx[1])
                    val displayName = cursor.getString(idx[2])
                    val orientation = cursor.getInt(idx[3])
                    val bucketDisplayName = cursor.getString(idx[4])
                    if (displayName != null) {
                        photo = PhotoData(null,null,null,null)
                        photo.photoID = photoID
                        photo.photoPath = photoPath
                        photo.orientation = orientation
                        photo.bucketPhotoName = bucketDisplayName
                        photoList.add(photo)
                    }

                } while (cursor.moveToNext())

                cursor.close()
            }
        } catch (ex: Exception) {
            // Log the exception's message or whatever you like
        } finally {
            try {
                if (cursor != null && !cursor.isClosed) {
                    cursor.close()
                }
            } catch (ex: Exception) {
            }

        }

//        list = findViewById(R.id.listGV) as GridView
        listGV.setOnItemClickListener(this)

        val imageLoader = ImageLoader(resolver)

        val adapter = ImageAdapter(this, photoList, imageLoader, selected)
        listGV.setAdapter(adapter)

        imageLoader.setListener(adapter)

        addBtnLL.setOnClickListener(View.OnClickListener {
            val result = arrayOfNulls<String>(selected.size)
            var idx = 0
            for (strPo in selected) {
                // result[idx++] =
                // photoList.get(Integer.parseInt(strPo)).photoPath;
                result[idx++] = photoList[Integer.parseInt(strPo)].photoPath
            }

            val returnIntent = Intent()
            returnIntent.putExtra("result", result)
            setResult(RESULT_OK, returnIntent)
            finish()
        })

        backLL.setOnClickListener {

            finish()
        }
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val strPo = position.toString()

        val photo_id = photoList[position].photoID

        if (photo_id == -1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                loadPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE)
            } else {
                takePhoto()
            }
        } else {
            if (selected.contains(strPo)) {
                selected.remove(strPo)

                val adapter = listGV!!.getAdapter()
                if (adapter != null) {
                    val f = adapter as ImageAdapter
                    (f as BaseAdapter).notifyDataSetChanged()
                }

            } else {
                //            if (selected.size() + picCount >= (limit-nowPicCount)) {
                //                Utils.alert(context, "사진은 "+limit+"개까지 등록가능합니다.");
                //                return;
                //            }

                selected.add(strPo)

                val adapter = listGV.getAdapter()
                if (adapter != null) {
                    val f = adapter as ImageAdapter
                    (f as BaseAdapter).notifyDataSetChanged()
                }
            }
        }
    }


    private fun loadPermissions(perm: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(perm), requestCode)
        } else {
            if (Manifest.permission.WRITE_EXTERNAL_STORAGE == perm) {
                loadPermissions(Manifest.permission.CAMERA, REQUEST_PERMISSION_CAMERA)
            } else if (Manifest.permission.CAMERA == perm) {
                takePhoto()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadPermissions(Manifest.permission.CAMERA, REQUEST_PERMISSION_CAMERA)
                } else {
                    // no granted
                }
                return
            }
            REQUEST_PERMISSION_CAMERA -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhoto()
                } else {
                    // no granted
                }
                return
            }
        }
    }

    private fun takePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {

            // File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

            // File photo = new File(dir, System.currentTimeMillis() + ".jpg");

            try {
                val photo = File.createTempFile(
                        System.currentTimeMillis().toString(), /* prefix */
                        ".jpg", /* suffix */
                        storageDir      /* directory */
                )

                //                imageUri = Uri.fromFile(photo);
                imageUri = FileProvider.getUriForFile(context, context!!.getApplicationContext().packageName + ".provider", photo)
                imagePath = photo.absolutePath
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                startActivityForResult(intent, FROM_CAMERA)

            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                FROM_CAMERA -> {
                    val realPathFromURI = imageUri!!.getPath()
                    context!!.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://$realPathFromURI")))
                    try {

                        //                        selectedImage = Utils.getImage(context.getContentResolver(), realPathFromURI, 500);
                        //                         selectedImage = Utils.resize(selectedImage, 100);

                        val result = arrayOfNulls<String>(1)
                        val idx = 0
                        result[0] = imagePath.toString()

                        val returnIntent = Intent()
                        returnIntent.putExtra("result", result)
                        setResult(RESULT_OK, returnIntent)
                        finish()

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
            }//}
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onBackPressed() {
        back()
    }

    private fun back() {
        finish()
    }

    fun onClickBack(view: View) {
        back()
    }

    fun onClickHome(view: View) {
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

}

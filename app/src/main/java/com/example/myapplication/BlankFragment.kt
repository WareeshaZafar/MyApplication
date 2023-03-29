package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


class BlankFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blank, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sceneViewerIntent = Intent(Intent.ACTION_VIEW)
        val modelUri = Uri.parse("app/src/main/assets/model.gltf")
        val intentUri = Uri.parse("https://arvr.google.com/scene-viewer/1.0")
            .buildUpon()
            .appendQueryParameter("file", modelUri.toString())
            .appendQueryParameter("mode", "3d_only")
            .build()
        sceneViewerIntent.data = intentUri
        sceneViewerIntent.`package` = "com.google.ar.core"
        context?.startActivity(sceneViewerIntent)
    }



}
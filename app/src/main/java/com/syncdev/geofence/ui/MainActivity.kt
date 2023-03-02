package com.syncdev.geofence.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.syncdev.geofence.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    private lateinit var destinationAdapter: DestinationAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        destinationAdapter= DestinationAdapter()
        binding.recycleView.layoutManager=LinearLayoutManager(this)
        binding.recycleView.adapter=destinationAdapter

        binding.btnAddDestination.setOnClickListener {
            startActivity(Intent(this, MapsActivity::class.java))
        }

    }
}
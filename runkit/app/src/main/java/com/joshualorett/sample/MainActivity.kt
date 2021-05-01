package com.joshualorett.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.joshualorett.runkit.sample.R
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel> { MainViewModel.Factory(application) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pagingAdapter = SessionAdapter(SessionComparator)
        val recyclerView = findViewById<RecyclerView>(R.id.sessionList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = pagingAdapter

        lifecycleScope.launch {
            viewModel.sessions.collectLatest { pagingData ->
                pagingAdapter.submitData(pagingData)
            }
        }
    }
}
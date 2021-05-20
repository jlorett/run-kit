package com.joshualorett.fusedapp.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.joshualorett.fusedapp.R
import com.joshualorett.fusedapp.database.SessionDatabaseFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    private val viewModel by viewModels<HomeViewModel> {
        HomeViewModel.Factory(SessionDatabaseFactory.getInstance(requireContext()).sessionDao())
    }
    private lateinit var startSessionBtn: ExtendedFloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startSessionBtn = view.findViewById(R.id.startSessionBtn)
        startSessionBtn.setOnClickListener { startNewSession() }

        val pagingAdapter = SessionAdapter(SessionComparator)
        val recyclerView = view.findViewById<RecyclerView>(R.id.sessionList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = pagingAdapter

        lifecycleScope.launch {
            viewModel.sessions.collectLatest { pagingData ->
                pagingAdapter.submitData(pagingData)
            }
        }
    }

    private fun startNewSession() {
        val navController = findNavController()
        navController.navigate(R.id.action_homeFragment_to_activeSessionFragment)
    }
}
package com.joshualorett.fusedapp.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.addRepeatingJob
import androidx.navigation.fragment.findNavController
import androidx.paging.cachedIn
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.joshualorett.fusedapp.R
import com.joshualorett.fusedapp.database.SessionDatabaseFactory
import kotlinx.coroutines.flow.collectLatest

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    private val viewModel by viewModels<HomeViewModel> {
        HomeViewModel.Factory(SessionDatabaseFactory.getInstance(requireContext()).sessionDao())
    }
    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val startSessionBtn = view.findViewById<ExtendedFloatingActionButton>(R.id.startSessionBtn)
        startSessionBtn.setOnClickListener { startNewSession() }
        val recyclerView = view.findViewById<RecyclerView>(R.id.sessionList)
        val pagingAdapter = SessionAdapter()
        recyclerView.adapter = pagingAdapter
        viewLifecycleOwner.addRepeatingJob(Lifecycle.State.STARTED) {
            viewModel.sessions
                .cachedIn(this)
                .collectLatest { pagingData ->
                    pagingAdapter.submitData(pagingData)
                }
        }
    }

    private fun startNewSession() {
        navController.navigate(R.id.action_homeFragment_to_activeSessionFragment)
    }
}
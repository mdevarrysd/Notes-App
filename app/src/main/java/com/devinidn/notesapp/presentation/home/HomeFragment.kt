package com.devinidn.notesapp.presentation.home

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.devinidn.notesapp.R
import com.devinidn.notesapp.data.local.entity.Notes
import com.devinidn.notesapp.databinding.FragmentHomeBinding
import com.devinidn.notesapp.presentation.NotesViewModel
import com.devinidn.notesapp.utils.ExtensionFunctions.setActionBar

class HomeFragment : Fragment(), androidx.appcompat.widget.SearchView.OnQueryTextListener {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding as FragmentHomeBinding

    private val homeViewModel: NotesViewModel by viewModels()
    private val homeAdapter by lazy { HomeAdapter() }

    private var _currentData: List<Notes>? = null
    private val currentData get() = _currentData as List<Notes>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setHasOptionsMenu(true)

        binding.apply {
            toolbarHome.setActionBar(requireActivity())

            fabAdd.setOnClickListener {
                findNavController().navigate(R.id.action_homeFragment_to_addFragment)
            }
        }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.rvNotes.apply {
            homeViewModel.getAllNotes().observe(viewLifecycleOwner) {
                checkNotesIsEmpty(it)
                homeAdapter.setData(it)
                _currentData = it
            }

            adapter = homeAdapter
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            swipeToDelete(this)
        }
    }

    private fun checkNotesIsEmpty(data: List<Notes>) {
        binding.apply {
            if (data.isEmpty()) {
                imgNoData.visibility = View.VISIBLE
                rvNotes.visibility = View.INVISIBLE
            } else {
                imgNoData.visibility = View.INVISIBLE
                rvNotes.visibility = View.VISIBLE
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_home, menu)

        val searchView = menu.findItem(R.id.menu_search)
        val actionView = searchView.actionView as? androidx.appcompat.widget.SearchView
        actionView?.setOnQueryTextListener(this)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        query?.let {
            searchNoteByQuery(it)
        }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        newText?.let {
            searchNoteByQuery(it)
        }
        return true
    }

    private fun searchNoteByQuery(query: String) {
        val querySearch = "%$query%"
        homeViewModel.searchNoteByQuery(querySearch).observe(this) {
            homeAdapter.setData(it)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_priority_high -> homeViewModel.sortByHighPriority.observe(this) {
                homeAdapter.setData(it)
            }
            R.id.menu_priority_low -> homeViewModel.sortByLowPriority.observe(this) {
                homeAdapter.setData(it)
            }
            R.id.menu_delete -> confirmDeleteAllNote()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun confirmDeleteAllNote() {


        if (currentData.isEmpty()) {
            AlertDialog.Builder(context)
                .setTitle("No Notes")
                .setMessage("There is no data to delete here")
                .setPositiveButton("Ok") { _, _ -> }
                .show()

        } else {
            AlertDialog.Builder(context)
                .setTitle("Delete All Note")
                .setMessage("Are you sure want to remove all of the notes?")
                .setPositiveButton("Yes") { _, _ ->
                    homeViewModel.deleteAllNotes()
                    Toast.makeText(context, "Successfully deleted note", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("No") { _, _ -> }
                .show()
        }

    }

    private fun swipeToDelete(recyclerView: RecyclerView) {
        val swipeToDelete = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deletedItem = homeAdapter.listNotes[viewHolder.adapterPosition]
                homeViewModel.deleteNote(deletedItem)
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeToDelete)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
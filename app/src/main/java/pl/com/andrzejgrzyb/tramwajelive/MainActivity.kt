package pl.com.andrzejgrzyb.tramwajelive

import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import pl.com.andrzejgrzyb.tramwajelive.fragment.FilterFragment
import pl.com.andrzejgrzyb.tramwajelive.fragment.MapFragment

class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModel()

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                openFragment(MapFragment.instance)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                openFragment(FilterFragment.instance)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                mainViewModel.refreshData()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        navigation.selectedItemId = R.id.navigation_home
        mainViewModel.toastMessage.observe(this, Observer {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        })
    }

    override fun onStart() {
        super.onStart()
        mainViewModel.startRefreshingData()
    }

    override fun onPause() {
        mainViewModel.stopRefreshingData()
        super.onPause()
    }

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        mainViewModel.filteredLineNumbers.observe(this, Observer {
            menu?.findItem(R.id.filter)?.icon?.mutate()?.setColorFilter(
                ContextCompat.getColor(
                    this,
                    if (mainViewModel.isFilterOn()) R.color.colorAccent else R.color.bgLineNumberFilter
                ), PorterDuff.Mode.SRC_ATOP
            )
        })
        return true
    }

    fun filter(v: View) {
        val menu = PopupMenu(this, v)
//        for (s in limits) { // "limits" its an arraylist
//            menu.getMenu().add(s)
//        }
        for (i in 0..3) {
            menu.menu.add(0, i, i, "Filter $i").setOnMenuItemClickListener(menuItemClickListener)
        }
        menu.show()
    }

    private val menuItemClickListener = MenuItem.OnMenuItemClickListener {
        Toast.makeText(this, it.title, Toast.LENGTH_SHORT).show()
        return@OnMenuItemClickListener true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.refresh -> {
                mainViewModel.refreshData()
            }
            R.id.filter -> {
                Log.i("MainActivity", "Filter button clicked")
                mainViewModel.filterButtonClicked()
            }
        }
        return true
    }

}

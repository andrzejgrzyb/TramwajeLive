package pl.com.andrzejgrzyb.tramwajelive

import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import pl.com.andrzejgrzyb.tramwajelive.databinding.ActivityMainBinding
import pl.com.andrzejgrzyb.tramwajelive.fragment.FilterFragment
import pl.com.andrzejgrzyb.tramwajelive.fragment.MapFragment

class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModel()

    override fun onBackPressed() {
        if (mainViewModel.currentFragment.value != R.id.navigation_home) {
            navigation.selectedItemId = R.id.navigation_home
        } else {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.mainViewModel = mainViewModel
        mainViewModel.toastMessage.observe(this, Observer {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        })
        mainViewModel.currentFragment.observe(this, Observer {
            openFragment(
                when (it) {
                    R.id.navigation_lines -> FilterFragment.instance
                    else -> MapFragment.instance
                }
            )
            navigation.menu.findItem(it).isChecked = true
        })
    }

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        mainViewModel.filtersEnabled.observe(this, Observer { isEnabled ->
            menu?.findItem(R.id.filter)?.icon?.mutate()?.setColorFilter(
                ContextCompat.getColor(
                    this,
                    if (isEnabled) R.color.colorAccent else R.color.bgLineNumberFilter
                ), PorterDuff.Mode.SRC_ATOP
            )
        })
        return true
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

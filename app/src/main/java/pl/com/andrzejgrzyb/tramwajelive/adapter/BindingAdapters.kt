package pl.com.andrzejgrzyb.tramwajelive.adapter

import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.ChipGroup


@BindingAdapter("app:listener")
fun setOnNavigationItemSelectedListener(
    view: BottomNavigationView,
    listener: BottomNavigationView.OnNavigationItemSelectedListener
) {
    view.setOnNavigationItemSelectedListener(listener)
}

fun ChipGroup.getCheckChipIndex() = this.indexOfChild(this.findViewById(this.checkedChipId))
fun ChipGroup.setCheckChipIndex(index: Int) {
    val chipToCheck = this.getChildAt(index)
    if (chipToCheck != null) {
        this.check(chipToCheck.id)
    }
}

class ChipGroupBindingAdapter {
    companion object {
        @JvmStatic
        @BindingAdapter(value = ["android:checkedButtonIndexAttrChanged"])
        fun setListener(chipGroup: ChipGroup, listener: InverseBindingListener) {
            chipGroup.setOnCheckedChangeListener { chipGroup: ChipGroup, i: Int ->
                listener.onChange()
            }
        }

        @JvmStatic
        @BindingAdapter("android:checkedButtonIndex")
        fun setCheckedButtonIndex(view: ChipGroup, index: Int) {
            if (view.childCount > 1 && view.getCheckChipIndex() != index) {
                view.setCheckChipIndex(index)
            }
        }

        @JvmStatic
        @InverseBindingAdapter(attribute = "android:checkedButtonIndex")
        fun getCheckedButtonIndex(chipGroup: ChipGroup): Int {
            return chipGroup.getCheckChipIndex()
        }
    }

}


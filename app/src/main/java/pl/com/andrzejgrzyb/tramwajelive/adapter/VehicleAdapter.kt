package pl.com.andrzejgrzyb.tramwajelive.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pl.com.andrzejgrzyb.tramwajelive.R
import pl.com.andrzejgrzyb.tramwajelive.model.VehicleInfo
import kotlinx.android.synthetic.main.recycler_view_item_vehicle_info.view.*

class VehicleAdapter() : RecyclerView.Adapter<VehicleAdapter.ViewHolder>() {

    internal var data: List<VehicleInfo> = ArrayList()
        set(data) {
            field = data
            this.notifyDataSetChanged()
        }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_item_vehicle_info, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(data[position])
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindData(vehicleInfo: VehicleInfo) {
            itemView.line_number.text = vehicleInfo.lines.toString()
            itemView.brigade.text = vehicleInfo.brigade.toString()
            itemView.latitude.text = vehicleInfo.lat.toString()
            itemView.longitude.text = vehicleInfo.lon.toString()
            itemView.time.text = vehicleInfo.time
        }
    }
}
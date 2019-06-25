package com.ksy.vision.portfolio

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ProgressBar
import android.widget.TextView

class ChartListAdapter(val context: Context, val chartList: List<String>) : BaseAdapter() {


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_view, null)
            holder = ViewHolder()
            holder.desc = view.findViewById<TextView>(R.id.analysis_des)
            holder.perStat = view.findViewById<TextView>(R.id.analysis_per)
            holder.chart = view.findViewById<ProgressBar>(R.id.analysis_chart)
            view.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
            view = convertView
        }

        val m = chartList.get(position).split(":")
            if(m.size > 1){
                holder.desc?.text = m[1]
                holder.perStat?.text = m[0] + " % "
                holder.chart?.progress = Math.ceil(m[0].toDouble()).toInt()
            }
        return view
    }

    override fun getItem(position: Int): Any {
        return chartList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return chartList.size
    }

    private class ViewHolder {
        var desc: TextView? = null
        var perStat: TextView? = null
        var chart: ProgressBar? = null
    }
}
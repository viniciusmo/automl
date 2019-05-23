package com.github.viniciusmo.automl

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_image_label.view.*

class ImageLabelAdapter(private val labels: ArrayList<ImageLabel>,private val context: Context) :
        RecyclerView.Adapter<ImageLabelAdapter.ViewHolder>() {

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)


    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
                .inflate(R.layout.item_image_label, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageLabel = labels[position]
        val confidence = imageLabel.confidence * 100.0f
        when (confidence) {
            in 0.0..50.0 -> holder.view.txtConfidence.setTextColor(ContextCompat.getColor(context, R.color.colorError))
            in 50.0..70.0 -> holder.view.txtConfidence.setTextColor(ContextCompat.getColor(context, R.color.colorAlert))
            in 70.0..100.0 -> holder.view.txtConfidence.setTextColor(ContextCompat.getColor(context, R.color.colorSuccess))
        }
        holder.view.txtLabel.text = imageLabel.label
        holder.view.txtConfidence.text = "Confidence : $confidence %"
    }

    override fun getItemCount() = labels.size
}
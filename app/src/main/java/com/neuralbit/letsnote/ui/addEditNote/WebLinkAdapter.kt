package com.neuralbit.letsnote.ui.addEditNote

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.neuralbit.letsnote.R


class WebLinkAdapter(
    val context : Context,
): RecyclerView.Adapter<WebLinkAdapter.ViewHolder>() {
    private var webPhoneLinks : List<WebPhoneLink> = ArrayList()


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val linkIcon : ImageView = itemView.findViewById(R.id.linkTypeIB)
        val linkText : TextView = itemView.findViewById(R.id.webPhoneLink)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.web_phone_link_layout, parent, false)
        itemView.setOnClickListener{
            Toast.makeText(context, "Touched", Toast.LENGTH_SHORT).show()
        }
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val webPhoneLink = webPhoneLinks[position]
        holder.linkText.text = webPhoneLink.link
        if (webPhoneLink.type == WebPhoneType.WEB){
            holder.linkIcon.setImageResource(R.drawable.ic_baseline_link_24)
            holder.linkText.setOnClickListener {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webPhoneLink.link))
                browserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
                context.startActivity(browserIntent)
            }

        }else{
            holder.linkIcon.setImageResource(R.drawable.ic_baseline_local_phone_black_24)
            holder.linkText.setOnClickListener {
                val phoneIntent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", webPhoneLink.link, null))
                phoneIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
                context.startActivity(phoneIntent)
            }
        }

    }

    override fun getItemCount(): Int {
        return webPhoneLinks.size
    }

    fun updateWebPhoneLinkList(webPhoneLinks : List<WebPhoneLink>){
        this.webPhoneLinks = webPhoneLinks
        notifyDataSetChanged()
    }
}

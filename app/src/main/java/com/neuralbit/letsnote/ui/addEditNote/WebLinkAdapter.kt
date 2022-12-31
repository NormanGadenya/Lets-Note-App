package com.neuralbit.letsnote.ui.addEditNote

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
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

            holder.linkIcon.setOnClickListener {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webPhoneLink.link))
                browserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
                context.startActivity(browserIntent)
            }

            holder.linkText.setOnLongClickListener {
                val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("label", webPhoneLink.link)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, R.string.link_copied, Toast.LENGTH_SHORT).show()
                return@setOnLongClickListener true
            }

        }else if( webPhoneLink.type == WebPhoneType.PHONE_NUMBER ){
            holder.linkIcon.setImageResource(R.drawable.ic_baseline_local_phone_black_24)
            holder.linkText.setOnClickListener {
                val phoneIntent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", webPhoneLink.link, null))
                phoneIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
                context.startActivity(phoneIntent)
            }

            holder.linkIcon.setOnClickListener {
                val phoneIntent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", webPhoneLink.link, null))
                phoneIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
                context.startActivity(phoneIntent)
            }

            holder.linkText.setOnLongClickListener {
                val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("label", webPhoneLink.link)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, R.string.phone_copied, Toast.LENGTH_SHORT).show()
                return@setOnLongClickListener true
            }


        }else{
            holder.linkIcon.setImageResource(R.drawable.ic_baseline_email_24)
            holder.linkText.setOnClickListener {
                val emailIntent = Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + webPhoneLink.link))
                emailIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
                context.startActivity(emailIntent)
            }

            holder.linkIcon.setOnClickListener {
                val emailIntent = Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + webPhoneLink.link))
                emailIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
                context.startActivity(emailIntent)
            }

            holder.linkText.setOnLongClickListener {
                val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("label", webPhoneLink.link)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, R.string.email_copied, Toast.LENGTH_SHORT).show()
                return@setOnLongClickListener true
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

package com.thakurnitin2684.screentimerank

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import java.util.*

class MemberViewHolder(containerView: View) : RecyclerView.ViewHolder(containerView) {
    val name: TextView = containerView.findViewById(R.id.memberName)
    val image: ImageView = containerView.findViewById(R.id.memberImage)
}

class MembersAdapter(
    context: Context,
    private val resource: Int,
    private val memberName: ArrayList<String>,
    private val imageUrl: ArrayList<String>

) : RecyclerView.Adapter<MemberViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.each_member, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.name.text = memberName[position]
        Picasso.get().load(imageUrl[position]).error(R.drawable.profile)
            .transform(CircleTransform())
            .placeholder(R.drawable.profile).into(holder.image)
    }


    override fun getItemCount(): Int {
        return memberName.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

}
package com.thakurnitin2684.screentimerank.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.thakurnitin2684.screentimerank.utils.CircleTransform
import com.thakurnitin2684.screentimerank.R
import com.thakurnitin2684.screentimerank.databinding.EachMemberBinding
import java.util.*


class MembersAdapter(
    private val memberName: ArrayList<String>,
    private val imageUrl: ArrayList<String>

) : RecyclerView.Adapter<MembersAdapter.DataViewHolder>() {


    private lateinit var binding: EachMemberBinding


    class DataViewHolder(private val binding: EachMemberBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(member: String, url: String) {


            binding.memberName.text = member
            Picasso.get().load(url).error(R.drawable.profile)
                .transform(CircleTransform())
                .placeholder(R.drawable.profile).into(binding.memberImage)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        binding = EachMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DataViewHolder(binding)

    }


    override fun onBindViewHolder(holder: DataViewHolder, position: Int) =
        holder.bind(memberName[position], imageUrl[position])


    fun addData(
        members: ArrayList<String>,
        urls: ArrayList<String>
    ) {
        memberName.clear()
        imageUrl.clear()

        memberName.addAll(members)
        imageUrl.addAll(urls)

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
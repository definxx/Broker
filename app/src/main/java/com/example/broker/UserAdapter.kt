package com.example.broker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class UserAdapter(private val userList: List<User>, private val onItemClick: (String) -> Unit) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.usersFullname)
        val email: TextView = view.findViewById(R.id.UsersEmail)
        val roundImage: ImageView = view.findViewById(R.id.roundImage)  // Ensure this matches the layout XML
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_item, parent, false)
        return UserViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.name.text = user.name
        holder.email.text = user.email

        // Check if profilePhotoUrl is not null
        val photoUrl = user.profilePhotoUrl
        if (!photoUrl.isNullOrBlank()) {
            Glide.with(holder.itemView.context)
                .load(photoUrl)
                .circleCrop()  // Makes the image round
                .into(holder.roundImage)
        } else {
            Glide.with(holder.itemView.context)
                .load(R.drawable.brokerlogoo)  // Use a default avatar image if the URL is null
                .circleCrop()
                .into(holder.roundImage)
        }

        // Set the click listener for the email or entire item
        holder.email.setOnClickListener {
            onItemClick(user.email)  // Pass the email to the callback
        }
    }

    override fun getItemCount() = userList.size
}

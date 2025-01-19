package com.example.broker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UsersPaymentAdapter(private val userInformation: List<UserInformation>) : RecyclerView.Adapter<UsersPaymentAdapter.UserPaymentHolder>() {

    // ViewHolder that holds references to the views for each item
    class UserPaymentHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.userFirstname)
        val email: TextView = view.findViewById(R.id.userEmail)
        val profit: TextView = view.findViewById(R.id.userProfit)
        val deposit: TextView = view.findViewById(R.id.userDeposit)
    }

    // Inflates the view for each item and creates the ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserPaymentHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_item_2, parent, false)
        return UserPaymentHolder(itemView)
    }

    // Binds the data to the view holder
    override fun onBindViewHolder(holder: UserPaymentHolder, position: Int) {
        val user = userInformation[position]
        holder.name.text = user.name
        holder.email.text = user.email
        holder.profit.text = user.profit // Total profit
        holder.deposit.text = user.deposit // Total deposit
    }


    // Returns the total number of items in the list
    override fun getItemCount() = userInformation.size
}

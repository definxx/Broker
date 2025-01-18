package com.example.broker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
class PaymentAddress(
    private val payment: MutableList<Payment>,
    private val onDelete: (String) -> Unit
) : RecyclerView.Adapter<PaymentAddress.UserViewHolder>() {

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productName: TextView = view.findViewById(R.id.productNameEditText)
        val productType: TextView = view.findViewById(R.id.productTypeEditText)
        val address: TextView = view.findViewById(R.id.addressEditText)
        val deleteButton: TextView = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_item_1, parent, false)
        return UserViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val payment = payment[position]
        holder.productName.text = payment.productName
        holder.productType.text = payment.productType
        holder.address.text = payment.address

        holder.deleteButton.setOnClickListener {
            onDelete(payment.id)
        }
    }

    override fun getItemCount() = payment.size
}


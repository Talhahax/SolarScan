package com.talha.solarscan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.talha.solarscan.R
import com.talha.solarscan.bill.Bill

class BillAdapter(
    private var bills: List<Bill>,
    private val onViewDetailsClick: (Bill) -> Unit
) : RecyclerView.Adapter<BillAdapter.BillViewHolder>() {

    inner class BillViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.card_bill)
        val textUnits: TextView = view.findViewById(R.id.text_units)
        val textCost: TextView = view.findViewById(R.id.text_cost)
        val textDate: TextView = view.findViewById(R.id.text_date)
        val buttonViewDetails: MaterialButton = view.findViewById(R.id.button_view_details)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bill, parent, false)
        return BillViewHolder(view)
    }

    override fun onBindViewHolder(holder: BillViewHolder, position: Int) {
        val bill = bills[position]

        holder.textUnits.text = "${bill.units} units"
        holder.textCost.text = "Rs. ${formatNumber(bill.cost)}"
        holder.textDate.text = bill.billingDate

        holder.buttonViewDetails.setOnClickListener {
            onViewDetailsClick(bill)
        }
    }

    override fun getItemCount(): Int = bills.size

    fun updateBills(newBills: List<Bill>) {
        bills = newBills
        notifyDataSetChanged()
    }

    private fun formatNumber(number: Int): String {
        return String.format("%,d", number)
    }
}
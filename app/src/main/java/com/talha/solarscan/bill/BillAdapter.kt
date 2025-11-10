package com.talha.solarscan.bill

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.talha.solarscan.R
import java.text.SimpleDateFormat
import java.util.*

class BillAdapter(
    private var bills: List<Bill>,
    private val onBillClick: (Bill) -> Unit,
    private val hasRecommendation: (Long) -> Boolean
) : RecyclerView.Adapter<BillAdapter.BillViewHolder>() {

    inner class BillViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textUnits: TextView = itemView.findViewById(R.id.text_units)
        private val textCost: TextView = itemView.findViewById(R.id.text_cost)
        private val textDate: TextView = itemView.findViewById(R.id.text_date)
        private val btnShowRecommendations: Button = itemView.findViewById(R.id.button_view_details)

        fun bind(bill: Bill) {
            textUnits.text = "${bill.units} kWh"
            textCost.text = "Rs. ${formatNumber(bill.cost)}"

            // Format date
            val dateText = if (bill.billingDate.isNullOrEmpty()) {
                formatTimestamp(bill.createdAt)
            } else {
                bill.billingDate
            }
            textDate.text = dateText

            // Show button only if recommendation exists
            if (hasRecommendation(bill.id)) {
                btnShowRecommendations.visibility = View.VISIBLE
                btnShowRecommendations.setOnClickListener {
                    onBillClick(bill)
                }
            } else {
                btnShowRecommendations.visibility = View.GONE
            }

            // Optional: Make entire card clickable if recommendation exists
            itemView.setOnClickListener {
                if (hasRecommendation(bill.id)) {
                    onBillClick(bill)
                }
            }
        }

        private fun formatNumber(number: Int): String {
            return String.format("%,d", number)
        }

        private fun formatTimestamp(timestamp: Long): String {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bill, parent, false)
        return BillViewHolder(view)
    }

    override fun onBindViewHolder(holder: BillViewHolder, position: Int) {
        holder.bind(bills[position])
    }

    override fun getItemCount() = bills.size

    fun updateBills(newBills: List<Bill>) {
        bills = newBills
        notifyDataSetChanged()
    }
}
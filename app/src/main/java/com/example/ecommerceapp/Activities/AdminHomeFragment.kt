package com.example.ecommerceapp.Activities

import android.R
import android.app.ProgressDialog
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecommerceapp.Adapters.OrdersAdapter
import com.example.ecommerceapp.Helpers.DateAxisValueFormatter
import com.example.ecommerceapp.Models.DeliveryStatus
import com.example.ecommerceapp.Models.Order
import com.example.ecommerceapp.Utils.Constants
import com.example.ecommerceapp.Utils.FirebaseDatabaseManager
import com.example.ecommerceapp.Utils.Globals
import com.example.ecommerceapp.databinding.FragmentAdminHomeBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.log

class AdminHomeFragment : Fragment(), FirebaseDatabaseManager.OrderDataChangeListener {
    private lateinit var binding: FragmentAdminHomeBinding
    private lateinit var ordersAdapter: OrdersAdapter
    private  var orderList : ArrayList<Order> = ArrayList()
    private val categorySalesDataMap = mutableMapOf<String, Int>()
    private val ordersPerDayDataMap = mutableMapOf<Long, Int>()
    private lateinit var progressDialog : ProgressDialog
    private var asyncTasksCounter = 0
    private var initialLaunch = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ordersAdapter = OrdersAdapter(requireContext(), "AdminHomeFragment", orderList)

        progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle("Retrieving Data From Firebase")
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAdminHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (initialLaunch){
            progressDialog.show()
        }

        val allOptions = arrayOf("All") + DeliveryStatus.values().map { it.name }

        val statusAdapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, allOptions)
        statusAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)

        val defaultSelection = DeliveryStatus.PENDING.name
        val defaultSelectionIndex = allOptions.indexOf(defaultSelection)

        Log.d("AdminHomeFragmentLog", defaultSelectionIndex.toString())

        binding.statusFilterSpinner.setSelection(defaultSelectionIndex)

        binding.statusFilterSpinner.adapter = statusAdapter

        binding.statusFilterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedStatus = if (position == 0) {
                    "All"
                } else {
                    DeliveryStatus.values()[position - 1].name
                }
                Log.d("AdminHomeFragmentLog", selectedStatus)

                ordersAdapter.filterByStatus(selectedStatus)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle case where nothing is selected (if needed)
            }
        }

        FirebaseDatabaseManager.addOrderDataChangeListener(this)

        binding.ordersRCV.adapter = ordersAdapter
        binding.ordersRCV.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun createPieChart(pieChart: PieChart) {
        if (isAdded) {
            val entries = ArrayList<PieEntry>()

            for ((category, count) in categorySalesDataMap) {
                entries.add(PieEntry(count.toFloat(), category))
            }

            val dataSet = PieDataSet(entries, "Sales By Category")
            dataSet.setColors(*ColorTemplate.MATERIAL_COLORS)
            dataSet.valueTextColor = resources.getColor(android.R.color.black)
            dataSet.valueTextSize = 9f
            dataSet.valueFormatter = PercentFormatter(pieChart)

            val pieData = PieData(dataSet)
            pieChart.data = pieData

            // Customize other attributes of the PieChart as needed
            pieChart.description.isEnabled = false
            pieChart.setEntryLabelTextSize(9f)
            pieChart.setUsePercentValues(true)
            pieChart.animateXY(1000, 1000)
            pieChart.setEntryLabelColor(resources.getColor(android.R.color.black))
            pieChart.invalidate()
        }
    }

    private fun createLineChart(lineChart: LineChart) {
        if (isAdded) {
            val entries = ordersPerDayDataMap.toList().sortedBy { it.first }.map {
                Entry(it.first.toFloat(), it.second.toFloat())
            }

            val dataSet = LineDataSet(entries, "Orders Per Day")
            dataSet.color = Color.BLUE
            dataSet.setCircleColor(Color.BLUE)
            dataSet.valueTextColor = Color.BLACK
            dataSet.valueTextSize = 12f

            val lineData = LineData(dataSet)
            lineChart.data = lineData

            lineChart.description.isEnabled = false
            lineChart.legend.isEnabled = false
            lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
            lineChart.xAxis.labelRotationAngle = -45f
            lineChart.xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val date = Date(value.toLong())
                    val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
                    return dateFormat.format(date)
                }
            }

            lineChart.axisLeft.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString() // Convert float to integer and return as string
                }
            }

            lineChart.axisRight.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString() // Convert float to integer and return as string
                }
            }

            lineChart.animateXY(1000, 1000)
            lineChart.invalidate()
        }
    }

    private fun checkAndDismissProgressDialog() {

        if (asyncTasksCounter == 1) {
            initialLaunch = false
            progressDialog.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        FirebaseDatabaseManager.removeOrderDataChangeListener(this)
    }

    override fun onOrderDataChanged(updatedData: Map<String, List<Order>>) {
        orderList.clear()
        orderList.addAll(updatedData.values.flatten())
        ordersAdapter.notifyDataSetChanged()

        for(order in orderList){
            order.cart.forEach { item ->
                val category = item.itemCategory.name
                categorySalesDataMap[category] = (categorySalesDataMap[category] ?: 0) + item.quantity
            }

            ordersPerDayDataMap[order.timeStamp] = (ordersPerDayDataMap[order.timeStamp] ?: 0) + 1
        }

        createPieChart(binding.categoryPercentChart)
        createLineChart(binding.ordersPerDayChart)

        if (!orderList.isEmpty()){
            asyncTasksCounter++
            checkAndDismissProgressDialog()
        }


    }
}
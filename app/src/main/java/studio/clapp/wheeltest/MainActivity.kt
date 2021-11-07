package studio.clapp.wheeltest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import studio.clapp.wheelpicker.adapters.HourPickerAdapter
import studio.clapp.wheelpicker.adapters.MinutePickerAdapter
import studio.clapp.wheelpicker.dialog.TimePickerDialog
import studio.clapp.wheeltest.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val timePickerHourAdapter = HourPickerAdapter()
        val timePickerMinuteAdapter = MinutePickerAdapter()
        binding.dialogClassTimePickerMinutePicker.setAdapter(
            timePickerMinuteAdapter
        )
        binding.dialogClassTimePickerHourPicker.setAdapter(
            timePickerHourAdapter
        )
        TimePickerDialog.Builder(this).setOnPickedListener { i, i2 -> println("$i $i2") }
            .setSelectedTime("23", "55").build()
            .show()
        binding.root.setOnClickListener {
            TimePickerDialog.Builder(this).build().show()
        }
    }
}
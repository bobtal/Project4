package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.geofence.GeofencingConstants.ACTION_GEOFENCE_EVENT
import com.udacity.project4.locationreminders.geofence.GeofencingConstants.GEOFENCE_RADIUS_IN_METERS
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {

    private val TAG = SaveReminderFragment::class.java.simpleName
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

//            COMPLETED: use the user entered reminder details to:
//             1) add a geofencing request
//             2) save the reminder to the local db

            val reminder = ReminderDataItem(
                title, description, location, latitude, longitude)
            if (_viewModel.validateEnteredData(reminder)) {
                _viewModel.saveReminder(reminder)
                val geofence = getGeofence(reminder.id, latitude, longitude)
                val geofencingRequest = getGeofencingRequest(geofence)
                val geofencingClient = LocationServices.getGeofencingClient(context!!)
                geofencingClient.addGeofences(geofencingRequest, getPendingIntent())
                    .addOnCompleteListener {
                        Toast.makeText(context, R.string.geofence_added, Toast.LENGTH_SHORT).show()
                        Log.d(TAG, getString(R.string.geofence_added))
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, R.string.geofences_not_added, Toast.LENGTH_SHORT).show()
                        Log.e(TAG, getString(R.string.geofences_not_added) + it.message)
                    }
            }

        }
        
    }

    // Get a geofencing request
    private fun getGeofencingRequest(geofence: Geofence?): GeofencingRequest? {
        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
        return geofencingRequest
    }

    // Get a geofence
    private fun getGeofence(
        id: String,
        latitude: Double?,
        longitude: Double?
    ): Geofence? {
        val geofence = Geofence.Builder()
            .setRequestId(id)
            .setCircularRegion(latitude!!, longitude!!, GEOFENCE_RADIUS_IN_METERS)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .setLoiteringDelay(5000)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()
        return geofence
    }

    // Get a PendingIntent for the Broadcast Receiver that handles geofence transitions.
    fun getPendingIntent() : PendingIntent {
        val intent = Intent(context!!, GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        val geofencePendingIntent = PendingIntent.getBroadcast(
            context!!, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        return geofencePendingIntent
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
}

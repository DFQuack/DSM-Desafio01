package sv.edu.udb.dsm_desafio01

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT
import com.google.android.material.snackbar.Snackbar
import sv.edu.udb.dsm_desafio01.R.string
import sv.edu.udb.dsm_desafio01.databinding.ActivityPromedioBinding

class PromedioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPromedioBinding
    private val requestPermissionLauncher =
        // Si el permiso se acepta, muestra la notificación. De lo contrario, muestra un Snackbar de información y la app continúa
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                showAverageNotification(lastAverage, avgResult)
            } else {
                Snackbar.make(binding.root, getString(string.notificationsDenied), LENGTH_SHORT).show()
            }
        }
    // La notificación puede no aparecer de forma inmediata (depende del estado de los permisos), por lo que se necesita guardar el valor aparte
    private var lastAverage: Double = 0.0
    private var avgResult = ""
    private val notificationId = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPromedioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        createNotificationChannel()

        binding.btnCalc.setOnClickListener {
            val name = validateName()
            val grades = validateGrades()
            if (!grades.isNullOrEmpty() && !name.isNullOrEmpty()) {
                val average = getAverage(grades)
                binding.average.text = getString(string.average, average)
                avgResult = if (average >= 6.0) getString(string.avgPassed, name) else getString(string.avgFailed, name)
                binding.avgResult.text = avgResult
                binding.average.visibility = View.VISIBLE
                notificationPermissionCheck(average, avgResult)
            } else {
                binding.average.visibility = View.INVISIBLE
            }
        }
    }

    // Valida el nombre ingresado
    private fun validateName(): String? {
        val nameTextbox = binding.tbName
        if (nameTextbox.text.trim().isEmpty()) {
            nameTextbox.error =getString(string.emptyError)
            Snackbar.make(binding.root, getString(string.emptyError), LENGTH_SHORT).show()
            return null
        }
        return nameTextbox.text.trim().toString()
    }

    // Valida las notas
    fun validateGrades(): List<Double>? {
        val fields = listOf(binding.tbG1, binding.tbG2, binding.tbG3, binding.tbG4, binding.tbG5)
        val grades = mutableListOf<Double>()
        for (field in fields) {
            // Intenta convertir cada valor a double
            val value = field.text.toString().trim().toDoubleOrNull()
            // Verifica que los valores ingresados sean numéricos
            if (value == null) {
                field.error = getString(string.typeError)
                Snackbar.make(binding.root, getString(string.typeError), LENGTH_SHORT).show()
                return null
            }
            // Verifica que los valores estén entre 0 y 10
            if (value !in 0.0..10.0) {
                field.error = getString(string.rangeError)
                Snackbar.make(binding.root, getString(string.rangeError), LENGTH_SHORT).show()
                return null
            }
            // Verifica que las notas sean diferentes
            if (value in grades) {
                field.error = getString(string.sameValueError)
                Snackbar.make(binding.root, getString(string.sameValueError), LENGTH_SHORT).show()
                return null
            }
            grades.add(value)
        }
        return grades
    }

    // Obtiene el promedio
    fun getAverage(grades: List<Double>): Double {
        var sum = 0.0
        for (grade in grades) {
            sum += grade
        }
        return sum / grades.size
    }

    // Canal de notificaciones (necesario para que se muestren)
    private fun createNotificationChannel() {
        val id = getString(string.resultNotificationId)
        val name = getString(string.channelName)
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        val channel = NotificationChannel(id, name, importance)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    // Se encarga de pedir los permisos para notificaciones
    private fun notificationPermissionCheck(average: Double, result: String) {
        lastAverage = average // Para que requestPermissionLauncher pueda utilizar el valor
        // No se requiere permiso en runtime antes de Android 13
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            showAverageNotification(average, result)
            return
        }

        val permission = Manifest.permission.POST_NOTIFICATIONS
        when {
            // Si ya tiene el permiso, muestra la notificación sin más
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                showAverageNotification(average, result)
            }
            // Si el permiso fue denegado previamente, se muestra un Snackbar que explica la razón del permiso.
            // Dar OK al Snackbar pide el permiso nuevamente
            shouldShowRequestPermissionRationale(permission) -> {
                Snackbar.make(binding.root, getString(string.notificationRationale), LENGTH_LONG)
                    .setAction(getString(string.ok)) {
                        requestPermissionLauncher.launch(permission)
                    }
                    .show()
            }
            // Primera vez pidiendo el permiso
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    // Muestra la notificación (solo se llama si los permisos se obtuvieron)
    @SuppressLint("MissingPermission")
    private fun showAverageNotification(average: Double, result: String) {
        val notification = NotificationCompat.Builder(this, getString(string.resultNotificationId))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(string.resultNotificationTitle))
            .setContentText(getString(string.average, average) + "\n" + result)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(this).notify(notificationId, notification)
    }
}
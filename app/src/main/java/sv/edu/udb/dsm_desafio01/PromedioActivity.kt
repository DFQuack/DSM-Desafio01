package sv.edu.udb.dsm_desafio01

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT
import sv.edu.udb.dsm_desafio01.databinding.ActivityPromedioBinding
import com.google.android.material.snackbar.Snackbar
import sv.edu.udb.dsm_desafio01.R.string

class PromedioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPromedioBinding
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

        binding.btnCalc.setOnClickListener {
            val grades = validateGrades()
        }
    }
    fun validateGrades(): List<Double>? {
        val fields = listOf(binding.tbG1, binding.tbG2, binding.tbG3, binding.tbG4, binding.tbG5)
        val grades = mutableListOf<Double>()
        for (field in fields) {
            // Intenta convertir cada valor a double
            val value = field.text.toString().toDoubleOrNull()
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
}
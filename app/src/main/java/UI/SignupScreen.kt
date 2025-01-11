package UI
import Extras.Result
import ViewModels.AuthViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.welfare.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var pinCode by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }

    // Dropdown for Roles
    var expandedRole by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("") }
    val roleOptions = listOf("Citizen", "Municipal Corporation", "NGO")

    // Observing authentication result
    val authResult by authViewModel.authResult.observeAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF180b42)),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Signup Logo
                Image(
                    painter = painterResource(id = R.drawable.luser), // Placeholder for signup logo
                    contentDescription = "Sign-Up Logo",
                    modifier = Modifier
                        .size(100.dp)
                        .padding(top = 16.dp)
                )
            }

            item {
                Text(
                    "Sign Up",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Full Name
            item {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Person Icon") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Email
            item {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Password
            item {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password Icon") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Address
            item {
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // PIN Code
            item {
                OutlinedTextField(
                    value = pinCode,
                    onValueChange = {
                        pinCode = it
                        if (it.length == 6) {
                            // Mock: Autofill City/District (replace with real API)
                            val location = getCityAndDistrictFromPin(it)
                            city = location.first
                            district = location.second
                        }
                    },
                    label = { Text("PIN Code") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // City (Autofilled)
            item {
                OutlinedTextField(
                    value = city,
                    onValueChange = {},
                    label = { Text("City") },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // District (Autofilled)
            item {
                OutlinedTextField(
                    value = district,
                    onValueChange = {},
                    label = { Text("District") },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Role Dropdown
            item {
                DropdownField(
                    label = "Select Role",
                    options = roleOptions,
                    expanded = expandedRole,
                    selectedOption = selectedRole,
                    onOptionSelected = { selectedRole = it; expandedRole = false },
                    onExpandedChange = { expandedRole = !expandedRole },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Signup Button
            item {
                Button(
                    onClick = {
                        authViewModel.signUp(
                            email = email,
                            password = password,
                            fullName = fullName,
                            address = address,
                            pinCode = pinCode,
                            city = city,
                            district = district,
                            role = selectedRole
                        )
                        // Reset fields after signup
                        fullName = ""
                        email = ""
                        password = ""
                        address = ""
                        pinCode = ""
                        city = ""
                        district = ""
                        selectedRole = ""
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003366)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("SIGN UP", color = Color.White, fontSize = 16.sp)
                }
            }

            // Login Navigation
            item {
                Text(
                    "Already have an account? Sign in.",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            authViewModel.clearAllAuthStates()
                            onNavigateToLogin()
                        }
                )
            }

            // Display Auth Result
            item {
                authResult?.let { result ->
                    Spacer(modifier = Modifier.height(8.dp))
                    when (result) {
                        is Result.Success -> Text("Registration successful", color = Color.Green)
                        is Result.Error -> Text("Registration failed: ${result.message}", color = Color.Red)
                        Result.Loading -> Text("Loading...", color = Color.Gray)
                    }
                }
            }
        }
    }
}

// Mock function for autofilling city and district
fun getCityAndDistrictFromPin(pinCode: String): Pair<String, String> {
    return Pair("Mock City", "Mock District") // Replace with API or database lookup
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    options: List<String>,
    expanded: Boolean,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onExpandedChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { onExpandedChange() },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange() }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        onExpandedChange()
                    }
                )
            }
        }
    }
}

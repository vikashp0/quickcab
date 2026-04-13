package com.example.ola

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

val QuickCabGreen = Color(0xFFADFF2F)
val QuickCabBlack = Color(0xFF121212)
val QuickCabGray = Color(0xFFF5F5F5)

data class Ride(val type: String, val price: Int, val icon: String)
data class RideHistory(val type: String, val price: Int, val discount: Int, val date: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                QuickCabApp(this)
            }
        }
    }
}

@Composable
fun QuickCabApp(context: Context) {
    var screen by remember { mutableStateOf("splash") }
    var selectedRide by remember { mutableStateOf<Ride?>(null) }
    val history = remember { mutableStateListOf<RideHistory>() }

    Box(Modifier.fillMaxSize().background(Color.White)) {
        AnimatedContent(
            targetState = screen,
            transitionSpec = {
                fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
            },
            label = "ScreenTransition"
        ) { targetScreen ->
            when (targetScreen) {
                "splash" -> SplashScreen { screen = "register" }
                
                "register" -> RegisterScreen(context) { screen = "home" }
                
                "home" -> MainHomeScreen(onRideSelected = {
                    selectedRide = it
                    screen = "confirm"
                })

                "confirm" -> ConfirmScreen(selectedRide!!) {
                    screen = "driver"
                }

                "driver" -> DriverScreen {
                    screen = "payment"
                }

                "payment" -> PaymentScreen(selectedRide!!) {
                    history.add(it)
                    screen = "history"
                }

                "history" -> HistoryScreen(history) {
                    screen = "home"
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    var startAnim by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0.6f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "LogoScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(1500),
        label = "TextAlpha"
    )

    LaunchedEffect(Unit) {
        startAnim = true
        delay(3000)
        onTimeout()
    }

    Box(
        Modifier.fillMaxSize().background(QuickCabBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .size(160.dp)
                    .scale(scale)
                    .clip(RoundedCornerShape(32.dp))
                    .background(QuickCabGreen),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.ElectricCar,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = QuickCabBlack
                    )
                    Text(
                        "QuickCab",
                        color = QuickCabBlack,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        letterSpacing = (-0.5).sp
                    )
                }
            }
            Spacer(Modifier.height(32.dp))
            Text(
                "Moving People, Moving India",
                color = Color.White.copy(alpha = alpha * 0.7f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun RegisterScreen(context: Context, onDone: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome to QuickCab", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = QuickCabBlack)
        Text("Create your account to start riding", fontSize = 16.sp, color = Color.Gray)
        
        Spacer(Modifier.height(40.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) }
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                if (name.isNotEmpty() && phone.isNotEmpty()) {
                    prefs.edit().putString("name", name).apply()
                    onDone()
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = QuickCabBlack),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Next", color = QuickCabGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MainHomeScreen(onRideSelected: (Ride) -> Unit) {
    var tab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(tab == 0, { tab = 0 }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home") })
                NavigationBarItem(tab == 1, { tab = 1 }, icon = { Icon(Icons.Default.AccountBalanceWallet, null) }, label = { Text("Wallet") })
                NavigationBarItem(tab == 2, { tab = 2 }, icon = { Icon(Icons.Default.History, null) }, label = { Text("Rides") })
                NavigationBarItem(tab == 3, { tab = 3 }, icon = { Icon(Icons.Default.Person, null) }, label = { Text("Profile") })
            }
        }
    ) { pad ->
        when (tab) {
            0 -> HomeScreen(Modifier.padding(pad), onRideSelected)
            1 -> SimpleInfoScreen("Wallet Balance", "₹1,250", pad)
            2 -> SimpleInfoScreen("Past Rides", "No recent rides", pad)
            3 -> SimpleInfoScreen("Profile", "User", pad)
        }
    }
}

@Composable
fun HomeScreen(modifier: Modifier, onRideSelected: (Ride) -> Unit) {
    var pickup by remember { mutableStateOf("") }
    var drop by remember { mutableStateOf("") }
    var distanceInput by remember { mutableStateOf("") }
    val distance = distanceInput.toIntOrNull() ?: 0

    val rides = listOf(
        Ride("QuickCab Mini", distance * 12, "🚗"),
        Ride("QuickCab Prime", distance * 18, "🚙"),
        Ride("QuickCab Auto", distance * 8, "🛺"),
        Ride("QuickCab Bike", distance * 5, "🏍")
    )

    Column(modifier.fillMaxSize().background(QuickCabGray)) {
        Box(Modifier.fillMaxWidth().background(QuickCabBlack).padding(20.dp)) {
            Text("QuickCab", color = QuickCabGreen, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        }

        Card(
            Modifier.padding(16.dp).fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(16.dp)) {
                TextField(
                    pickup, { pickup = it },
                    placeholder = { Text("Your Current Location") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent),
                    leadingIcon = { Icon(Icons.Default.MyLocation, null, tint = Color.Blue) }
                )
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                TextField(
                    drop, { drop = it },
                    placeholder = { Text("Where to?") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent),
                    leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = Color.Red) }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    distanceInput, { distanceInput = it },
                    label = { Text("Estimated Distance (KM)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (distance > 0) {
            Text("Select a Ride", Modifier.padding(start = 16.dp, bottom = 8.dp), fontWeight = FontWeight.Bold)
            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                items(rides) { ride ->
                    Card(
                        Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { onRideSelected(ride) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(ride.icon, fontSize = 32.sp)
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(ride.type, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text("Comfy, AC hatchbacks", fontSize = 12.sp, color = Color.Gray)
                            }
                            Text("₹${ride.price}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                }
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Enter destination & distance to see fares", color = Color.Gray)
            }
        }
    }
}

@Composable
fun ConfirmScreen(ride: Ride, onConfirm: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(QuickCabGray),
        verticalArrangement = Arrangement.Bottom
    ) {
        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(24.dp)) {
                Text("Confirm your ${ride.type}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Fare")
                    Text("₹${ride.price}", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = QuickCabGreen)
                ) {
                    Text("Confirm Booking", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DriverScreen(onNext: () -> Unit) {
    val driverName = remember { listOf("Rahul K.", "Suresh M.", "Anil P.").random() }
    val plateNumber = remember { "KA 01 EK ${ (1000..9999).random() }" }

    Box(Modifier.fillMaxSize().background(QuickCabBlack), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(100.dp).clip(CircleShape).background(Color.DarkGray), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Person, null, modifier = Modifier.size(60.dp), tint = Color.White)
            }
            Spacer(Modifier.height(16.dp))
            Text(driverName, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(plateNumber, color = QuickCabGreen, fontSize = 18.sp)
            Spacer(Modifier.height(40.dp))
            LinearProgressIndicator(color = QuickCabGreen, modifier = Modifier.width(200.dp))
            Text("Driver is reaching in 4 mins", color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
            
            Spacer(Modifier.height(60.dp))
            Button(onClick = onNext, colors = ButtonDefaults.buttonColors(containerColor = QuickCabGreen)) {
                Text("Arrived & Complete Ride", color = Color.Black)
            }
        }
    }
}

@Composable
fun PaymentScreen(ride: Ride, onDone: (RideHistory) -> Unit) {
    val discount = 50
    val finalAmount = ride.price - discount

    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("Payment Summary", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(32.dp))
        
        PaymentRow("Trip Fare", "₹${ride.price}")
        PaymentRow("Discount (QuickCab50)", "- ₹$discount", Color.Green)
        HorizontalDivider(Modifier.padding(vertical = 16.dp))
        PaymentRow("Amount to Pay", "₹$finalAmount", fontWeight = FontWeight.ExtraBold)

        Spacer(Modifier.height(48.dp))

        Button(
            onClick = { onDone(RideHistory(ride.type, finalAmount, discount, "Today")) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = QuickCabBlack)
        ) {
            Text("Pay ₹$finalAmount", color = QuickCabGreen, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PaymentRow(label: String, value: String, color: Color = Color.Black, fontWeight: FontWeight = FontWeight.Normal) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray)
        Text(value, color = color, fontWeight = fontWeight)
    }
}

@Composable
fun HistoryScreen(list: List<RideHistory>, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(QuickCabGray)) {
        Box(Modifier.fillMaxWidth().background(Color.White).padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                Text("My Rides", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        }

        LazyColumn(Modifier.padding(16.dp)) {
            items(list.reversed()) { ride ->
                Card(
                    Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(ride.type, fontWeight = FontWeight.Bold)
                            Text("₹${ride.price}", fontWeight = FontWeight.Bold)
                        }
                        Text("Paid via Wallet", fontSize = 12.sp, color = Color.Gray)
                        Text(ride.date, fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }
        
        if (list.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No rides yet")
            }
        }
    }
}

@Composable
fun SimpleInfoScreen(title: String, info: String, pad: PaddingValues) {
    Column(Modifier.fillMaxSize().padding(pad).background(QuickCabGray), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(title, fontSize = 18.sp, color = Color.Gray)
        Text(info, fontSize = 32.sp, fontWeight = FontWeight.Bold)
    }
}

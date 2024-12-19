package com.example.kotlin_pr12

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.kotlin_pr12.ui.theme.Kotlin_pr12Theme
import kotlinx.coroutines.launch



class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Kotlin_pr12Theme {
                Menu()
            }
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Menu(){
        var imageList = remember { mutableStateListOf<String>() }
        var title by remember { mutableStateOf("Главная") }
        val scope = rememberCoroutineScope()
        val navController = rememberNavController()
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                // Содержимое бокового меню
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                ) {
                    IconButton(onClick = {navController.navigate("home")
                        title = "Главная"}){
                        Icon(Icons.Filled.Home, contentDescription =null, tint = Color.White)
                    }
                    Spacer(Modifier.padding(60.dp))
                    IconButton(onClick = {navController.navigate("list")
                        title = "Список"}){
                        Icon(Icons.Filled.List, contentDescription =null, tint = Color.White)
                    }
                }
            }
        ){
            Scaffold(
                topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
                        title = { Text(title) },
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch {
                                    drawerState.apply {
                                        if (isClosed) open() else close()
                                    }
                                }
                            }) {
                                Icon(Icons.Filled.Menu, contentDescription = "Menu")
                            }
                        }
                    )
                }, bottomBar = {
                    BottomAppBar(containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        IconButton(onClick = {navController.navigate("home")
                            title = "Главная"}){
                            Icon(Icons.Filled.Home, contentDescription =null, tint = Color.White)
                        }
                        Spacer(Modifier.weight(1f, true))
                        IconButton(onClick = {navController.navigate("list")
                            title = "Список"}){
                            Icon(Icons.Filled.List, contentDescription =null, tint = Color.White)
                        }

                    }
                }, content = {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    )
                    {
                        NavHost(navController, startDestination = "home") {
                            composable("home") { Greeting(imageList) }
                            composable("list") { ListScreen(imageList) }
                        }
                    }
                }
            )
        }

    }



    @SuppressLint("MutableCollectionMutableState", "UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun Greeting(imageList:MutableList<String>) {
        var text by remember { mutableStateOf("") }
        var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Row {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        Modifier.padding(50.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        if (imageBitmap != null) {
                            Image(
                                bitmap = imageBitmap!!.asImageBitmap(),
                                contentDescription = "Загруженное изображение",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        TextField(
                            value = text,
                            onValueChange = { text = it },
                            label = { Text("Ведите ссылку") }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            val image_url = text

                            if (image_url.isNotEmpty()) {
                                //Создаем работку
                                val workRequest = OneTimeWorkRequest.Builder(Saving::class.java)
                                    .setInputData(workDataOf("image_url" to image_url))
                                    .build()
                                //Ставим в очередь
                                WorkManager.getInstance(this@MainActivity).enqueue(workRequest)

                                //Отслеживаем статус выполения
                                val workInfoLiveData = WorkManager.getInstance(this@MainActivity)
                                    .getWorkInfoByIdLiveData(workRequest.id)

                                //Тут уже получаем данные чтоб подгрузить куда надо
                                workInfoLiveData.observe(this@MainActivity) { workInfo ->
                                    if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                                        val savedImagePath = workInfo.outputData.getString("path")
                                        if (savedImagePath != null) {
                                            imageBitmap = BitmapFactory.decodeFile(savedImagePath)
                                            imageList.add(savedImagePath)
                                            Toast.makeText(this@MainActivity, "Изображение скачано", Toast.LENGTH_SHORT).show()
                                        }
                                    } else if (workInfo.state == WorkInfo.State.FAILED) {
                                        Toast.makeText(this@MainActivity, "Ошибка при скачивании", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                val image_url_df = "https://avatars.mds.yandex.net/get-ott/224348/2a0000017e1276b1eefddcb1795690c294ac/orig"

                                val workRequest = OneTimeWorkRequest.Builder(Saving::class.java)
                                    .setInputData(workDataOf("image_url" to image_url_df))
                                    .build()
                                WorkManager.getInstance(this@MainActivity).enqueue(workRequest)

                                val workInfoLiveData = WorkManager.getInstance(this@MainActivity)
                                    .getWorkInfoByIdLiveData(workRequest.id)

                                workInfoLiveData.observe(this@MainActivity) { workInfo ->
                                    if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                                        val savedImagePath = workInfo.outputData.getString("path")
                                        if (savedImagePath != null) {
                                            imageBitmap = BitmapFactory.decodeFile(savedImagePath)
                                            imageList.add(savedImagePath)
                                            Toast.makeText(this@MainActivity, "Изображение скачано", Toast.LENGTH_SHORT).show()
                                        }
                                    } else if (workInfo.state == WorkInfo.State.FAILED) {
                                        Toast.makeText(this@MainActivity, "Ошибка при скачивании", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }) {
                            Text("Загрузть изображение")
                        }
                    }
                }
            }
        }


    }
    @Composable
    fun ListScreen(imageList:MutableList<String>){
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Row {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(Modifier.padding(50.dp),
                        horizontalAlignment = Alignment.CenterHorizontally){
                        LazyColumn {
                            items(imageList) { item ->
                                Row {
                                    Text(
                                        text = item, fontSize = 20.sp, modifier = Modifier
                                            .weight(1f)
                                            .wrapContentWidth(Alignment.Start)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Image(
                                        bitmap = BitmapFactory.decodeFile(item).asImageBitmap(),
                                        contentDescription = "часть списка",
                                        modifier = Modifier.height(120.dp)
                                    )
                                }
                            }
                        }

                    }
                }
            }
        }

    }

}
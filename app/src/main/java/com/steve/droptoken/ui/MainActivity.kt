package com.steve.droptoken.ui

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.steve.droptoken.*
import com.steve.droptoken.api.TokenService
import com.steve.droptoken.databinding.ActivityMainBinding
import com.steve.droptoken.util.GlideApp
import com.steve.droptoken.util.TokenUtil
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import retrofit2.HttpException


class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    val INTERNET_PERMISSION = 101
    private var userMode = true
    private var playMode = Constants.PLAYER_FIRST
    private var player = 1
    private var server = 2
    val images = arrayOf(
        R.id.img0, R.id.img1, R.id.img2, R.id.img3, R.id.img4, R.id.img5, R.id.img6, R.id.img7,
        R.id.img8, R.id.img9, R.id.img10, R.id.img11, R.id.img12, R.id.img13, R.id.img14, R.id.img15
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkForPermissions(android.Manifest.permission.INTERNET, "Internet", INTERNET_PERMISSION)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        for (i: Int in 0 until images.size) {
            val iv = findViewById(images.get(i)) as ImageView
            iv.setOnClickListener{
                nextStep(iv, i)
            }
        }
        updateAllImages()
        val spinner = binding.select
        val adapter = ArrayAdapter.createFromResource(this, R.array.Levels, R.layout.spinner_item)
        adapter.setDropDownViewResource(R.layout.spinner_textview)
        spinner.adapter = adapter
        spinner.setSelection(0)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View,
                i: Int,
                l: Long
            ) {
                val id = spinner.selectedItemId.toInt()
                playMode = if (id == 0) Constants.PLAYER_FIRST else Constants.SERVER_FIRST
                cleanGame(false)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                return
            }
        }

    }

    private fun serverMove() {
        CoroutineScope(IO).launch {
            getNextMoves()
        }
    }
    private suspend fun getNextMoves() {
        val existingMoves = viewModel.moveStepsString()
        val result: MutableList<Int> = mutableListOf()
        try {
            result.addAll(TokenService.invoke().getNextMove(existingMoves))
        } catch (e: HttpException) {
            if (e.code() == 400) {
                gameOver()
            } else {
                println(e.message())
                gameOver()
                return
            }
        }
        Log.e(TAG, "Response from server: $result" )
        if (result.size == 0)
            gameOver()
        else {
            val index = result.get(result.lastIndex)
            updateUI(index)
            winTheGame(server, index)
            // waiting for player's input if not win.
        }
    }

    private fun checkGameWinning(role: Int, index: Int) {
        var name = if (role == player) "You" else "Server"
        if (viewModel.checkGameWinning(index)) {
            Toast.makeText(applicationContext, "$name won the game!", Toast.LENGTH_LONG).show()
        }
    }
    private suspend fun gameOver() {
        withContext(Main) {
            cleanGame(true)
        }
    }

    private fun cleanGame(toast: Boolean) {
        viewModel.resetTokens()
        updateAllImages()
        if (toast)
            Toast.makeText(this@MainActivity, "Game Over! Retart game.", Toast.LENGTH_SHORT).show()
        if (playMode == Constants.SERVER_FIRST) {
            userMode = false
            serverMove()
        } else {
            userMode = true
        }
    }
    private suspend fun updateUI(i: Int) {
        withContext(Main) {
            nextStep(i)
        }
    }
    // for server:
    private fun nextStep(index: Int) {
        Log.e(TAG, "nextStep() index: $index")
        if (!userMode) {
            val position = TokenUtil.getPositionFromArrayIndex(index)
            Log.e(TAG, "nextStep: position: i: ${position.i}, j: ${position.j}")
            if (viewModel.isValidPosition(position)) {
                userMode = true
                val server = 3 - player
                viewModel.assignTokenValue(index, server)
                GlideApp.with(this)
                    .load(viewModel.getImageSrc(server))
                    .override(Constants.IMAGE_SIZE, Constants.IMAGE_SIZE)
                    .into(findViewById(images.get(index)))
                println(viewModel.tokens)
            } else {
                Log.e(TAG, "The returned index from server is invalid. retry... " )
                if (!winTheGame(server, index))
                    serverMove()
            }
        }
    }

    // For player:
    private fun nextStep(view: ImageView, index: Int) {

        if (userMode) {
            val position = TokenUtil.getPositionFromArrayIndex(index)
            if (viewModel.isValidPosition(position)) {
                userMode = false
                viewModel.assignTokenValue(index, player)
                GlideApp.with(this)
                    .load(viewModel.getImageSrc(player))
                    .override(Constants.IMAGE_SIZE, Constants.IMAGE_SIZE)
                    .into(view)
                println(viewModel.tokens)

                if (!winTheGame(player, index))
                    serverMove()
            } else {
                Toast.makeText(this, "Please select a valid spot", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun winTheGame(who: Int, index: Int) : Boolean{
        val name = if (who == player) "You" else "Server"
        if (viewModel.checkGameWinning(index)) {
            Toast.makeText(this@MainActivity, "$name won the game!", Toast.LENGTH_LONG).show()
            // waiting for 2 second, then clean the game.
            delayCleanup()
            return true
        }
        return false
    }

    private fun delayCleanup() {
        CoroutineScope(IO).launch {
            delay(2000)
            cleanGame(false)
        }
    }

    private fun updateAllImages() {
        for(i: Int in 0..(Constants.ARRAY_SIZE -1))
            GlideApp.with(this)
                .load(viewModel.getImage(i))
                .override(Constants.IMAGE_SIZE, Constants.IMAGE_SIZE)
                .into(findViewById(images.get(i)))

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun checkForPermissions(permission: String, name: String, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                ContextCompat.checkSelfPermission(applicationContext, permission) == PackageManager.PERMISSION_DENIED ->
                    ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            }
        }
    }

}
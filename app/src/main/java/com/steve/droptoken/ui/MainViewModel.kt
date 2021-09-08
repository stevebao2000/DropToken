package com.steve.droptoken.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import com.steve.droptoken.Constants
import com.steve.droptoken.Position
import com.steve.droptoken.R
import com.steve.droptoken.util.TokenUtil
import java.lang.IllegalArgumentException

class MainViewModel : ViewModel() {
    private val TAG = "MainViewModel"
    val ARRAY_SIZE = Constants.ARRAY_SIZE
    val tokens : MutableList<Int> = IntArray(ARRAY_SIZE) { Constants.EMPTY_TOKEN }.toMutableList()
    val tokenImages: Array<Int> = arrayOf(
        R.drawable.ic_clear,
        R.drawable.ic_red,
        R.drawable.ic_blue
    )
    var moveSteps: MutableList<Int> = mutableListOf()

    private fun getTokenValue(i: Int, j: Int): Int {
        return tokens.get(TokenUtil.getArrayIndexFromPosition(i,j))
    }

    fun checkGameWinning(index: Int) : Boolean {
        val position = TokenUtil.getPositionFromArrayIndex(index)

        if (position.j <= Constants.MATRIX_SIZE - 1)
            if (horizontalFilledSameColor(position.j)) return true

        if (position.j == Constants.MATRIX_SIZE - 1) {
            if (verticalFilledSameColor(position.i)) return true
        }
        if (diagnal1FilledSameColor()) return true
        if (diagnal2FilledSameColor()) return true
        return false
    }

    // diagnal from up-left to lower-right
    private fun diagnal1FilledSameColor() : Boolean {
        val token = getTokenValue(0, 3)
        if (token == Constants.EMPTY_TOKEN) return false
        for (i: Int in 1 until Constants.MATRIX_SIZE) {
            if (getTokenValue(i, 3 -i) != token) return false
        }
        return true
    }

    // diagnal from lower-left to up-right
    private fun diagnal2FilledSameColor() : Boolean {
        val token = getTokenValue(0, 0)
        if (token == Constants.EMPTY_TOKEN) return false
        for (i: Int in 1 until Constants.MATRIX_SIZE) {
            if (getTokenValue(i, i) != token) return false
        }
        return true
    }

    private fun horizontalFilledSameColor(j: Int) : Boolean {
        val token = getTokenValue(0, j)
        if (token == Constants.EMPTY_TOKEN) return false
        for (i: Int in 1 until Constants.MATRIX_SIZE)
            if (getTokenValue(i, j) != token) return false
        return true
    }

    private fun verticalFilledSameColor(i: Int) : Boolean {
        val token = getTokenValue(i, 0)
        if (token == Constants.EMPTY_TOKEN) return false
        for (j: Int in 1 until Constants.MATRIX_SIZE)
            if (getTokenValue(i, j) != token) return false
        return true
    }

    private fun isTokenEmpty(i: Int, j: Int) : Boolean {
        return getTokenValue(i,j) == Constants.EMPTY_TOKEN
    }

    private fun isTokenFilled(i : Int, j: Int): Boolean {
        return (!isTokenEmpty(i, j))
    }

    fun isValidPosition(p: Position) : Boolean {
        return isValidPosition(p.i, p.j)
    }

    fun isValidIndex(index: Int) : Boolean {
        if (tokens[index] == Constants.EMPTY_TOKEN) return true
        return false
    }

    private fun isValidPosition(a: Int, b: Int) : Boolean {
        if (isTokenFilled(a, b)) return false
        if (a == 0) return true

        for (i: Int in 0 until a) {
            if (isTokenEmpty(i, b))
                return false
        }
        return true
    }

    fun assignTokenValue(index: Int, value: Int): Boolean {
        if (!TokenUtil.validTokenValue(value)) return false

        tokens[index] = value
        moveSteps.add(index)
        Log.e(TAG, "Now moveSteps: ${moveSteps} " )
        return true
    }

    fun resetTokens() {
        for (i: Int in 0 until ARRAY_SIZE )
            tokens[i] = Constants.EMPTY_TOKEN
        moveSteps = mutableListOf()
    }

    fun getImage(i: Int): Int {
        if (TokenUtil.validArrayIndex(i))
            return tokenImages.get(tokens.get(i))
        throw IllegalArgumentException("Invalid index: $i")
    }

    fun moveStepsString() : String{
        return moveSteps.toString()
    }
    fun getImageSrc(i: Int) : Int {
        return tokenImages.get(i)
    }
}
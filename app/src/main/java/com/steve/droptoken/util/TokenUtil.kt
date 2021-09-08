package com.steve.droptoken.util

import com.steve.droptoken.Constants
import com.steve.droptoken.Position

object TokenUtil {
    fun validPosition(i: Int, j: Int) : Boolean {
        if (i < 0 || i > Constants.MATRIX_SIZE) return false
        if (j < 0 || j > Constants.MATRIX_SIZE) return false
        return true
    }

    fun getArrayIndexFromPosition(i: Int, j: Int) : Int {
        return i* Constants.MATRIX_SIZE + j
    }

//    fun getIndex(p: Position) : Int {
//        return p.i * Constants.MATRIX_SIZE + p.j
//    }

    fun validArrayIndex(i: Int) : Boolean {
        return (i >= 0 && i < Constants.ARRAY_SIZE)
    }

    fun validTokenValue(value: Int) : Boolean {
        if (value >= Constants.MIN_VALUE && value <= Constants.MAX_VALUE)
            return true
        return false
    }

    fun getPositionFromArrayIndex(index: Int): Position {
        val j: Int = index % Constants.MATRIX_SIZE
        val i: Int = (index-j) / Constants.MATRIX_SIZE
        return Position(i,j)
    }
}
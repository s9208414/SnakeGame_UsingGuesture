package com.example.snakegameusinggestures

import android.app.Application

class ShareVar: Application() {
    private var labelName = 0
    fun setLabel(labelName: Int){
        this.labelName = labelName
    }
    fun getLabel(): Int{
        return this.labelName
    }
}
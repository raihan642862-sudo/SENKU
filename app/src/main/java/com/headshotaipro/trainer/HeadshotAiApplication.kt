package com.headshotaipro.trainer

import android.app.Application
import com.headshotaipro.trainer.data.local.HeadshotDatabase
import com.headshotaipro.trainer.data.repository.TrainingRepositoryImpl
import com.headshotaipro.trainer.domain.repository.TrainingRepository

class HeadshotAiApplication : Application() {

    val database: HeadshotDatabase by lazy {
        HeadshotDatabase.getInstance(this)
    }

    val trainingRepository: TrainingRepository by lazy {
        TrainingRepositoryImpl(database.trainingSessionDao())
    }
}

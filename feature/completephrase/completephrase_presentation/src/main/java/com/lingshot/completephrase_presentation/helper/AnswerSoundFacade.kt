package com.lingshot.completephrase_presentation.helper

import android.content.Context
import android.media.MediaPlayer
import com.lingshot.completephrase_presentation.R

class AnswerSoundFacade(context: Context) {

    private val successSound = MediaPlayer.create(context, R.raw.verify_answer_success)
    private val errorSound = MediaPlayer.create(context, R.raw.verify_answer_error)
    private val finishedSound = MediaPlayer.create(context, R.raw.answers_finished)

    fun playSuccessSound() {
        successSound.start()
    }

    fun playErrorSound() {
        errorSound.start()
    }

    fun playFinishedSound() {
        finishedSound.start()
    }

    fun cleanUpResources() {
        successSound.release()
        errorSound.release()
        finishedSound.release()
    }
}
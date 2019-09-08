package com.example.android.guesstheword.screens.game

import android.os.CountDownTimer
import android.text.format.DateUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

//Patterns for Vibrating device
private val CORRECT_BUZZ_PATTERN = longArrayOf(100, 100, 100, 100, 100, 100)
private val PANIC_BUZZ_PATTERN = longArrayOf(0, 200)
private val GAME_OVER_BUZZ_PATTERN = longArrayOf(0, 2000)
private val NO_BUZZ_PATTERN = longArrayOf(0)

class GameViewModel : ViewModel() {

    companion object {
        //These represent diff important times
        //This is when the game is over
        const val DONE = 0L
        //This is the number of milliseconds in a second
        const val ONE_SECOND = 1000L
        //This is the total time of the game (L represents Long value)
        const val COUNTDOWN_TIME = 60000L
        // This is the time when the phone will start buzzing each second
        private const val COUNTDOWN_PANIC_SECONDS = 10L
    }

    // The current word
    private val _word = MutableLiveData<String>()
    val word: LiveData<String> get() = _word

    // The current score
    private val _score = MutableLiveData<Int>()
    val score: LiveData<Int> get() = _score

    //Variable for GameFinished
    private val _eventGameFinish = MutableLiveData<Boolean>()
    val eventGameFinish: LiveData<Boolean> get() = _eventGameFinish

    // The list of words - the front of the list is the next word to guess
    private lateinit var wordList: MutableList<String>

    private val timer: CountDownTimer

    //Countdown time
    private val _currentTime = MutableLiveData<Long>()
    private val currentTime: LiveData<Long> get() = _currentTime

    //Formatting time to timeString
    val currentTimeString = Transformations.map(currentTime) { time ->
        DateUtils.formatElapsedTime(time)
    }

    enum class BuzzType(val pattern: LongArray) {
        CORRECT(CORRECT_BUZZ_PATTERN),
        GAME_OVER(GAME_OVER_BUZZ_PATTERN),
        COUNTDOWN_PANIC(PANIC_BUZZ_PATTERN),
        NO_BUZZ(NO_BUZZ_PATTERN)
    }

    // Event that triggers the phone to buzz using different patterns, determined by BuzzType
    private val _eventBuzz = MutableLiveData<BuzzType>()
    val eventBuzz: LiveData<BuzzType>
        get() = _eventBuzz

    init {
        Log.i("GameViewModel", "GameViewModel created")
        _eventGameFinish.value = false
        _score.value = 0
        resetList()
        nextWord()
        timer = object : CountDownTimer(COUNTDOWN_TIME, ONE_SECOND) {
            override fun onFinish() {
                _currentTime.value = DONE
                _eventGameFinish.value = true
                _eventBuzz.value = BuzzType.GAME_OVER
            }

            override fun onTick(millisUntilFinished: Long) {
                _currentTime.value = (millisUntilFinished / ONE_SECOND)
                if (millisUntilFinished/ ONE_SECOND <= COUNTDOWN_PANIC_SECONDS){
                    _eventBuzz.value = BuzzType.COUNTDOWN_PANIC
                }
            }
        }
        timer.start()
    }

    override fun onCleared() {
        super.onCleared()
        timer.cancel()
        Log.i("GameViewModel", "onCleared called")
    }

    /**
     * Resets the list of words and randomizes the order
     */
    private fun resetList() {
        wordList = mutableListOf(
                "queen",
                "hospital",
                "basketball",
                "cat",
                "change",
                "snail",
                "soup",
                "calendar",
                "sad",
                "desk",
                "guitar",
                "home",
                "railway",
                "zebra",
                "jelly",
                "car",
                "crow",
                "trade",
                "bag",
                "roll",
                "bubble"
        )
        wordList.shuffle()
    }

    /**
     * Moves to the next word in the list
     */
    private fun nextWord() {
        //Select and remove a word from the list
        if (wordList.isEmpty()) {
            resetList()
        }
        _word.value = wordList.removeAt(0)
    }

    /** Methods for buttons presses **/

    fun onSkip() {
        _score.value = (_score.value)?.minus(1)
        nextWord()
    }

    fun onCorrect() {
        _score.value = (_score.value)?.plus(1)
        _eventBuzz.value = BuzzType.CORRECT
        nextWord()
    }

    fun onGameFinishComplete() {
        _eventGameFinish.value = false
    }

    fun onBuzzComplete() {
        _eventBuzz.value = BuzzType.NO_BUZZ
    }
}
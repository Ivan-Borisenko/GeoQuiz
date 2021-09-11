package com.example.geoquiz

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders

private const val TAG = "MainActivity" //для сбора логов
private const val KEY_INDEX = "index" //ключ пары "ключ-значение" для сохранения значения currentIndex
//индекс для сохранённого состояния экземпляра, сохраняет даже при уничтожении процесса, но жрёт память
//т.о. ViewModel кэширует много данных (в т.ч. из сети), а сохр.состояние - для номера в этом кэше
private const val REQUEST_CODE_CHEAT = 0

class MainActivity : AppCompatActivity() {

    private lateinit var trueButton: Button //объявление ф-ции, слушающей кнопку True
    private lateinit var falseButton: Button //объявление ф-ции для кнопки False
    //кнопки появятся в памяти после setContentView(..), поэтому инициализация должна быть после неё
    private lateinit var cheatButton: Button
    private lateinit var nextButton: ImageButton
    private lateinit var questionTextView: TextView

    /*//добавление списка вопросов с ответами, хотя обычно информация берётся из других мест
    private val questionBank = listOf(
        Question(R.string.question_australia, true),
        Question(R.string.question_oceans, true),
        Question(R.string.question_mideast, false),
        Question(R.string.question_africa, false),
        Question(R.string.question_americas, true),
        Question(R.string.question_asia, true))

    //добавление счётчика
    private var currentIndex = 0*/

    private val quizViewModel: QuizViewModel by lazy {
        ViewModelProviders.of(this).get(QuizViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate(Bundle?) called") //отметка для лога
        setContentView(R.layout.activity_main)

        val currentIndex = savedInstanceState?.getInt(KEY_INDEX, 0)?: 0
        quizViewModel.currentIndex = currentIndex

        /*val provider: ViewModelProvider = ViewModelProviders.of(this)
        val quizViewModel = provider.get(QuizViewModel::class.java)
        Log.d(TAG, "Got a QuizViewModel: $quizViewModel")*/

        trueButton = findViewById(R.id.true_button)
        falseButton = findViewById(R.id.false_button)
        nextButton = findViewById(R.id.next_button)
        cheatButton = findViewById(R.id.cheat_button)
        questionTextView = findViewById(R.id.question_text_view)

        //теперь добавим слушателей на кнопки
        trueButton.setOnClickListener {
            //Gravity.TOP позволяет вывести toast сверху экрана, с API 30 возможность отключена
            /*val toast = Toast.makeText(this, R.string.correct_toast, Toast.LENGTH_LONG)
            toast.setGravity(Gravity.TOP, 0, 0)
            toast.show() //функционал перенесён в ф-цию checkAnswer */
            checkAnswer(true)
            falseButton.isClickable = false
        }

        falseButton.setOnClickListener {
            /*val toast = Toast.makeText(this, R.string.incorrect_toast, Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.TOP, 0, 0)
            toast.show()*/
            checkAnswer(false)
            trueButton.isClickable = false
        }

        nextButton.setOnClickListener {
            //currentIndex = (currentIndex + 1) % questionBank.size
            /*val questionTextResId = questionBank[currentIndex].textResId
            questionTextView.setText(questionTextResId)*/
            quizViewModel.moveToNext()
            updateQuestion()
            trueButton.isClickable = true
            falseButton.isClickable = true
        }

        cheatButton.setOnClickListener {
            //val intent = Intent(this, CheatActivity::class.java)
            val answerIsTrue = quizViewModel.currentQuestionAnswer
            val intent = CheatActivity.newIntent(this, answerIsTrue)
            //startActivity(intent)
            startActivityForResult(intent, REQUEST_CODE_CHEAT)
        }

        //код повторяется с предыдущим, значит нужно его вынести в отедльную функцию через инкапсуляцию
        /*val questionTextResId = questionBank[currentIndex].textResId
        questionTextView.setText(questionTextResId)*/
        updateQuestion()

        //добавлено пролистывание назад, isClickable - блокировка кнопки для повторного нажатия
        /*questionTextView.setOnClickListener {
            currentIndex = if (currentIndex > 0) {
                    (currentIndex - 1) % questionBank.size
            } else {
                questionBank.lastIndex.also { currentIndex = it }
            }
            updateQuestion()
            trueButton.isClickable = true
            falseButton.isClickable = true
        }*/
    }

    override fun onActivityResult(requestCode: Int,
                                    resultCode: Int,
                                    data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        if (requestCode == REQUEST_CODE_CHEAT) {
            quizViewModel.isCheater = data?.getBooleanExtra(EXTRA_ANSWER_SHOW, false) ?: false
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart() called")
    }
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() called")
    }
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause() called")
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        Log.i(TAG, "onSaveInstanceState")
        savedInstanceState.putInt(KEY_INDEX, quizViewModel.currentIndex)
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop() called")
    }
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() called")
    }

    //вот сюда перенесён повторяющийся код
    private fun updateQuestion() {
        //val questionTextResId = questionBank[currentIndex].textResId
        val questionTextResId = quizViewModel.currentQuestionText
        questionTextView.setText(questionTextResId)
    }

    private fun checkAnswer(userAnswer: Boolean) {
        //val correctAnswer = questionBank[currentIndex].answer
        val correctAnswer = quizViewModel.currentQuestionAnswer
        /*val messageResId = if (userAnswer == correctAnswer) {
            R.string.correct_toast
        } else {
            R.string.incorrect_toast
        }*/
        val messageResId = when {
            quizViewModel.isCheater -> R.string.judgment_toast
            userAnswer == correctAnswer -> R.string.correct_toast
            else -> R.string.incorrect_toast
        }

        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT)
            .show()
    }

    //ф-ция отслеживания наличия ответов на все вопросы
    /*private fun resultAnswer() {
        //коллекция (1-вопрос, 2-0/correct/incorrect) из questionBank все вопросы, из checkAnswer все correct
        val answerBank = questionBank
        //по заполнении - showToast доля correct от общего числа вопросов
    }*/

}
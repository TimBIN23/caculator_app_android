package com.main.calculator;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * MainAppII is a calculator application that handles user input,
 * performs basic arithmetic operations, and displays calculation history.
 */
public class MainAppII extends AppCompatActivity {

    private TextView display;                 // Display for calculator input/output
    private StringBuilder currentInput;       // Holds the current input
    private List<String> history;             // Stores calculation history

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        display = findViewById(R.id.display);
        currentInput = new StringBuilder();
        history = new ArrayList<>();

        initializeButtons();                  // Setup button listeners
    }

    /**
     * Initializes all calculator buttons and assigns their click listeners.
     */
    private void initializeButtons() {
        int[] buttonIds = new int[]{
                R.id.btn_memory, R.id.btn_bracket_open, R.id.btn_bracket_close, R.id.btn_delete,
                R.id.btn_clear, R.id.btn_toggle_sign, R.id.btn_percentage, R.id.btn_divide,
                R.id.btn_7, R.id.btn_8, R.id.btn_9, R.id.btn_multiply,
                R.id.btn_4, R.id.btn_5, R.id.btn_6, R.id.btn_subtract,
                R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_add,
                R.id.btn_0, R.id.btn_dot, R.id.btn_equals
        };

        for (int id : buttonIds) {
            Button button = findViewById(id);
            button.setOnClickListener(this::onButtonClick);
        }
    }

    /**
     * Handles button clicks and performs the corresponding calculator actions.
     *
     * @param view The button that was clicked.
     */
    private void onButtonClick(View view) {
        Button button = (Button) view;
        String buttonText = button.getText().toString();
        int viewId = view.getId();

        if (viewId == R.id.btn_memory) {
            showHistoryDialog(); // Show the memory (calculation history)
        } else if (viewId == R.id.btn_clear) {
            currentInput.setLength(0);
            updateDisplay("0");
        } else if (viewId == R.id.btn_delete) {
            if (currentInput.length() > 0) {
                currentInput.deleteCharAt(currentInput.length() - 1);
            }
            updateDisplay(currentInput.length() > 0 ? currentInput.toString() : "0");
        } else if (viewId == R.id.btn_equals) {
            try {
                String expression = currentInput.toString();
                String result = evaluateExpression(expression);
                history.add(expression + " = " + result); // Add to history
                updateDisplay(result);
                currentInput.setLength(0);
                currentInput.append(result);
            } catch (Exception e) {
                updateDisplay("Error");
                currentInput.setLength(0);
            }
        } else if (viewId == R.id.btn_toggle_sign) {
            toggleSign();
        } else {
            // Check for consecutive operators
            if (isOperator(buttonText.charAt(0))) {
                if (currentInput.length() == 0 || isOperator(currentInput.charAt(currentInput.length() - 1))) {
                    return; // Ignore consecutive operators
                }
            }
            currentInput.append(buttonText);
            updateDisplay(currentInput.toString());
        }
    }

    /**
     * Updates the calculator display with the given text.
     *
     * @param text The text to display.
     */
    private void updateDisplay(String text) {
        display.setText(text);
    }

    /**
     * Clears the current input and resets the display to "0".
     */
    private void clearInput() {
        currentInput.setLength(0);
        updateDisplay("0");
    }

    /**
     * Deletes the last character from the current input.
     */
    private void deleteLastCharacter() {
        if (currentInput.length() > 0) {
            currentInput.deleteCharAt(currentInput.length() - 1);
        }
        updateDisplay(currentInput.length() > 0 ? currentInput.toString() : "0");
    }

    /**
     * Toggles the sign of the current input (positive to negative or vice versa).
     */
    private void toggleSign() {
        if (currentInput.length() > 0) {
            if (currentInput.charAt(0) == '-') {
                currentInput.deleteCharAt(0);
            } else {
                currentInput.insert(0, "-");
            }
            updateDisplay(currentInput.toString());
        }
    }

    /**
     * Appends the given text to the current input, preventing consecutive operators.
     *
     * @param text The text to append.
     */
    private void appendToInput(String text) {
        if (isOperator(text.charAt(0))) {
            if (currentInput.length() == 0 || isOperator(currentInput.charAt(currentInput.length() - 1))) {
                return;
            }
        }
        currentInput.append(text);
        updateDisplay(currentInput.toString());
    }

    /**
     * Evaluates the current expression and displays the result.
     */
    private void evaluateAndDisplayResult() {
        try {
            String expression = currentInput.toString();
            String result = evaluateExpression(expression);
            history.add(expression + " = " + result);
            updateDisplay(result);
            currentInput.setLength(0);
            currentInput.append(result);
        } catch (Exception e) {
            updateDisplay("Error");
            currentInput.setLength(0);
        }
    }

    /**
     * Evaluates the given arithmetic expression using a stack-based algorithm.
     *
     * @param expression The arithmetic expression to evaluate.
     * @return The result of the evaluation.
     */
    private String evaluateExpression(String expression) {
        Stack<Double> numbers = new Stack<>();
        Stack<Character> operators = new Stack<>();

        int i = 0;
        while (i < expression.length()) {
            char c = expression.charAt(i);

            if (Character.isDigit(c) || c == '.') {
                StringBuilder num = new StringBuilder();
                while (i < expression.length() && (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    num.append(expression.charAt(i));
                    i++;
                }
                numbers.push(Double.parseDouble(num.toString()));
            } else if (c == '(') {
                operators.push(c);
                i++;
            } else if (c == ')') {
                while (!operators.isEmpty() && operators.peek() != '(') {
                    numbers.push(applyOperator(operators.pop(), numbers.pop(), numbers.pop()));
                }
                operators.pop();
                i++;
            } else if (isOperator(c)) {
                while (!operators.isEmpty() && precedence(operators.peek()) >= precedence(c)) {
                    numbers.push(applyOperator(operators.pop(), numbers.pop(), numbers.pop()));
                }
                operators.push(c);
                i++;
            } else {
                i++;
            }
        }

        while (!operators.isEmpty()) {
            numbers.push(applyOperator(operators.pop(), numbers.pop(), numbers.pop()));
        }

        return numbers.pop().toString();
    }

    /**
     * Checks if the given character is an operator.
     *
     * @param c The character to check.
     * @return True if the character is an operator; false otherwise.
     */
    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == 'x' || c == '/' || c == '%';
    }

    /**
     * Returns the precedence of the given operator.
     *
     * @param operator The operator to check.
     * @return The precedence of the operator.
     */
    private int precedence(char operator) {
        switch (operator) {
            case '+':
            case '-':
                return 1;
            case 'x':
            case '/':
            case '%':
                return 2;
            default:
                return -1;
        }
    }

    /**
     * Applies the given operator to two operands.
     *
     * @param operator The operator to apply.
     * @param b The second operand.
     * @param a The first operand.
     * @return The result of the operation.
     */
    private double applyOperator(char operator, double b, double a) {
        switch (operator) {
            case '+': return a + b;
            case '-': return a - b;
            case 'x': return a * b;
            case '/':
                if (b == 0) throw new ArithmeticException("Division by zero");
                return a / b;
            case '%': return a % b;
            default: throw new IllegalArgumentException("Invalid operator: " + operator);
        }
    }

    /**
     * Displays a dialog showing the calculation history.
     */
    private void showHistoryDialog() {
        if (history.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Memory")
                    .setMessage("No history available.")
                    .setPositiveButton("OK", null)
                    .show();
        } else {
            StringBuilder historyText = new StringBuilder();
            for (String entry : history) {
                historyText.append(entry).append("\n");
            }
//            updateDisplay(historyText.toString())
            new AlertDialog.Builder(this)
                    .setTitle("Memory")
                    .setMessage(historyText.toString())
                    .setPositiveButton("OK", null)
                    .show();
        }
    }
}

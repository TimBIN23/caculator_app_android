package com.main.calculator;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private TextView display;                    // Display for showing input/output
    private final StringBuilder currentInput = new StringBuilder(); // Current user input
    private boolean isNewInput = true;           // Tracks if a new input sequence started
    private boolean hasDecimal = false;          // Tracks if the current number already has a decimal
    private boolean lastInputIsOperator = false; // Prevents consecutive operators

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        display = findViewById(R.id.display); // Reference to the display TextView
        // Number button IDs
        int[] numberButtons = {
                R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
                R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9
        };
        // Operator button IDs
        int[] operatorButtons = {
                R.id.btn_add, R.id.btn_subtract, R.id.btn_divide, R.id.btn_multiply
        };

        // Assign listeners to number buttons
        for (int id : numberButtons) {
            findViewById(id).setOnClickListener(this::handleNumberClick);
        }

        // Assign listeners to operator buttons
        for (int id : operatorButtons) {
            findViewById(id).setOnClickListener(this::handleOperatorClick);
        }
        // Assign listeners to specific buttons
        findViewById(R.id.btn_dot).setOnClickListener(this::handleDecimalClick);
        findViewById(R.id.btn_clear).setOnClickListener(v -> resetCalculator());
        findViewById(R.id.btn_equals).setOnClickListener(v -> displayCalculationResult());
    }

    /**
     * Handles clicks on number buttons.
     * Appends the clicked number to the current input.
     */
    private void handleNumberClick(View v) {
        Button button = (Button) v;
        if (isNewInput) {
            currentInput.setLength(0); // Clear input if it's a new sequence
            isNewInput = false;
        }
        currentInput.append(button.getText()); // Append the number
        display.setText(currentInput.toString()); // Update the display
        lastInputIsOperator = false; // Reset the operator flag
    }

    /**
     * Handles clicks on the decimal button.
     * Ensures only one decimal point is added per number.
     */
    private void handleDecimalClick(View v) {
        if (!hasDecimal && !lastInputIsOperator) {
            if (currentInput.length() == 0 || isNewInput) {
                currentInput.append("0"); // Add leading zero if empty
            }
            currentInput.append("."); // Append decimal point
            display.setText(currentInput.toString()); // Update display
            hasDecimal = true; // Set decimal flag
            isNewInput = false; // Reset new input flag
        }
    }

    /**
     * Handles clicks on operator buttons.
     * Ensures no consecutive operators are added.
     */
    private void handleOperatorClick(View v) {
        if (currentInput.length() == 0 || lastInputIsOperator) return; // Ignore invalid clicks

        Button button = (Button) v;
        currentInput.append(" ").append(button.getText()).append(" "); // Append operator with spaces
        display.setText(currentInput.toString()); // Update display

        isNewInput = false; // Reset new input flag
        hasDecimal = false; // Reset decimal flag
        lastInputIsOperator = true; // Set operator flag
    }

    /**
     * Clears the display and resets all flags.
     */
    private void resetCalculator() {
        currentInput.setLength(0); // Clear input buffer
        display.setText("0"); // Reset display to default
        isNewInput = true; // Reset new input flag
        hasDecimal = false; // Reset decimal flag
        lastInputIsOperator = false; // Reset operator flag
    }

    /**
     * Calculates the result of the current expression.
     * Handles exceptions for invalid input.
     */
    private void displayCalculationResult() {
        try {
            String expression = currentInput.toString(); // Get the current expression
            double result = evaluateExpression(expression); // Evaluate the expression
            display.setText(String.valueOf(result)); // Display the result
            currentInput.setLength(0); // Clear the input
            currentInput.append(result); // Store the result for further operations
            isNewInput = true; // Set new input flag
            hasDecimal = (String.valueOf(result).contains(".")); // Update decimal flag
            lastInputIsOperator = false; // Reset operator flag
        } catch (Exception e) {
            display.setText("Error"); // Display error message
            isNewInput = true; // Reset new input flag
        }
    }

    /**
     * Evaluates the given arithmetic expression using two stacks.
     *
     * @param expression The expression to evaluate.
     * @return The result of the evaluation.
     */
    private double evaluateExpression(String expression) {
        String[] tokens = expression.split(" "); // Split the expression into tokens
        Stack<Double> numbers = new Stack<>(); // Stack for numbers
        Stack<String> operators = new Stack<>(); // Stack for operators

        for (String token : tokens) {
            if (token.matches("\\d+(\\.\\d+)?")) { // If the token is a number
                numbers.push(Double.parseDouble(token));
            } else if (token.matches("[+\\-*/]")) { // If the token is an operator
                while (!operators.isEmpty() && precedence(operators.peek()) >= precedence(token)) {
                    performCalculation(numbers, operators.pop());
                }
                operators.push(token);
            }
        }

        // Process remaining operators
        while (!operators.isEmpty()) {
            performCalculation(numbers, operators.pop());
        }

        return numbers.pop(); // Final result
    }

    /**
     * Determines the precedence of an operator.
     *
     * @param operator The operator.
     * @return The precedence value.
     */
    private int precedence(String operator) {
        switch (operator) {
            case "*": return 2;
            case "/": return 2;
            case "+": return 1;
            case "-": return 1;
            default: return 0;
        }
    }

    /**
     * Performs a computation using the given operator and numbers.
     *
     * @param numbers  The stack of numbers.
     * @param operator The operator to apply.
     */
    private void performCalculation(Stack<Double> numbers, String operator) {
        if (numbers.size() < 2) return; // Ensure sufficient operands

        double b = numbers.pop(); // Second operand
        double a = numbers.pop(); // First operand
        double result = 0;

        switch (operator) {
            case "+": result = a + b; break;
            case "-": result = a - b; break;
            case "/": result = (b == 0) ? 0 : a / b; break; // Handle division by zero
            case "*": result = a * b; break;
        }

        numbers.push(result); // Push the result back onto the stack
    }
}

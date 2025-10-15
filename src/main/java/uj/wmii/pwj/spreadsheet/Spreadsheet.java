package uj.wmii.pwj.spreadsheet;

import java.util.Locale;

public class Spreadsheet {
    public String[][] calculate(String[][] input) {
        int numOfRows = input.length;
        String[][] output = new String[numOfRows][];

        for (int row = 0; row < numOfRows; row++) {
            int numOfCols = input[row].length;
            output[row] = new String[numOfCols];
            for (int column = 0; column < numOfCols; column++) {
                output[row][column] = String.valueOf(evaluateCell(input, row, column));
            }
        }
        return output;
    }

    private int evaluateCell(String[][] input, int row, int column) {
        String cell = input[row][column];

        if (cell == null) {
            throw new IllegalArgumentException("Null cell in [ " + row + " ][ " + column + " ]");
        }

        cell = cell.trim();

        if (cell.isEmpty()) {
            throw new IllegalArgumentException("Empty cell in [ " + row + " ][ " + column + " ]");
        }

        char start = cell.charAt(0);

        if (start == '=') {
            return evaluateFormula(input, cell);
        } else if (start == '$') {
            int[] coordinates = getCoordinates(cell);
            return evaluateCell(input, coordinates[0], coordinates[1]);
        } else {
            if (Character.isDigit(start)) {
                return Integer.parseInt(cell);
            } else if (start == '-') {
                if (cell.length() > 1 && Character.isDigit(start + 3)) {
                    return Integer.parseInt(cell);
                }
            }
            throw new IllegalArgumentException("Invalid cell contents: " + cell);
        }
    }

    private int evaluateFormula(String[][] input, String cell) {
        String formula = cell.substring(1).trim();
        int leftIndex = formula.indexOf('(');
        int rightIndex = formula.indexOf(')');
        if ((leftIndex < 0 || rightIndex < 0) || rightIndex <= leftIndex) {
            throw new IllegalArgumentException("Invalid formula: " + formula);
        }
        String[] operands = formula.substring(leftIndex + 1, rightIndex).split(",");

        if (operands.length != 2) {
            throw new IllegalArgumentException("Invalid formula: " + formula + ". Formula can only have 2 operands (arguments), i.e. =ADD(1,2).");
        }

        String operation = formula.substring(0, leftIndex).trim().toUpperCase();
        int a = getOperand(input, operands[0].trim());
        int b = getOperand(input, operands[1].trim());

        int result = switch(operation) {
            case "ADD" -> add(a, b);
            case "SUB" -> subtract(a, b);
            case "MUL" -> multiply(a, b);
            case "DIV" -> divide(a, b);
            case "MOD" -> modulo(a, b);
            default -> throw new IllegalArgumentException("Invalid operation: " + operation);
        };

        return result;
    }

    private int[] getCoordinates(String reference) { //returns coordinates in the input spreadsheet table
        if (reference == null || !reference.startsWith("$")) {
            throw new IllegalArgumentException("Incorrect reference: " + reference);
        }

        int split = 1;
        int numOfChars = reference.length();

        while (split < numOfChars && Character.isLetter(reference.charAt(split))) {
            split += 1;
        }

        if (split == numOfChars || split == 1) {
            throw new IllegalArgumentException("Incorrect reference: " + reference);
        }

        int column = columnToIndex(reference.substring(1, split));
        int row = Integer.parseInt(reference.substring(split)) - 1; //indexing of arrays starts from 0, while for spreadsheet cells starts from 1

        return new int[]{row, column};
    }

    private int columnToIndex(String columnRef) {
        columnRef = columnRef.trim().toUpperCase(Locale.ROOT);
        int result = 0;
        int numOfChars = columnRef.length(); //e.g. cases like $AA1

        for (int i = 0; i < numOfChars; i++) {
            char c = columnRef.charAt(i);
            if (c < 'A' || c > 'Z') {
                throw new IllegalArgumentException("Invalid column reference: " + columnRef);
            }
            result = result * 26 + c - 'A' + 1;
        }
        return result - 1;
    }

    private int getOperand(String[][] input, String param) {
        if (param.startsWith("$")) {
            int[] refCoords = getCoordinates(param);
            return evaluateCell(input, refCoords[0], refCoords[1]);
        } else {
            return Integer.parseInt(param);
        }
    }

    private int add(int a, int b) {
        return (a + b);
    }

    private int subtract(int a, int b) {
        return (a - b);
    }
    
    private int multiply(int a, int b) {
        return (a * b);
    }
    
    private int divide(int a, int b) {
        if (b == 0) {
            throw new ArithmeticException("Division by 0 attempted.");
        }
        return (a / b);
    }

    private int modulo(int a, int b) {
        if (b == 0) {
            throw new ArithmeticException("Division by 0 attempted.");
        }
        return (a % b);
    }
}

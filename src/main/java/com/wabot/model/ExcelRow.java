package com.wabot.model;

public class ExcelRow {
    private final int index;
    private final String number;
    private final String message;

    public ExcelRow(int index, String number, String message) {
        this.index = index;
        this.number = number;
        this.message = message;
    }

    public int getIndex() {
        return index;
    }

    public String getNumber() {
        return number;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ExcelRow other)) {
            return false;
        }
        return other.getNumber().equals(number);
    }

    @Override
    public int hashCode() {
        return this.number.hashCode();
    }
}

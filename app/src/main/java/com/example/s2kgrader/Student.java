package com.example.s2kgrader;

public class Student {

    private String name;
    private int rollno;
    private long phoneno;
    private String answer;

    public Student() {

    }

    public Student(String name, int rollno, long phoneno, String answer) {
        this.name = name;
        this.rollno = rollno;
        this.phoneno = phoneno;
        this.answer = answer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRollno() {
        return rollno;
    }

    public void setRollno(int rollno) {
        this.rollno = rollno;
    }

    public long getPhoneno() {
        return phoneno;
    }

    public void setPhoneno(long phoneno) {
        this.phoneno = phoneno;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}

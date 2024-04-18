package com.example.demo.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class Student {
    private String name;
    private String place;
    private int phone;
    private int GPA;
}

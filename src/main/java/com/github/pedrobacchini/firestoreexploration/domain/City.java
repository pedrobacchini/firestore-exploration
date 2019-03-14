package com.github.pedrobacchini.firestoreexploration.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class City {

    private String name;
    private String state;
    private String country;
    private Boolean capital;
    private Long population;
}

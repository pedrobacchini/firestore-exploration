package com.github.pedrobacchini.firestoreexploration.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.ToString;

@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CityWeather {

    private Integer temp;
    private String conditions;

    public Integer getTemp() {
        return temp;
    }

    public void setTemp(Integer temp) {
        this.temp = temp;
    }

    public String getConditions() {
        return conditions;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions;
    }
}

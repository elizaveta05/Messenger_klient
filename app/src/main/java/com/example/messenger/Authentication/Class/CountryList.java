package com.example.messenger.Authentication.Class;

import java.util.ArrayList;
import java.util.List;

public class CountryList {

    public static List<String> getCountries() {
        List<String> countries = new ArrayList<>();
        countries.add("Выберите страну");
        countries.add("Россия");
        countries.add("США");
        countries.add("Китай");
        countries.add("Бразилия");
        countries.add("Германия");
        countries.add("Индия");
        countries.add("Австралия");
        return countries;
    }
}

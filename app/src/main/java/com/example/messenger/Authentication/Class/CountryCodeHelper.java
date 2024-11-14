package com.example.messenger.Authentication.Class;

public class CountryCodeHelper {
    public static String getCountryCode(String country) {
        switch (country) {
            case "Россия":
                return "7";
            case "США":
                return "1";
            case "Китай":
                return "86";
            case "Бразилия":
                return "55";
            case "Германия":
                return "49";
            case "Индия":
                return "91";
            case "Австралия":
                return "61";
            default:
                return "";
        }
    }
}

package com.budgetmanager.utils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Calcule les jours fériés français (métropole) pour une année donnée,
 * y compris les fêtes mobiles basées sur la date de Pâques.
 */
public class HolidayUtils {

    /**
     * Retourne les jours fériés d'un mois donné.
     *
     * @param year  année (ex: 2026)
     * @param month mois indexé à partir de 0 (Calendar.JANUARY == 0)
     * @return map jour-du-mois -> nom du jour férié
     */
    public static Map<Integer, String> getHolidaysForMonth(int year, int month) {
        Map<Integer, String> result = new HashMap<>();
        for (Map.Entry<String, String> entry : getHolidaysForYear(year).entrySet()) {
            String[] parts = entry.getKey().split("-");
            int m = Integer.parseInt(parts[1]);
            int d = Integer.parseInt(parts[2]);
            if (m == month) {
                result.put(d, entry.getValue());
            }
        }
        return result;
    }

    /**
     * @return map "yyyy-M-d" (mois indexé à 0) -> nom du jour férié.
     */
    public static Map<String, String> getHolidaysForYear(int year) {
        Map<String, String> holidays = new HashMap<>();

        addFixed(holidays, year, Calendar.JANUARY, 1, "Jour de l'An");
        addFixed(holidays, year, Calendar.MAY, 1, "Fête du Travail");
        addFixed(holidays, year, Calendar.MAY, 8, "Victoire 1945");
        addFixed(holidays, year, Calendar.JULY, 14, "Fête Nationale");
        addFixed(holidays, year, Calendar.AUGUST, 15, "Assomption");
        addFixed(holidays, year, Calendar.NOVEMBER, 1, "Toussaint");
        addFixed(holidays, year, Calendar.NOVEMBER, 11, "Armistice 1918");
        addFixed(holidays, year, Calendar.DECEMBER, 25, "Noël");

        Calendar easter = computeEaster(year);
        addFromEaster(holidays, easter, 1, "Lundi de Pâques");
        addFromEaster(holidays, easter, 39, "Ascension");
        addFromEaster(holidays, easter, 50, "Lundi de Pentecôte");

        return holidays;
    }

    private static void addFixed(Map<String, String> holidays, int year, int month, int day, String name) {
        holidays.put(year + "-" + month + "-" + day, name);
    }

    private static void addFromEaster(Map<String, String> holidays, Calendar easter, int offsetDays, String name) {
        Calendar c = (Calendar) easter.clone();
        c.add(Calendar.DAY_OF_MONTH, offsetDays);
        holidays.put(c.get(Calendar.YEAR) + "-" + c.get(Calendar.MONTH) + "-" + c.get(Calendar.DAY_OF_MONTH), name);
    }

    /**
     * Algorithme de Gauss / Meeus pour le dimanche de Pâques (calendrier grégorien).
     */
    private static Calendar computeEaster(int year) {
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * m + 114) / 31; // 3 = mars, 4 = avril
        int day = ((h + l - 7 * m + 114) % 31) + 1;

        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(year, month - 1, day);
        return cal;
    }
}

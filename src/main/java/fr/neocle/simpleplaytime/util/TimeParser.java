package fr.neocle.simpleplaytime.util;

public class TimeParser {
    public static long parseTimeString(String input) throws IllegalArgumentException {
        long totalMillis = 0L;
        StringBuilder number = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isDigit(c)) {
                number.append(c);
            } else {
                if (number.length() == 0)
                    throw new IllegalArgumentException("Missing number before unit: " + c);

                long value = Long.parseLong(number.toString());
                number.setLength(0);

                switch (c) {
                    case 'y':
                        totalMillis += value * 365L * 24 * 60 * 60 * 1000;
                        break;
                    case 'M':
                        totalMillis += value * 30L * 24 * 60 * 60 * 1000;
                        break;
                    case 'w':
                        totalMillis += value * 7L * 24 * 60 * 60 * 1000;
                        break;
                    case 'd':
                        totalMillis += value * 24L * 60 * 60 * 1000;
                        break;
                    case 'h':
                        totalMillis += value * 60L * 60 * 1000;
                        break;
                    case 'm':
                        totalMillis += value * 60L * 1000;
                        break;
                    case 's':
                        totalMillis += value * 1000L;
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown time unit: " + c);
                }
            }
        }
        if (number.length() > 0) {
            throw new IllegalArgumentException("Dangling number without unit at end");
        }
        return totalMillis;
    }
}

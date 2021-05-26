package main.ServiceFirst;

import java.util.Random;

public class NameGenerator {
    private static final int diffBetweenAZ = 25;
    private static final int charValueOfA = 97;
    private String lastGeneratedName = "";
    int nameLength;

    char[] vowels = {
            'a', 'e', 'i', 'o', 'u'
    };

    public NameGenerator(int lengthOfName) {
        this.nameLength = lengthOfName;
    }

    public String getName() {
        while (true) {
            Random randomNumberGenerator = new Random();

            char[] charArrayName = new char[nameLength];

            for (int i = 0; i < nameLength; i++) {
                if (isOddIndex(i)) {
                    charArrayName[i] = getVowel(randomNumberGenerator);
                } else {
                    charArrayName[i] = getConsonant(randomNumberGenerator);
                }
            }

            charArrayName[0] = (char) Character
                    .toUpperCase(charArrayName[0]);

            String currentGeneratedName = new String(charArrayName);

            if (!currentGeneratedName.equals(lastGeneratedName)) {
                lastGeneratedName = currentGeneratedName;
                return currentGeneratedName;
            }

        }

    }

    private boolean isOddIndex(int i) {
        return i % 2 == 0;
    }

    private char getConsonant(Random randomNumberGenerator) {
        while (true) {
            char currentCharacter = (char) (randomNumberGenerator
                    .nextInt(diffBetweenAZ) + charValueOfA);
            if (currentCharacter == 'a' || currentCharacter == 'e'
                    || currentCharacter == 'i' || currentCharacter == 'o'
                    || currentCharacter == 'u') { }
            else {
                return currentCharacter;
            }
        }
    }

    private char getVowel(Random randomNumberGenerator) {
        return vowels[randomNumberGenerator.nextInt(vowels.length)];
    }
}

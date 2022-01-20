package utils.shop

class Upgrades {
    companion object {

        /**
         * Liczba roznych typow ulepszen; nalezy ja zmieniac wraz z rozbudowywaniem oferty ulepszen
         */
        const val n = 8

        fun getTiers(): IntArray {
            return IntArray(n){0}
        }

        fun getValues(): IntArray {
            return IntArray(n)
        }

        fun getCosts(): IntArray {
            return IntArray(n)
        }

        /**
         * Nazwy typow ulepszen, czysto estetyczne
         */
        fun getNames(): Array<String> {
            return arrayOf(
                "Inteligentne IDE\nLinijki na klik.",
                "Podswietlana klawiatura\nModyfikator linijek na klik.",
                "Zmysl developera\nSzansa na krytyczne klik.",
                "Uczenie maszynowe\nLinijki na sek.",
                "Korpo szkolenia\nModyfikator linijek na sek. Zespolu",
                "Rozbudowa biura\nMiejsce na dodatkowego programiste w Zespole",
                "Udzialy na gieldzie\nModyfikator kasy za projekty",
                "Dealer ziaren\nModyfikator kupowanej kofeiny"
            )
        }

        /**
         * Wartosci wyszczegolnionych ulepszen, zalezne od Tieru
         */
        fun calcValues(tiers: IntArray): IntArray {
            val values = IntArray(n)
            values[0] = 10 + (tiers[0] * (tiers[0] + 1)) * 5
            values[1] = 2 + tiers[1]
            values[2] = 5 + tiers[2]
            values[3] = 1 + tiers[3] * tiers[3]
            values[4] = 10 + tiers[4] * 10
            values[5] = 1 + tiers[5]
            values[6] = 10 + tiers[6] * 10
            values[7] = 10 + tiers[7] * 10
            return values
        }

        /**
         * Ceny wyszczegolnionych ulepszen, zalezne od Tieru
         */
        fun calcCosts(tiers: IntArray): IntArray {
            val costs = IntArray(n)
            costs[0] = 100 + (tiers[0] * (tiers[0] + 1)) * 100
            costs[1] = 100 + (tiers[1] * (tiers[1] + 1)) * 100
            costs[2] = 100 + (tiers[2] * (tiers[2] + 1)) * 100
            costs[3] = 100 + (tiers[3] * (tiers[3] + 1)) * 100
            costs[4] = 100 + (tiers[4] * (tiers[4] + 1)) * 100
            costs[5] = 100 + (tiers[5] * (tiers[5] + 1)) * 100
            costs[6] = 100 + (tiers[6] * (tiers[6] + 1)) * 100
            costs[7] = 100 + (tiers[7] * (tiers[7] + 1)) * 100
            return costs
        }
    }
}
package utils.shop

class Caffeine {
    companion object {

        /**
         * Liczba ofert kofeinowych; nalezy ja zmieniac wraz z rozbudowywaniem oferty
         */
        const val n = 6

        /**
         * Nazwy ofert, czysto estetyczne
         */
        fun getNames(): Array<String> {
            return arrayOf(
                "Kubek kawy\nKofeina: 1",
                "Zgrzewka energoli\nKofeina: 20",
                "Tir z eksportu\nKofeina: 400",
                "Podziemny magazyn\nKofeina: 8000",
                "Ekspres\nKofeina: 7x 30/dzien",
                "Szlak handlowy\nKofeina: 7x 500/dzien"
            )
        }

        /**
         * Wartosci ofert w walucie Kofeiny
         */
        fun getOffers(): IntArray {
            val offers = IntArray(n)
            offers[0] = 1
            offers[1] = 20
            offers[2] = 400
            offers[3] = 8000
            offers[4] = 30
            offers[5] = 500
            return offers
        }

        /**
         * Ceny poszczegolnych ofert kofeinowych
         */
        fun getCosts(): IntArray {
            val costs = IntArray(n)
            costs[0] = 20
            costs[1] = 300
            costs[2] = 5000
            costs[3] = 100000
            costs[4] = 1000
            costs[5] = 10000
            return costs
        }
    }
}
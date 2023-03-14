package com.mikuac.yuri.dto

data class EpicFreeGameDTO(
    val elements: List<Elements>
) {
    data class Elements(
        val customAttributes: List<CustomAttribute>,
        val description: String,
        val catalogNs: CatalogNs,
        val keyImages: List<KeyImage>,
        val price: Price,
        val promotions: Promotions?,
        val seller: Seller,
        val title: String,
    ) {

        data class CatalogNs(
            val mappings: List<Mapping>
        )

        data class Mapping(
            val pageSlug: String,
        )

        data class CustomAttribute(
            val key: String,
            val value: String
        )

        data class KeyImage(
            val type: String,
            val url: String
        )

        data class Price(
            val totalPrice: TotalPrice
        ) {
            data class TotalPrice(
                val fmtPrice: FmtPrice,
            ) {
                data class FmtPrice(
                    val originalPrice: String
                )
            }
        }

        data class Promotions(
            val promotionalOffers: List<PromotionalOffers>,
            val upcomingPromotionalOffers: List<UpcomingPromotionalOffer>
        ) {
            data class PromotionalOffers(
                val promotionalOffers: List<PromotionalOffer>
            )

            data class UpcomingPromotionalOffer(
                val promotionalOffers: List<PromotionalOffer>
            )

            data class PromotionalOffer(
                val startDate: String,
                val endDate: String
            )
        }

        data class Seller(
            val name: String
        )

    }
}
package ca.qc.cstj.andromia.models

import kotlinx.serialization.Serializable

@Serializable
data class Pagination<T>(
        val _links: Liens,
        val count: Int,
        val total: Int,
        val page: Int,
        val items: List<T>)
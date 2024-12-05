package com.dna.beyoureyes

import io.grpc.okhttp.internal.framed.Header
import retrofit2.http.Body


data class ApiResponse (
    val response: Response
) {

    data class Response(
        val header: Header,
        val body: Body
    )

    data class Header(
        val resultCode: String,
        val resultMsg: String,
        val type: String
    )


    data class Body(
        val items: List<Item>,
        val totalCount: String,
        val numOfRows: String,
        val pageNo: String
    )

    data class Item(
        val foodNm: String,
        val nutConSrtrQua: String,
        val enerc: String,
        val water: String?,
        val prot: String,
        val fatce: String,
        val ash: String?,
        val chocdf: String,
        val sugar: String,
        val fibtg: String?,
        val ca: String?,
        val fe: String?,
        val p: String?,
        val k: String?,
        val nat: String,
        val vitaRae: String?,
        val retol: String?,
        val cartb: String?,
        val thia: String?,
        val ribf: String?,
        val nia: String?,
        val vitc: String?,
        val vitd: String?,
        val chole: String,
        val fasat: String,
        val fatrn: String,
        val srcCd: String,
        val servSize: String,
        val foodSize: String

    )
}



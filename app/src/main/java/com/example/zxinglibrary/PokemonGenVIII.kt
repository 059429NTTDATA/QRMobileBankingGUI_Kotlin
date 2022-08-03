package com.example.zxinglibrary

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.beust.klaxon.Klaxon


class PokemonGenVIII : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pokemon_gen_viii)

        callPokemonApi()
    }

    private fun callPokemonApi() {
        val queue = Volley.newRequestQueue(this)
        val offset = 0
        val limit = 1
        val url = "https://pokeapi.co/api/v2/pokemon/?offset=${offset}&limit=${limit}"
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                val obj = Klaxon().parse<DataClassUtils.PokemonListModel>(response)
                val pokemonResults = obj?.results
                pokemonResults?.forEachIndexed { index, element ->
                    println("$index pokemon name : ${element.name}, pokemon url : ${element.url}")
                    val urlPoke = element.url
                    val stringRequest = StringRequest(Request.Method.GET, urlPoke, { response ->
                        val pokemonObj = Klaxon().parse<DataClassUtils.PokemonAPIModel>(response)
                        val officialArtworkUrlFrontDefault =
                            pokemonObj?.sprites?.other?.officialArtwork?.frontDefault
                        DownloadImgUtils.downloadImgFromUrlAndSetToImageView(
                            findViewById<ImageView>(
                                R.id.iv_pokemon_display
                            ), officialArtworkUrlFrontDefault
                        )
                    }, {
                        Toast.makeText(
                            this,
                            "PokemonAPI Error",
                            Toast.LENGTH_LONG
                        ).show()
                    })
                    queue.add(stringRequest)
                }
            },
            {
                Toast.makeText(
                    this,
                    "Pokemon Error",
                    Toast.LENGTH_LONG
                ).show()
            })

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }
}


package com.example.zxinglibrary

import com.beust.klaxon.*

class DataClassUtils {
    data class QRValueList(var list: ArrayList<QRValue>?)
    data class QRValue(var qrKey: String, var value: String, var machine: String, var clientKey: String, var generateQr: String)

    data class PokemonResults(var name: String, var url: String)
    data class PokemonListModel(
        var count: Int?,
        var next: String,
        var previous: String?,
        var results: ArrayList<PokemonResults>?
    )

    data class PokemonAPIModel(
        val abilities: List<Ability>,

        @Json(name = "base_experience")
        val baseExperience: Long,

        val forms: List<Species>,

        @Json(name = "game_indices")
        val gameIndices: List<GameIndex>,

        val height: Long,

        @Json(name = "held_items")
        val heldItems: List<Any?>,

        val id: Long,

        @Json(name = "is_default")
        val isDefault: Boolean,

        @Json(name = "location_area_encounters")
        val locationAreaEncounters: String,

        val moves: List<Move>,
        val name: String,
        val order: Long,

        @Json(name = "past_types")
        val pastTypes: List<Any?>,

        val species: Species,
        val sprites: Sprites,
        val stats: List<Stat>,
        val types: List<Type>,
        val weight: Long
    )

    data class Ability(
        val ability: Species,

        @Json(name = "is_hidden")
        val isHidden: Boolean,

        val slot: Long
    )

    data class Species(
        val name: String,
        val url: String
    )

    data class GameIndex(
        @Json(name = "game_index")
        val gameIndex: Long,

        val version: Species
    )

    data class Move(
        val move: Species,

        @Json(name = "version_group_details")
        val versionGroupDetails: List<VersionGroupDetail>
    )

    data class VersionGroupDetail(
        @Json(name = "level_learned_at")
        val levelLearnedAt: Long,

        @Json(name = "move_learn_method")
        val moveLearnMethod: Species,

        @Json(name = "version_group")
        val versionGroup: Species
    )

    data class GenerationV(
        @Json(name = "black-white")
        val blackWhite: Sprites
    )

    data class GenerationIv(
        @Json(name = "diamond-pearl")
        val diamondPearl: Sprites,

        @Json(name = "heartgold-soulsilver")
        val heartgoldSoulsilver: Sprites,

        val platinum: Sprites
    )

    data class Versions(
        @Json(name = "generation-i")
        val generationI: GenerationI,

        @Json(name = "generation-ii")
        val generationIi: GenerationIi,

        @Json(name = "generation-iii")
        val generationIii: GenerationIii,

        @Json(name = "generation-iv")
        val generationIv: GenerationIv,

        @Json(name = "generation-v")
        val generationV: GenerationV,

        @Json(name = "generation-vi")
        val generationVi: Map<String, Home>,

        @Json(name = "generation-vii")
        val generationVii: GenerationVii,

        @Json(name = "generation-viii")
        val generationViii: GenerationViii
    )

    data class Sprites(
        @Json(name = "back_default")
        val backDefault: String,

        @Json(name = "back_female")
        val backFemale: Any? = null,

        @Json(name = "back_shiny")
        val backShiny: String,

        @Json(name = "back_shiny_female")
        val backShinyFemale: Any? = null,

        @Json(name = "front_default")
        val frontDefault: String,

        @Json(name = "front_female")
        val frontFemale: Any? = null,

        @Json(name = "front_shiny")
        val frontShiny: String,

        @Json(name = "front_shiny_female")
        val frontShinyFemale: Any? = null,

        val other: Other? = null,
        val versions: Versions? = null,
        val animated: Sprites? = null
    )

    data class GenerationI(
        @Json(name = "red-blue")
        val redBlue: RedBlue,

        val yellow: RedBlue
    )

    data class RedBlue(
        @Json(name = "back_default")
        val backDefault: String,

        @Json(name = "back_gray")
        val backGray: String,

        @Json(name = "back_transparent")
        val backTransparent: String,

        @Json(name = "front_default")
        val frontDefault: String,

        @Json(name = "front_gray")
        val frontGray: String,

        @Json(name = "front_transparent")
        val frontTransparent: String
    )

    data class GenerationIi(
        val crystal: Crystal,
        val gold: Gold,
        val silver: Gold
    )

    data class Crystal(
        @Json(name = "back_default")
        val backDefault: String,

        @Json(name = "back_shiny")
        val backShiny: String,

        @Json(name = "back_shiny_transparent")
        val backShinyTransparent: String,

        @Json(name = "back_transparent")
        val backTransparent: String,

        @Json(name = "front_default")
        val frontDefault: String,

        @Json(name = "front_shiny")
        val frontShiny: String,

        @Json(name = "front_shiny_transparent")
        val frontShinyTransparent: String,

        @Json(name = "front_transparent")
        val frontTransparent: String
    )

    data class Gold(
        @Json(name = "back_default")
        val backDefault: String,

        @Json(name = "back_shiny")
        val backShiny: String,

        @Json(name = "front_default")
        val frontDefault: String,

        @Json(name = "front_shiny")
        val frontShiny: String,

        @Json(name = "front_transparent")
        val frontTransparent: String? = null
    )

    data class GenerationIii(
        val emerald: Emerald,

        @Json(name = "firered-leafgreen")
        val fireredLeafgreen: Gold,

        @Json(name = "ruby-sapphire")
        val rubySapphire: Gold
    )

    data class Emerald(
        @Json(name = "front_default")
        val frontDefault: String,

        @Json(name = "front_shiny")
        val frontShiny: String
    )

    data class Home(
        @Json(name = "front_default")
        val frontDefault: String,

        @Json(name = "front_female")
        val frontFemale: Any? = null,

        @Json(name = "front_shiny")
        val frontShiny: String,

        @Json(name = "front_shiny_female")
        val frontShinyFemale: Any? = null
    )

    data class GenerationVii(
        val icons: DreamWorld,

        @Json(name = "ultra-sun-ultra-moon")
        val ultraSunUltraMoon: Home
    )

    data class DreamWorld(
        @Json(name = "front_default")
        val frontDefault: String,

        @Json(name = "front_female")
        val frontFemale: Any? = null
    )

    data class GenerationViii(
        val icons: DreamWorld
    )

    data class Other(
        @Json(name = "dream_world")
        val dreamWorld: DreamWorld,

        val home: Home,

        @Json(name = "official-artwork")
        val officialArtwork: OfficialArtwork
    )

    data class OfficialArtwork(
        @Json(name = "front_default")
        val frontDefault: String
    )

    data class Stat(
        @Json(name = "base_stat")
        val baseStat: Long,

        val effort: Long,
        val stat: Species
    )

    data class Type(
        val slot: Long,
        val type: Species
    )
}
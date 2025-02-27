import kotlinx.html.*
import kotlinx.html.stream.createHTML
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


@Serializable
data class Country(
    val name: CountryName,
    val region: String,
    val population: Long,
    val area: Double
)

@Serializable
data class CountryName(
    val common: String
)


class CountryService {
    private val client = HttpClient.newHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    fun fetchCountries(): List<Country> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://restcountries.com/v3.1/all"))
            .GET()
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return json.decodeFromString(response.body())
    }
}


class CountryPage(private val countries: List<Country>) {
    fun generate(): String {
        val sortedByPopulation = countries.sortedByDescending { it.population }.take(10)
        val countryNames = countries.map { it.name.common }.take(10)
        val groupedByRegion = countries.groupBy { it.region }
        val filteredCountries = countries.filter { it.name.common.startsWith("P") }

        return createHTML().html {
            head {
                title("Datos de Países")
                style {
                    +"body { font-family: Arial, sans-serif; margin: 20px; padding: 10px; background-color: #f4f4f4; }"
                    +"h1, h2 { color: #333; }"
                    +"ul { background: white; padding: 15px; border-radius: 8px; box-shadow: 2px 2px 10px rgba(0,0,0,0.1); }"
                    +"li { margin-bottom: 5px; }"
                }
            }
            body {
                h1 { +"Información de Países" }

                section("top-population") {
                    h2 { +"Top 10 países por población" }
                    ul {
                        sortedByPopulation.forEach {
                            li { +"${it.name.common} - ${it.population} habitantes" }
                        }
                    }
                }

                section("country-names") {
                    h2 { +"Lista de 10 nombres de países" }
                    ul {
                        countryNames.forEach {
                            li { +it }
                        }
                    }
                }

                section("regions") {
                    h2 { +"Países agrupados por región" }
                    ul {
                        groupedByRegion.entries.forEach { (region, list) ->
                            li { +"$region: ${list.size} países" }
                        }
                    }
                }

                section("countries-p") {
                    h2 { +"Países que empiezan con 'P'" }
                    ul {
                        filteredCountries.forEach {
                            li { +it.name.common }
                        }
                    }
                }
            }
        }
    }
}


fun main() {
    val service = CountryService()
    val countries = service.fetchCountries()
    val htmlContent = CountryPage(countries).generate()

    println(htmlContent)
}

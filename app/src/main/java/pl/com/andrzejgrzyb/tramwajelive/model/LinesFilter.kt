package pl.com.andrzejgrzyb.tramwajelive.model

import androidx.annotation.ColorInt
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.converter.PropertyConverter


@Entity
data class LinesFilter(
    @Id
    var id: Long,
    @ColorInt
    var color: Int,
    @Convert(converter = StringsConverter::class, dbType = String::class)
    var lines: MutableSet<String>
) {
    fun addToLines(line: String) {
        lines.add(line)
    }
}

class StringsConverter : PropertyConverter<Set<String>, String> {
    private val SEMICOLON = ";"

    override fun convertToDatabaseValue(entityProperty: Set<String>?): String {
        return entityProperty?.joinToString(separator = SEMICOLON) ?: ""
    }

    override fun convertToEntityProperty(databaseValue: String?): Set<String> {
        return if (databaseValue.isNullOrEmpty()) HashSet()
        else databaseValue.split(SEMICOLON).toMutableSet()
    }

}
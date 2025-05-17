package com.ucasoft.komm.website

import mui.material.Box
import mui.material.Container
import mui.material.Typography
import mui.system.sx
import react.FC
import web.cssom.Auto
import web.cssom.Color
import web.cssom.Margin
import web.cssom.Padding
import web.cssom.TextAlign
import web.cssom.px

val CodeExamples = FC {
    Box {
        sx {
            padding = Padding(10.px, 0.px)
        }
        Container {
            Box {
                sx {
                    textAlign = TextAlign.center
                    marginBottom = 6.px
                }
                Typography {
                    sx {
                        color = Color("text.secondary")
                        maxWidth = 700.px
                        margin = Margin(0.px, Auto.auto)
                    }
                    +"KOMM generates extension functions for your objects, making mapping as simple as a function call."
                }
            }
            Code {
                title = "Example: Basic Mapping"
                code = """// Define your source and target classes
data class UserDto(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String?
)

@Mapper(source = UserDto::class)
data class User(
    val userId: Long,
    val firstName: String,
    val lastName: String,
    val email: String? = null
) {
    val fullName: String
        get() = "${'$'}firstName ${'$'}lastName"
}

// Generated extension function allows simple mapping
val userDto = UserDto(1, "John", "Doe", "john.doe@example.com")
val user = userDto.toUser() // Maps UserDto to User automatically

// Properties with different names can be mapped using annotations
@Mapper(source = UserDto::class)
data class UserWithAnnotations(
    @MappingFrom("id")
    val userId: Long,
    val firstName: String,
    val lastName: String,
    val email: String? = null
)
                """.trimIndent()
            }
            Code {
                title = "Example: Custom Converters"
                code = """// Define a converter for complex type transformations
class DateConverter : Converter<String, LocalDate> {
    override fun convert(source: String): LocalDate {
        return LocalDate.parse(source)
    }
}

data class EventDto(
    val id: Long,
    val title: String,
    val dateAsString: String
)

@Mapper(source = EventDto::class)
data class Event(
    val id: Long,
    val title: String,
    @MappingFrom("dateAsString")
    @MappingConverter(DateConverter::class)
    val date: LocalDate
)""".trimIndent()
            }
            Code {
                title = "Example: Collection Mapping with Iterable Plugin"
                code = """data class ItemDto(val id: Long, val name: String)
data class OrderDto(val id: Long, val items: List<ItemDto>)

@Mapper(source = ItemDto::class)
data class Item(val id: Long, val name: String)

@Mapper(source = OrderDto::class)
data class Order(
    val id: Long,
    val items: List<Item>
)

// With Iterable Plugin configured, collections are automatically mapped
val orderDto = OrderDto(1, listOf(ItemDto(1, "Item 1"), ItemDto(2, "Item 2")))
val order = orderDto.toOrder() // Items are automatically converted to target type""".trimIndent()
            }
        }
    }
}
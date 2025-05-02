package com.ucasoft.komm.simple.to

import com.ucasoft.komm.abstractions.KOMMResolver
import com.ucasoft.komm.annotations.KOMMMap
import com.ucasoft.komm.annotations.MapConfiguration
import com.ucasoft.komm.annotations.MapDefault
import com.ucasoft.komm.annotations.NullSubstitute
import com.ucasoft.komm.plugins.enum.annotations.KOMMEnum

@KOMMMap(to = [DestinationWithEnum::class])
data class SourceWithEnum(
    val name: String,
    val age: Int,
    val play: SourceEnum,
    @NullSubstitute(MapDefault(DirectionResolver::class))
    @KOMMEnum("OTHER")
    val direction: OtherSourceEnum?,
    val sharedEnum: SharedEnum
) {
    enum class SourceEnum {
        PING,
        PONG
    }
}

enum class OtherSourceEnum {
    UP,
    DOWN,
    LEFT,
    RIGHT
}

data class DestinationWithEnum(
    val name: String,
    val age: Int,
    val play: DestinationEnum,
    val direction: OtherDestinationEnum,
    val sharedEnum: SharedEnum?
) {
    enum class DestinationEnum {
        PING,
        PONG
    }
}

enum class OtherDestinationEnum {
    UP,
    DOWN,
    OTHER
}

enum class SharedEnum {
    HERE,
    THERE
}

class DirectionResolver(destination: DestinationWithEnum?) : KOMMResolver<DestinationWithEnum, OtherDestinationEnum>(destination) {

    override fun resolve() = OtherDestinationEnum.OTHER
}
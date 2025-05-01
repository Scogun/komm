package com.ucasoft.komm.simple.to

import com.ucasoft.komm.abstractions.KOMMConverter
import com.ucasoft.komm.annotations.KOMMMap
import com.ucasoft.komm.annotations.MapConvert

@KOMMMap(to = [DestinationWithEnum::class])
data class SourceWithEnum(
    val name: String,
    val age: Int,
    @MapConvert<SourceWithEnum, DestinationWithEnum, EnumConverter>(EnumConverter::class)
    val play: SourceEnum
) {
    enum class SourceEnum {
        PING,
        PONG
    }
}

data class DestinationWithEnum(
    val name: String,
    val age: Int,
    val play: DestinationEnum
) {
    enum class DestinationEnum {
        PING,
        PONG
    }
}

class EnumConverter(source: SourceWithEnum) : KOMMConverter<SourceWithEnum, SourceWithEnum.SourceEnum, DestinationWithEnum, DestinationWithEnum.DestinationEnum>(source) {
    override fun convert(sourceMember: SourceWithEnum.SourceEnum) = DestinationWithEnum.DestinationEnum.valueOf(sourceMember.name)
}
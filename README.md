# Kotlin Object Multiplatform Mapper (KOMM)

The **Kotlin Object Multiplatform Mapper** provides you a possibility to generate (via [KSP](https://github.com/google/ksp)) extension function to map one object to another.

---
* [Features](#features)
* [Usage](#usage)
  * [Add](#add-with-gradle)
    * [JVM project](#jvm-project)
    * [Multiplatform project](#multiplatform-project)
  * [Simple mapping](#simple-mapping)
  * [Configuration](#mapping-configuration)
  * [@MapFrom](#mapfrom-annotation)
  * [@MapConverter](#use-converter)
  * [@MapDefault](#use-resolver)
  * [@NullSubstitute](#use-nullsubstitute)
    * [Allow Not-Null Assertion](#mapping-configuration-1)
  * [Multi Sources](#multi-sources-support)
---

## Features
* Supports KSP Multiplatform
* Maps as constructor parameters as well as public properties with setter
* Supports properties types cast
* Supports Java objects get* functions
* Supports multi source classes with separated configurations
* Has next properties annotations:
  * Specify mapping from property with different name
  * Specify a converter to map data from source unusual way
  * Specify a resolver to map default values into properties
  * Specify null substitute to map nullable properties into not-nullable

## Usage
### Add with Gradle

#### JVM Project
```kotlin
plugins {
    id("com.google.devtools.ksp") version "1.9.22-1.0.17"
}

val kommVersion = "0.1.0"

depensencies {
    implementation("com.ucasoft.komm:komm-annotations:$kommVersion")
    ksp("com.ucasoft.komm:komm-processor:$kommVersion")
}
```
#### Multiplatform Project
```kotlin
plugins {
    id("com.google.devtools.ksp") version "1.9.22-1.0.17"
}

val kommVersion = "0.1.0"

kotlin {
    jvm {
        withJava()
    }
    js(IR) {
        nodejs()
    }
    // Add other platforms
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.ucasoft.komm:komm-annotations:$kommVersion")
            }
        }
        val jvmMain by getting
        val jsMain by getting
        // Init other sourceSets
    }
}

dependencies {
    add("kspJvm", "com.ucasoft.komm:komm-processor:$kommVersion")
    add("kspJs", "com.ucasoft.komm:komm-processor:$kommVersion")
    // Add other platforms like `kspAndroidNativeX64`, `kspLinuxX64`, `kspMingwX64` etc.
}
```

### Simple Mapping
#### Classes declaration
```kotlin
class SourceObject {

    val id = 150

    val intToString = 300

    val stringToInt = "250"
}

@KOMMMap(from = SourceObject::class)
data class DestinationObject(
    val id: Int,
    val stringToInt: Int
) {
    var intToString: String = ""
}
```
#### Generated extension function
```kotlin
fun SourceObject.toDestinationObject(): DestinationObject = DestinationObject(
    id = id,
    stringToInt = stringToInt.toInt()
).also { 
    it.intToString = intToString.toString()
}
```

### Mapping Configuration
### Classes declaration
```kotlin
@KOMMMap(
    from = SourceObject::class,
    config = MapConfiguration(
        tryAutoCast = false
    )
)
data class DestinationObject(
    val id: Int,
    val stringToInt: Int
) {
    var intToString: String = ""
}
```
### Generation result
```console
e: [ksp] com.ucasoft.komm.processor.exceptions.KOMMCastException: AutoCast is turned off! You have to use @MapConvert annotation to cast (stringToInt: Int) from (stringToInt: String)
```

### @MapFrom annotation
#### Classes declaration
```kotlin
class SourceObject {
    //...
    val userName = "user"
}

@KOMMMap(from = SourceObject::class)
data class DestinationObject(
    //...
    @MapFrom("userName")
    val name: String
) {
    var intToString: String = ""
}
```
#### Generated extension function
```kotlin
fun SourceObject.toDestinationObject(): DestinationObject = DestinationObject(
    //...
    name = userName
).also { 
    it.intToString = intToString.toString()
}
```

### Use Converter
#### Converter declaration
```kotlin
class CostConverter(source: SourceObject) : KOMMConverter<SourceObject, Double, String>(source) {

    override fun convert(sourceMember: Double) = "$sourceMember ${source.currency}"
}
```
#### Classes declaration
```kotlin
class SourceObject {
    //...
    val cost = 499.99
}

@KOMMMap(from = SourceObject::class)
data class DestinationObject(
    //...
    @MapConvert<SourceObject, CostConverter>(CostConverter::class)
    val cost: String
) {
    //...
    @MapConvert<SourceObject, CostConverter>(CostConverter::class, "cost")
    var otherCost: String = ""
}
```
#### Generated extension function
```kotlin
fun SourceObject.toDestinationObject(): DestinationObject = DestinationObject(
    //...
    cost = CostConverter(this).convert(cost)
).also { 
    //...
    it.otherCost = CostConverter(this).convert(cost)
}
```
### Use Resolver
#### Resolver declaration
```kotlin
class DateResolver(destination: DestinationObject?) : KOMMResolver<DestinationObject, Date>(destination) {
    
    override fun resolve(): Date = Date.from(Instant.now())
}
```
#### Classes declaration
```kotlin
@KOMMMap(from = SourceObject::class)
data class DestinationObject(
    //...
    @MapDefault<DateResolver>(DateResolver::class)
    val activeDate: Date
) {
    //...
    @MapDefault<DateResolver>(DateResolver::class)
    var otherDate: Date = Date.from(Instant.now())
}
```
#### Generated extension function
```kotlin
fun SourceObject.toDestinationObject(): DestinationObject = DestinationObject(
    //...
    activeDate = DateResolver(null).resolve()
).also { 
    //...
    it.otherDate = DateResolver(it).resolve()
}
```
### Use NullSubstitute
#### Mapping configuration
###### Classes declaration
```kotlin
@KOMMMap(
    from = SourceObject::class,
    config = MapConfiguration(
      allowNotNullAssertion = true
    )
)
data class DestinationObject(
    val id: Int
)
data class SourceObject(
    val id: Int?
)
```
###### Generated extension function
```kotlin
fun SourceObject.toDestinationObject(): DestinationObject = DestinationObject(
    id = id!!
)
```
###### Otherwise
```console
e: [ksp] com.ucasoft.komm.processor.exceptions.KOMMCastException: Auto Not-Null Assertion is not allowed! You have to use @NullSubstitute annotation for id property.
```
#### Resolver declaration
```kotlin
class IntResolver(destination: DestinationObject?): KOMMResolver<DestinationObject, Int>(destination) {

    override fun resolve() = 1
}
```
#### Classes declaration
```kotlin
@KOMMMap(
    from = SourceObject::class
)
data class DestinationObject(
    @NullSubatitute(MapDefault(IntResolver::class))
    val id: Int
) {
    @NullSubatitute(MapDefault(IntResolver::class), "id")
    var otherId: Int = 0
}
```
#### Generated extension function
```kotlin
fun SourceObject.toDestinationObject(): DestinationObject = DestinationObject(
    id = id ?: IntResolver(null).resolve()
).also {
    it.otherId = id ?: IntResolver(it).resolve()
}
```
### Multi Sources Support
#### Classes declaration
```kotlin
@KOMMMap(
    from = FirstSourceObject::class
)
@KOMMMap(
  from = SecondSourceObject::class
)
data class DestinationObject(
    @NullSubatitute(MapDefault(IntResolver::class), [FirstSourceObject::class])
    @MapFrom("userId", [SecondSourceObject::class])
    val id: Int
) {
    @NullSubatitute(MapDefault(IntResolver::class), "id", [FirstSourceObject::class])
    var otherId: Int = 0
}

data class FirstSourceObject(
  val id: Int?
)

data class SecondSourceObject(
    val userId: Int
)
```
#### Generated extension functions
```kotlin
fun FirstSourceObject.toDestinationObject(): DestinationObject = DestinationObject(
    id = id ?: IntResolver(null).resolve()
).also {
    it.otherId = id ?: IntResolver(it).resolve()
}

fun SecondSourceObject.toDestinationObject(): DestinationObject = DestinationObject(
  id = userId
)
```
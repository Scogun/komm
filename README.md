# Kotlin Object Multiplatform Mapper (KOMM)

The **Kotlin Object Multiplatform Mapper** provides you a possibility to generate (via [KSP](https://github.com/google/ksp)) extension function to map one object to another.

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Scogun_komm&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=Scogun_komm)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=Scogun_komm&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=Scogun_komm)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=Scogun_komm&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=Scogun_komm)  
![GitHub](https://img.shields.io/github/license/Scogun/komm?color=blue)  
![Publish workflow](https://github.com/Scogun/komm/actions/workflows/publish.yml/badge.svg)
[![Maven Central](https://img.shields.io/maven-central/v/com.ucasoft.komm/komm-annotations?label=KOMM-Annotations&color=blue)](https://search.maven.org/artifact/com.ucasoft.komm/komm-annotations)
[![Maven Central](https://img.shields.io/maven-central/v/com.ucasoft.komm/komm-processor?label=KOMM-Processоr&color=blue)](https://search.maven.org/artifact/com.ucasoft.komm/komm-processor)
[![Maven Central](https://img.shields.io/maven-central/v/com.ucasoft.komm/komm-plugins-iterable?label=KOMM-Plugins-Iterable&color=blue)](https://search.maven.org/artifact/com.ucasoft.komm/komm-plugins-iterable)
---
* [Features](#features)
* [Supported targets](#supported-targets)
* [Default plugins](#default-plugins)
* [Usage](#usage)
  * [Add](#add-with-gradle)
    * [JVM project](#jvm-project)
    * [Multiplatform project](#multiplatform-project)
  * [Simple mapping](#simple-mapping)
  * [Configuration](#mapping-configuration)
    * [Disable AutoCast](#disable-autocast)
    * [Change Convert Function Name](#change-convert-function-name)
  * [@MapName](#mapname-annotation)
  * [@MapConverter](#use-converter)
  * [@MapDefault](#use-resolver)
  * [@NullSubstitute](#use-nullsubstitute)
    * [Allow Not-Null Assertion](#mapping-configuration-1)
  * [Multi Sources](#multi-sources-support)
* [Plugins](#plugins)
  * [Iterable Plugin - Collections Mapping](#iterable-plugin---collections-mapping)
    * [Add](#add-with-gradle-1)
      * [JVM project](#jvm-project-1)
      * [Multiplatform project](#multiplatform-project-1)
    * [Allow NotNullAssertion](#allow-notnullassertion)
    * [NullSubstitute](#nullsubstitute)
  * [Exposed Plugin - ResultRow Mapping](#exposed-plugin---resultrow-mapping)
    * [Add](#add-with-gradle-2)
    * [Usage](#usage-1)
  * [Enum Plugin](#enum-plugin)
    * [Add](#add-with-gradle-3)
      * [JVM project](#jvm-project-3)
      * [Multiplatform project](#multiplatform-project-2)
    * [Usage](#usage-2)
---

## Features
* Supports KSP Multiplatform
* Maps as constructor parameters as well as public properties with setter
* Supports properties' types cast
* Supports Java objects get* functions
* Supports multi-source classes with separated configurations
* Has next properties annotations:
  * Specify mapping from property with different name
  * Specify a converter to map data from source unusual way
  * Specify a resolver to map default values into properties
  * Specify null substitute to map nullable properties into not-nullable
* Support extension via plugins

## Supported targets
* JVM
* JavaScript
* Linux
* Windows (mingwX64)
* macOS
* iOS

## Default plugins
* Iterable Plugin:
  * Support collections mapping with different types of elements
* Exposed Plugin:
  * Support mapping from Exposed Table Object (ResultRow)
* Enum Plugin:
  * Support Enum mapping with default value annotation

## Usage
### Add with Gradle

#### JVM Project
```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
}

val kommVersion = "0.25.0"

depensencies {
    implementation("com.ucasoft.komm:komm-annotations:$kommVersion")
    ksp("com.ucasoft.komm:komm-processor:$kommVersion")
}
```
#### Multiplatform Project
```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
}

val kommVersion = "0.25.0"

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

@KOMMMap(from = [SourceObject::class])
data class DestinationObject(
    val id: Int,
    val stringToInt: Int
) {
    var intToString: String = ""
}
```
or
```kotlin
@KOMMMap(to = [DestinationObject::class])
class SourceObject {

    val id = 150

    val intToString = 300

    val stringToInt = "250"
}

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
#### Disable AutoCast
###### Classes declaration
```kotlin
@KOMMMap(
    from = [SourceObject::class],
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
###### Generation result
```console
e: [ksp] com.ucasoft.komm.processor.exceptions.KOMMCastException: AutoCast is turned off! You have to use @MapConvert annotation to cast (stringToInt: Int) from (stringToInt: String)
```
#### Change Convert Function Name
###### Classes declaration
```kotlin
@KOMMMap(
    from = [SourceObject::class],
    config = MapConfiguration(
        convertFunctionName = "convertToDestination"
    )
)
data class DestinationObject(
    val id: Int,
    val stringToInt: Int
) {
    var intToString: String = ""
}
```
###### Generated extension function
```kotlin
fun SourceObject.convertToDestination(): DestinationObject = DestinationObject(
    id = id,
    stringToInt = stringToInt.toInt()
).also { 
    it.intToString = intToString.toString()
}
```

### @MapName annotation
#### Classes declaration
```kotlin
class SourceObject {
    //...
    val userName = "user"
}

@KOMMMap(from = [SourceObject::class])
data class DestinationObject(
    //...
    @MapName("userName")
    val name: String
) {
    var intToString: String = ""
}
```
or
```kotlin
@KOMMMap(to = [DestinationObject::class])
class SourceObject {
    //...
    @MapName("name")
    val userName = "user"
}

data class DestinationObject(
    //...
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
class CostConverter(source: SourceObject) : KOMMConverter<SourceObject, Double, DestinationObject, String>(source) {

    override fun convert(sourceMember: Double) = "$sourceMember ${source.currency}"
}
```
#### Classes declaration
```kotlin
class SourceObject {
    //...
    val cost = 499.99
}

@KOMMMap(from = [SourceObject::class])
data class DestinationObject(
    //...
    @MapConvert<SourceObject, DestinationObject, CostConverter>(CostConverter::class)
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
@KOMMMap(from = [SourceObject::class])
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
    from = [SourceObject::class],
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
#### Classes declaration Map From
```kotlin
@KOMMMap(
    from = [SourceObject::class]
)
data class DestinationObject(
    @NullSubstitute(MapDefault(IntResolver::class))
    val id: Int
) {
    @NullSubstitute(MapDefault(IntResolver::class), "id")
    var otherId: Int = 0
}
```
#### Generated extension function for Map From
```kotlin
fun SourceObject.toDestinationObject(): DestinationObject = DestinationObject(
    id = id ?: IntResolver(null).resolve()
).also {
    it.otherId = id ?: IntResolver(it).resolve()
}
```
#### Classes declaration Map To
```kotlin
@KOMMMap(
    to = [DestinationObject::class]
)
data class SourceObject(
    @NullSubstitute(MapDefault(IntResolver::class))
    val id: Int?
) 
```
#### Generated extension function for Map To
```kotlin
fun SourceObject.toDestinationObject(): DestinationObject = DestinationObject(
    id = id ?: IntResolver(null).resolve()
)
```
### Multi Sources Support
#### Classes declaration
```kotlin
@KOMMMap(
    from = [FirstSourceObject::class, SecondSourceObject::class]
)
data class DestinationObject(
    @NullSubstitute(MapDefault(IntResolver::class), [FirstSourceObject::class])
    @MapName("userId", [SecondSourceObject::class])
    val id: Int
) {
    @NullSubstitute(MapDefault(IntResolver::class), "id", [FirstSourceObject::class])
    var otherId: Int = 0
}

data class FirstSourceObject(
  val id: Int?
)

data class SecondSourceObject(
    val userId: Int
)
```
in case, different sources should be configured different:
```kotlin
@KOMMMap(
    from = [FirstSourceObject::class],
    config = MapConfiguration(
        allowNotNullAssertion = true
    )
)
@KOMMMap(
    from = [SecondSourceObject::class]
)
data class DestinationObject(
  @NullSubstitute(MapDefault(IntResolver::class), [FirstSourceObject::class])
  @MapName("userId", [SecondSourceObject::class])
  val id: Int
) {
  @NullSubstitute(MapDefault(IntResolver::class), "id", [FirstSourceObject::class])
  var otherId: Int = 0
}
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

## Plugins

### Iterable Plugin - Collections Mapping
#### Add with Gradle

###### JVM Project
```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
}

val kommVersion = "0.25.0"

depensencies {
    implementation("com.ucasoft.komm:komm-annotations:$kommVersion")
    ksp("com.ucasoft.komm:komm-processor:$kommVersion")
    ksp("com.ucasoft.komm:komm-plugins-iterable:$kommVersion")
}
```
###### Multiplatform Project
```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
}

val kommVersion = "0.25.0"

//...

dependencies {
    add("kspJvm", "com.ucasoft.komm:komm-plugins-iterable:$kommVersion")
    add("kspJvm", "com.ucasoft.komm:komm-processor:$kommVersion")
    add("kspJs", "com.ucasoft.komm:komm-plugins-iterable:$kommVersion")
    add("kspJs", "com.ucasoft.komm:komm-processor:$kommVersion")
    // Add other platforms like `kspAndroidNativeX64`, `kspLinuxX64`, `kspMingwX64` etc.
}
```
#### Allow NotNullAssertion
###### Classes declaration
```kotlin
class SourceObject {
    val intList: List<Int>? = listOf(1, 2, 3)
}

@KOMMMap(from = [SourceObject::class], config = MapConfiguration(allowNotNullAssertion = true))
data class DestinationObject(
    @MapName("intList")
    val stringList: MutableList<String>
)
```
###### Generated extension function
```kotlin
public fun SourceObject.toDestinationObject(): DestinationObject = DestinationObject(
	stringList = intList!!.map { it.toString() }.toMutableList()
)
```
#### NullSubstitute
###### Classes declaration
```kotlin
class SourceObject {
    val intList: List<Int>? = listOf(1, 2, 3)
}

@KOMMMap(from = [SourceObject::class])
data class DestinationObject(
    @NullSubstitute(MapDefault(StringListResolver::class), "intList")
    val stringList: MutableList<String>
)
```
###### Generated extension function
```kotlin
public fun SourceObject.toDestinationObject(): DestinationObject = DestinationObject(
	stringList = intList?.map { it.toString() }?.toMutableList() ?: StringListResolver(null).resolve()
)
```

### Exposed Plugin - ResultRow Mapping
#### Add with Gradle

###### JVM Project
```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
}

val kommVersion = "0.25.0"

depensencies {
    implementation("com.ucasoft.komm:komm-annotations:$kommVersion")
    ksp("com.ucasoft.komm:komm-processor:$kommVersion")
    ksp("com.ucasoft.komm:komm-plugins-exposed:$kommVersion")
}
```
#### Usage
###### Classes declaration
```kotlin
object SourceObject: Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val age = integer("age")

    override val primaryKey = PrimaryKey(id)
}

@KOMMMap(from = [SourceObject::class])
data class DestinationObject(
    val id: Int,
    val name: String,
    val age: Int
)
```
###### Generated extension function
```kotlin
public fun ResultRow.toDestinationObject(): DestinationObject = DestinationObject(
    id = this[SourceObject.id],
    name = this[SourceObject.name],
    age = this[SourceObject.age]
)
```

### Enum Plugin
#### Add with Gradle

###### JVM Project
```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
}

val kommVersion = "0.25.0"

depensencies {
    implementation("com.ucasoft.komm:komm-annotations:$kommVersion")
    ksp("com.ucasoft.komm:komm-processor:$kommVersion")
    ksp("com.ucasoft.komm:komm-plugins-enum:$kommVersion")
}
```
###### Multiplatform Project
```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
}

val kommVersion = "0.25.0"

//...

dependencies {
    add("kspJvm", "com.ucasoft.komm:komm-plugins-enum:$kommVersion")
    add("kspJvm", "com.ucasoft.komm:komm-processor:$kommVersion")
    add("kspJs", "com.ucasoft.komm:komm-plugins-enum:$kommVersion")
    add("kspJs", "com.ucasoft.komm:komm-processor:$kommVersion")
    // Add other platforms like `kspAndroidNativeX64`, `kspLinuxX64`, `kspMingwX64` etc.
}
```

#### Usage
##### Default
###### Classes declaration
```kotlin
enum class SourceEnum {
    UP, 
    DOWN,
    LEFT,
    RIGHT
}

data class SourceObject(
    val direction: SourceEnum
)

@KOMMMap(from = [SourceObject::class])
data class DestinationObject(
    val direction: DestinationObject.DestinationEnum
) {
    enum class DestinationEnum {
        UP,
        DOWN,
        OTHER
    }
}
```

###### Generated extension function
```kotlin
fun SourceObject.toDestinationObject(): toDestinationObject = toDestinationObject(
	direction = DestinationObject.DestinationEnum.valueOf(direction.name)
)
```

##### NullSubstitute
###### Classes declaration
```kotlin
data class SourceObject(
    val direction: SourceEnum?
)

@KOMMMap(from = [SourceObject::class])
data class DestinationObject(
    @NullSubstitute(MapDefault(DirectionResolver::class))
    val direction: DestinationObject.DestinationEnum
)

class DirectionResolver(destination: DestinationEnum?) : KOMMResolver<DestinationEnum, DestinationObject.DestinationEnum>(destination) {
  override fun resolve() = DestinationObject.DestinationEnum.OTHER
}
```

###### Generated extension function
```kotlin
fun SourceObject.toDestinationObject(): toDestinationObject = toDestinationObject(
	direction = (if (direction != null) DestinationObject.DestinationEnum.valueOf(direction.name) else null)
	  ?: DirectionResolver(null).resolve()
)
```


##### Default Value
###### Classes declaration
```kotlin
data class SourceObject(
    val direction: SourceEnum?
)

@KOMMMap(from = [SourceObject::class])
data class DestinationObject(
    @NullSubstitute(MapDefault(DirectionResolver::class))
    @KOMMEnum("OTHER")
    val direction: DestinationObject.DestinationEnum
)

class DirectionResolver(destination: DestinationEnum?) : KOMMResolver<DestinationEnum, DestinationObject.DestinationEnum>(destination) {
  override fun resolve() = DestinationObject.DestinationEnum.OTHER
}
```

###### Generated extension function
```kotlin
fun SourceObject.toDestinationObject(): toDestinationObject = toDestinationObject(
	direction = (if (direction != null) DestinationObject.DestinationEnum.valueOf(if
    (DestinationObject.DestinationEnum.entries.any { it.name == direction.name }) direction.name else "OTHER")
    else null) ?: DirectionResolver(null).resolve()
)
```
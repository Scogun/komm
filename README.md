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
  * [Context](#use-context)
  * [Configuration](#mapping-configuration)
    * [Disable AutoCast](#disable-autocast)
    * [Change Convert Function Name](#change-convert-function-name)
    * [Nullable Context](#nullable-context)
  * [@MapFunction](#mapfunction-annotation)
  * [@MapName](#mapname-annotation)
  * [@MapEmbedded](#mapembedded-annotation)
  * [@MapConvert](#use-converter)
  * [@MapDefault](#use-resolver)
  * [@MapTargetDefault](#class-level-target-default)
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
  * Specify a top-level extension function for property casting
  * Specify a resolver to map default values into properties
  * Specify target property default values from class-level annotations
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
Add KOMM annotations and the KSP processor to the project where mapper functions should be generated.

#### JVM Project
Use this setup for a single-target JVM project that runs KOMM through KSP.

```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.3.9"
}

val kommVersion = "0.80.3"

dependencies {
    implementation("com.ucasoft.komm:komm-annotations:$kommVersion")
    ksp("com.ucasoft.komm:komm-processor:$kommVersion")
}
```
#### Multiplatform Project
Use this setup for Kotlin Multiplatform projects, adding the KOMM processor to every KSP target you generate for.

```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.3.9"
}

val kommVersion = "0.80.3"

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
Use simple mapping when source and destination properties can be matched by name and converted automatically.

#### Classes declaration
Declare the source and destination models, then annotate either side to request a mapper between them.

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
KOMM generates an extension function that copies matching properties and casts compatible values.

```kotlin
fun SourceObject.toDestinationObject(): DestinationObject = DestinationObject(
    id = id,
    stringToInt = stringToInt.toInt()
).also { 
    it.intToString = intToString.toString()
}
```

### Use Context
Use mapping context when destination members depend on data that is not part of the source object, such as lookup tables produced by other flows.

#### Context declaration
Define a context object to carry extra data that is needed while building the destination model.

```kotlin
data class TransactionMapContext(
    val accounts: Map<Long, Account>,
    val accountCurrencies: Map<Long, AccountCurrency>,
    val categories: Map<Long, Category>
)
```
#### Classes declaration
Attach the context type to the mapping so the generated function requires that context argument.

```kotlin
@KOMMMap(from = [DbTransaction::class], context = TransactionMapContext::class)
data class Transaction(
    //...
)
```
#### Generated extension function
The generated mapper receives the context as a `kommContext` parameter.

```kotlin
fun DbTransaction.toTransaction(kommContext: TransactionMapContext): Transaction = Transaction(
    //...
)
```
The context is a snapshot. Combine reactive inputs before mapping, then build a fresh context whenever any dependency emits.
```kotlin
combine(transactions, accountCurrencies, categories, accounts) { items, currencies, cats, accs ->
    val context = TransactionMapContext(accs, currencies, cats)
    items.map { it.toTransaction(context) }
}
```

### Mapping Configuration
Use mapping configuration to tune generated function names, automatic casts, and context handling.

#### Disable AutoCast
Set `tryAutoCast = false` when incompatible property types should fail generation unless a converter is provided.

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
Set `convertFunctionName` when the generated extension should use a custom function name.

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
#### Nullable Context
Set `nullableContext = true` when a mapping context should be optional at the mapping function boundary.
KOMM generates a nullable context parameter with a default `null` value.
If a context-aware converter or resolver is used, the generated mapper checks that the context was provided before calling it.

###### Classes declaration
```kotlin
@KOMMMap(
    from = [SourceObject::class],
    context = SourceMapContext::class,
    config = MapConfiguration(
        nullableContext = true
    )
)
data class DestinationObject(
    val id: Int
)
```
###### Generated extension function
```kotlin
fun SourceObject.toDestinationObject(kommContext: SourceMapContext? = null): DestinationObject = DestinationObject(
    id = id
)
```

### @MapFunction annotation
Use `@MapFunction` when the automatic `toType()` cast should call a top-level extension function from another package.
KOMM imports the function and keeps extension-call syntax in generated code.

#### Function declaration
Declare the extension function that KOMM should call for the custom property conversion.

```kotlin
fun ByteArray.toImageBitmap(): ImageBitmap = //...
```
#### Classes declaration
Annotate the target property with the package and optional name of the conversion function.

```kotlin
data class SourceObject(
    val logo: ByteArray?
)

@KOMMMap(from = [SourceObject::class])
data class DestinationObject(
    @MapFunction(packageName = "com.test.converters")
    val logo: ImageBitmap?
)
```
or specify the function name explicitly:
```kotlin
@MapFunction(
    packageName = "com.test.converters",
    name = "toImageBitmap"
)
```
#### Generated extension function
The generated mapper imports and calls the configured extension function.

```kotlin
import com.test.converters.toImageBitmap

fun SourceObject.toDestinationObject(): DestinationObject = DestinationObject(
    logo = logo?.toImageBitmap()
)
```

### @MapName annotation
Use `@MapName` to connect properties whose names differ between the source and destination models.

#### Classes declaration
Use `@MapName` when a source and destination property represent the same value with different names.

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
The generated mapper reads from the configured source property name.

```kotlin
fun SourceObject.toDestinationObject(): DestinationObject = DestinationObject(
    //...
    name = userName
).also { 
    it.intToString = intToString.toString()
}
```

### @MapEmbedded annotation
Use `@MapEmbedded` when several destination properties should be mapped from the same nested source property.
KOMM checks only the first nested level. Direct source properties have priority over embedded properties.
If two embedded properties can provide the same destination property, generation fails and the mapping should be made explicit.

#### Classes declaration
Mark the nested source property that should contribute values to the destination model.

```kotlin
data class Account(
    val id: Long,
    val name: String
)

data class AccountWithCurrencies(
    val account: Account,
    val currencies: List<AccountCurrency>
)

@KOMMMap(from = [AccountWithCurrencies::class])
@MapEmbedded("account")
data class AccountDto(
    val name: String,
    val currencies: List<AccountCurrencyDto>
) {
    var id: Long = 0L
}
```
#### Generated extension function
The generated mapper reads destination values from both direct and embedded source properties.

```kotlin
fun AccountWithCurrencies.toAccountDto(): AccountDto = AccountDto(
    name = account.name,
    currencies = currencies.map { it.toAccountCurrencyDto() }
).also {
    it.id = account.id
}
```
#### Nullable embedded source
When the embedded source is nullable, use defaults or substitutes for destination properties that cannot be null.

```kotlin
data class AccountWithCurrencies(
    val account: Account?,
    val currencies: List<AccountCurrency>
)

@KOMMMap(from = [AccountWithCurrencies::class])
@MapEmbedded("account")
data class AccountDto(
    @NullSubstitute(MapDefault(StringResolver::class))
    val name: String,
    val currencies: List<AccountCurrencyDto>
)
```
#### Generated extension function
The generated mapper safely accesses the nullable embedded property and falls back to the configured default.

```kotlin
fun AccountWithCurrencies.toAccountDto(): AccountDto = AccountDto(
    name = account?.name ?: StringResolver(null).resolve(),
    currencies = currencies.map { it.toAccountCurrencyDto() }
)
```

### Use Converter
Use converters for property mapping that needs custom transformation logic.

#### Converter declaration
Create a converter when a property needs custom logic that depends on the source object.

```kotlin
class CostConverter(source: SourceObject) : KOMMConverter<SourceObject, Double, DestinationObject, String>(source) {

    override fun convert(sourceMember: Double) = "$sourceMember ${source.currency}"
}
```
#### Classes declaration
Apply `@MapConvert` to constructor or mutable properties that should use the converter.

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
    @MapConvert<SourceObject, DestinationObject, CostConverter>(CostConverter::class, "cost")
    var otherCost: String = ""
}
```
#### Generated extension function
The generated mapper instantiates the converter and calls it for each annotated property.

```kotlin
fun SourceObject.toDestinationObject(): DestinationObject = DestinationObject(
    //...
    cost = CostConverter(this).convert(cost)
).also { 
    //...
    it.otherCost = CostConverter(this).convert(cost)
}
```
`@MapConvert` can also use a context-aware converter when the mapping has `KOMMMap.context`.

```kotlin
data class AccountMapContext(
    val banks: Map<Long, Bank>
)

class BankConverter(
    source: FullAccount,
    context: AccountMapContext
) : KOMMContextConverter<FullAccount, Long?, AccountMapContext, Account, Bank?>(source, context) {

    override fun convert(sourceMember: Long?): Bank? =
        sourceMember?.let(context.banks::get)
}
```
#### Classes declaration
Use a context-aware converter when the conversion also needs values from `KOMMMap.context`.

```kotlin
@KOMMMap(from = [FullAccount::class], context = AccountMapContext::class)
data class Account(
    //...
    @MapConvert<FullAccount, Account, BankConverter>(BankConverter::class, "bankId")
    val bank: Bank?
)
```
#### Generated extension function
The generated mapper passes both the source object and mapping context to the converter.

```kotlin
fun FullAccount.toAccount(kommContext: AccountMapContext): Account = Account(
    //...
    bank = BankConverter(this, kommContext).convert(bankId)
)
```

### Use Resolver
Use resolvers to provide destination values that cannot be read directly from the source object.

#### Resolver declaration
Create a resolver when a destination value should be supplied instead of read from the source.

```kotlin
class DateResolver(destination: DestinationObject?) : KOMMResolver<DestinationObject, Date>(destination) {
    
    override fun resolve(): Date = Date.from(Instant.now())
}
```
#### Classes declaration
Apply `@MapDefault` to properties that should be resolved during mapper generation.

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
The generated mapper calls the resolver while constructing or updating the destination.

```kotlin
fun SourceObject.toDestinationObject(): DestinationObject = DestinationObject(
    //...
    activeDate = DateResolver(null).resolve()
).also { 
    //...
    it.otherDate = DateResolver(it).resolve()
}
```
`@MapDefault` can also use a context-aware resolver when the mapping has `KOMMMap.context`.
```kotlin
data class TransactionMapContext(
  val accounts: Map<Long, Account>,
  val accountCurrencies: Map<Long, AccountCurrency>,
  val categories: Map<Long, Category>
)

class FallbackAccountResolver(
    destination: Transaction?,
    context: TransactionMapContext
) : KOMMContextResolver<TransactionMapContext, Transaction, Account?>(destination, context) {

    override fun resolve(): Account? {
        return context.accounts.values.firstOrNull()
    }
}
```
#### Classes declaration
Use a context-aware resolver when the default value depends on `KOMMMap.context`.

```kotlin
@KOMMMap(from = [DbTransaction::class], context = TransactionMapContext::class)
data class Transaction(
    //...
    @MapDefault<FallbackAccountResolver>(FallbackAccountResolver::class)
    val expenseAccount: Account?
)
```
#### Generated extension function
The generated mapper passes the mapping context into the resolver.

```kotlin
fun DbTransaction.toTransaction(kommContext: TransactionMapContext): Transaction = Transaction(
    //... 
    expenseAccount = FallbackAccountResolver(null, kommContext).resolve()
)
```

### Class-level target default
Use `@MapTargetDefault` when a target property needs `@MapDefault`, but the target class cannot be annotated.
This is useful for `to` mappings into external models.

#### Classes declaration
Declare the source mapping and class-level default metadata for the external target property.

```kotlin
data class AccountCardMapContext(val accountId: Long)

class AccountIdResolver(
    destination: DbAccountCard?,
    context: AccountCardMapContext
) : KOMMContextResolver<AccountCardMapContext, DbAccountCard, Long>(destination, context) {

    override fun resolve(): Long = context.accountId
}

@KOMMMap(to = [DbAccountCard::class], context = AccountCardMapContext::class)
@MapTargetDefault(
    name = "accountId",
    default = MapDefault(AccountIdResolver::class),
    `for` = [DbAccountCard::class]
)
data class AccountCard(
    val id: Long,
    val type: String,
    val number: String
)
```
#### Generated extension function
The generated mapper resolves the target default while creating the external model.

```kotlin
fun AccountCard.toDbAccountCard(kommContext: AccountCardMapContext): DbAccountCard = DbAccountCard(
    id = id,
    accountId = AccountIdResolver(null, kommContext).resolve(),
    type = type,
    number = number
)
```
### Use NullSubstitute
Use null substitutes to map nullable source values into non-null destination properties.

#### Mapping configuration
Enable not-null assertions only when nullable source values are expected to be present at runtime.

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
Define the fallback value provider used when the nullable source property is `null`.

```kotlin
class IntResolver(destination: DestinationObject?): KOMMResolver<DestinationObject, Int>(destination) {

    override fun resolve() = 1
}
```
#### Classes declaration Map From
Place `@NullSubstitute` on destination properties for mappings declared with `from`.

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
The generated mapper uses the source value when present and falls back to the resolver when it is `null`.

```kotlin
fun SourceObject.toDestinationObject(): DestinationObject = DestinationObject(
    id = id ?: IntResolver(null).resolve()
).also {
    it.otherId = id ?: IntResolver(it).resolve()
}
```
#### Classes declaration Map To
Place `@NullSubstitute` on source properties for mappings declared with `to`.

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
The generated mapper applies the substitute while creating the configured target type.

```kotlin
fun SourceObject.toDestinationObject(): DestinationObject = DestinationObject(
    id = id ?: IntResolver(null).resolve()
)
```
### Multi Sources Support
Use multi-source mappings when the same destination type should be generated from more than one source type.

#### Classes declaration
Configure each source type separately when one destination can be mapped from multiple models.

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
KOMM generates one extension function per configured source type.

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
Use the iterable plugin to map collection properties while converting their element types.

#### Add with Gradle
Add the iterable plugin alongside the KOMM processor for every target that needs collection mapping.

###### JVM Project
```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.3.9"
}

val kommVersion = "0.80.3"

dependencies {
    implementation("com.ucasoft.komm:komm-annotations:$kommVersion")
    ksp("com.ucasoft.komm:komm-processor:$kommVersion")
    ksp("com.ucasoft.komm:komm-plugins-iterable:$kommVersion")
}
```
###### Multiplatform Project
```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.3.9"
}

val kommVersion = "0.80.3"

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
Allow not-null assertions when a nullable collection should be treated as present before element mapping.

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
Use `@NullSubstitute` when a nullable collection should fall back to a resolver.

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
Use the Exposed plugin to generate mappers from database `ResultRow` values.

#### Add with Gradle
Add the Exposed plugin to JVM projects that map `ResultRow` values into application models.

###### JVM Project
```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.3.9"
}

val kommVersion = "0.80.3"

dependencies {
    implementation("com.ucasoft.komm:komm-annotations:$kommVersion")
    ksp("com.ucasoft.komm:komm-processor:$kommVersion")
    ksp("com.ucasoft.komm:komm-plugins-exposed:$kommVersion")
}
```
#### Usage
Annotate a destination model with an Exposed table source to generate a `ResultRow` mapper.

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
Use the enum plugin to map enum properties by constant names and configured fallbacks.

#### Add with Gradle
Add the enum plugin alongside the KOMM processor for targets that map enum properties.

###### JVM Project
```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.3.9"
}

val kommVersion = "0.80.3"

dependencies {
    implementation("com.ucasoft.komm:komm-annotations:$kommVersion")
    ksp("com.ucasoft.komm:komm-processor:$kommVersion")
    ksp("com.ucasoft.komm:komm-plugins-enum:$kommVersion")
}
```
###### Multiplatform Project
```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.3.9"
}

val kommVersion = "0.80.3"

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
Use the enum plugin to map enum constants by name and optionally provide fallback behavior.

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

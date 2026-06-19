package com.ucasoft.komm.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.STRING
import com.tschuchort.compiletesting.KotlinCompilation
import com.ucasoft.komm.annotations.KOMMMap
import com.ucasoft.komm.annotations.MapDefault
import com.ucasoft.komm.annotations.MapEmbedded
import com.ucasoft.komm.annotations.NullSubstitute
import com.ucasoft.komm.processor.exceptions.KOMMException
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.reflection.shouldHaveMemberProperty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlin.test.Test

internal class MapEmbeddedTests: SatelliteTests() {

    @Test
    fun mapEmbeddedConstructorAndMutableProperties() {
        val accountSpec = buildFileSpec(
            "Account",
            mapOf(
                "id" to PropertySpecInit(INT),
                "name" to PropertySpecInit(STRING)
            )
        )
        val accountClassName = accountSpec.typeSpecs.first().name!!
        val sourceSpec = buildFileSpec(
            "SourceObject",
            mapOf("account" to PropertySpecInit(ClassName(packageName, accountClassName)))
        )
        val sourceClassName = sourceSpec.typeSpecs.first().name!!
        val generated = generate(
            accountSpec,
            sourceSpec,
            buildFileSpec(
                "DestinationObject",
                mapOf("name" to PropertySpecInit(STRING)),
                listOf(
                    KOMMMap::class to mapOf("from = %L" to listOf("[$sourceClassName::class]")),
                    MapEmbedded::class to mapOf("name = %S" to listOf("account"))
                ),
                mapOf("id" to PropertySpecInit(INT, "%L", 0))
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)

        val accountClass = generated.classLoader.loadClass("$packageName.$accountClassName")
        val sourceClass = generated.classLoader.loadClass("$packageName.$sourceClassName")
        val mappingClass = generated.classLoader.loadClass("$packageName.MappingExtensionsKt")
        val account = accountClass.constructors.first().newInstance(10, "Main")
        val source = sourceClass.constructors.first().newInstance(account)
        val destination = mappingClass.declaredMethods.first().invoke(null, source)

        destination.shouldNotBeNull()
        destination::class.shouldHaveMemberProperty("name") {
            it.getter.call(destination).shouldBe("Main")
        }
        destination::class.shouldHaveMemberProperty("id") {
            it.getter.call(destination).shouldBe(10)
        }
    }

    @Test
    fun directPropertyWinsOverEmbeddedProperty() {
        val accountSpec = buildFileSpec("Account", mapOf("name" to PropertySpecInit(STRING)))
        val accountClassName = accountSpec.typeSpecs.first().name!!
        val sourceSpec = buildFileSpec(
            "SourceObject",
            mapOf(
                "name" to PropertySpecInit(STRING),
                "account" to PropertySpecInit(ClassName(packageName, accountClassName))
            )
        )
        val sourceClassName = sourceSpec.typeSpecs.first().name!!
        val generated = generate(
            accountSpec,
            sourceSpec,
            buildFileSpec(
                "DestinationObject",
                mapOf("name" to PropertySpecInit(STRING)),
                listOf(
                    KOMMMap::class to mapOf("from = %L" to listOf("[$sourceClassName::class]")),
                    MapEmbedded::class to mapOf("name = %S" to listOf("account"))
                )
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)

        val accountClass = generated.classLoader.loadClass("$packageName.$accountClassName")
        val sourceClass = generated.classLoader.loadClass("$packageName.$sourceClassName")
        val mappingClass = generated.classLoader.loadClass("$packageName.MappingExtensionsKt")
        val account = accountClass.constructors.first().newInstance("Embedded")
        val source = sourceClass.constructors.first().newInstance("Direct", account)
        val destination = mappingClass.declaredMethods.first().invoke(null, source)

        destination.shouldNotBeNull()
        destination::class.shouldHaveMemberProperty("name") {
            it.getter.call(destination).shouldBe("Direct")
        }
    }

    @Test
    fun embeddedPropertyAmbiguityFails() {
        val accountSpec = buildFileSpec("Account", mapOf("name" to PropertySpecInit(STRING)))
        val accountClassName = accountSpec.typeSpecs.first().name!!
        val profileSpec = buildFileSpec("Profile", mapOf("name" to PropertySpecInit(STRING)))
        val profileClassName = profileSpec.typeSpecs.first().name!!
        val sourceSpec = buildFileSpec(
            "SourceObject",
            mapOf(
                "account" to PropertySpecInit(ClassName(packageName, accountClassName)),
                "profile" to PropertySpecInit(ClassName(packageName, profileClassName))
            )
        )
        val sourceClassName = sourceSpec.typeSpecs.first().name!!
        val generated = generate(
            accountSpec,
            profileSpec,
            sourceSpec,
            buildFileSpec(
                "DestinationObject",
                mapOf("name" to PropertySpecInit(STRING)),
                listOf(
                    KOMMMap::class to mapOf("from = %L" to listOf("[$sourceClassName::class]")),
                    MapEmbedded::class to mapOf("name = %S" to listOf("account")),
                    MapEmbedded::class to mapOf("name = %S" to listOf("profile"))
                )
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.INTERNAL_ERROR)
        generated.messages.shouldContain("${KOMMException::class.simpleName}: There are more than one embedded property with the same name name: account.name, profile.name.")
    }

    @Test
    fun mapEmbeddedBadSourceNameFails() {
        val accountSpec = buildFileSpec("Account", mapOf("name" to PropertySpecInit(STRING)))
        val accountClassName = accountSpec.typeSpecs.first().name!!
        val sourceSpec = buildFileSpec(
            "SourceObject",
            mapOf("account" to PropertySpecInit(ClassName(packageName, accountClassName)))
        )
        val sourceClassName = sourceSpec.typeSpecs.first().name!!
        val generated = generate(
            accountSpec,
            sourceSpec,
            buildFileSpec(
                "DestinationObject",
                mapOf("name" to PropertySpecInit(STRING)),
                listOf(
                    KOMMMap::class to mapOf("from = %L" to listOf("[$sourceClassName::class]")),
                    MapEmbedded::class to mapOf("name = %S" to listOf("missing"))
                )
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.INTERNAL_ERROR)
        generated.messages.shouldContain("${KOMMException::class.simpleName}: There is no embedded mapping source missing property from source $sourceClassName.")
    }

    @Test
    fun mapEmbeddedNullableParentWithNullSubstitute() {
        val accountSpec = buildFileSpec("Account", mapOf("id" to PropertySpecInit(INT)))
        val accountClassName = accountSpec.typeSpecs.first().name!!
        val sourceSpec = buildFileSpec(
            "SourceObject",
            mapOf("account" to PropertySpecInit(ClassName(packageName, accountClassName), isNullable = true))
        )
        val sourceClassName = sourceSpec.typeSpecs.first().name!!
        val resolver = buildResolver(ClassName(packageName, "DestinationObject"), INT, "return 25")
        val resolverClassName = resolver.typeSpecs.first().name!!
        val generated = generate(
            accountSpec,
            sourceSpec,
            resolver,
            buildFileSpec(
                "DestinationObject",
                mapOf(
                    "id" to PropertySpecInit(
                        INT,
                        annotations = listOf(
                            NullSubstitute::class to mapOf(
                                "default = %L" to listOf("${MapDefault::class.simpleName}($resolverClassName::class)")
                            )
                        )
                    )
                ),
                listOf(
                    KOMMMap::class to mapOf("from = %L" to listOf("[$sourceClassName::class]")),
                    MapEmbedded::class to mapOf("name = %S" to listOf("account"))
                )
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)

        val accountClass = generated.classLoader.loadClass("$packageName.$accountClassName")
        val sourceClass = generated.classLoader.loadClass("$packageName.$sourceClassName")
        val mappingClass = generated.classLoader.loadClass("$packageName.MappingExtensionsKt")
        var source = sourceClass.constructors.first().newInstance(null)
        var destination = mappingClass.declaredMethods.first().invoke(null, source)

        destination.shouldNotBeNull()
        destination::class.shouldHaveMemberProperty("id") {
            it.getter.call(destination).shouldBe(25)
        }

        val account = accountClass.constructors.first().newInstance(10)
        source = sourceClass.constructors.first().newInstance(account)
        destination = mappingClass.declaredMethods.first().invoke(null, source)

        destination::class.shouldHaveMemberProperty("id") {
            it.getter.call(destination).shouldBe(10)
        }
    }

    @Test
    fun mapEmbeddedUsesForWithMultipleSources() {
        val accountSpec = buildFileSpec("Account", mapOf("name" to PropertySpecInit(STRING)))
        val accountClassName = accountSpec.typeSpecs.first().name!!
        val profileSpec = buildFileSpec("Profile", mapOf("name" to PropertySpecInit(STRING)))
        val profileClassName = profileSpec.typeSpecs.first().name!!
        val firstSourceSpec = buildFileSpec(
            "FirstSourceObject",
            mapOf("account" to PropertySpecInit(ClassName(packageName, accountClassName)))
        )
        val firstSourceClassName = firstSourceSpec.typeSpecs.first().name!!
        val secondSourceSpec = buildFileSpec(
            "SecondSourceObject",
            mapOf("profile" to PropertySpecInit(ClassName(packageName, profileClassName)))
        )
        val secondSourceClassName = secondSourceSpec.typeSpecs.first().name!!
        val generated = generate(
            accountSpec,
            profileSpec,
            firstSourceSpec,
            secondSourceSpec,
            buildFileSpec(
                "DestinationObject",
                mapOf("name" to PropertySpecInit(STRING)),
                listOf(
                    KOMMMap::class to mapOf("from = %L" to listOf("[$firstSourceClassName::class, $secondSourceClassName::class]")),
                    MapEmbedded::class to mapOf(
                        "name = %S" to listOf("account"),
                        "`for` = %L" to listOf("[$firstSourceClassName::class]")
                    ),
                    MapEmbedded::class to mapOf(
                        "name = %S" to listOf("profile"),
                        "`for` = %L" to listOf("[$secondSourceClassName::class]")
                    )
                )
            )
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)

        val accountClass = generated.classLoader.loadClass("$packageName.$accountClassName")
        val profileClass = generated.classLoader.loadClass("$packageName.$profileClassName")
        val firstSourceClass = generated.classLoader.loadClass("$packageName.$firstSourceClassName")
        val secondSourceClass = generated.classLoader.loadClass("$packageName.$secondSourceClassName")
        val mappingClass = generated.classLoader.loadClass("$packageName.MappingExtensionsKt")
        var mappingMethod = mappingClass.declaredMethods.first { it.toString().contains(firstSourceClassName) }
        val firstSource = firstSourceClass.constructors.first().newInstance(
            accountClass.constructors.first().newInstance("Account")
        )
        var destination = mappingMethod.invoke(null, firstSource)

        destination::class.shouldHaveMemberProperty("name") {
            it.getter.call(destination).shouldBe("Account")
        }

        mappingMethod = mappingClass.declaredMethods.first { it.toString().contains(secondSourceClassName) }
        val secondSource = secondSourceClass.constructors.first().newInstance(
            profileClass.constructors.first().newInstance("Profile")
        )
        destination = mappingMethod.invoke(null, secondSource)

        destination::class.shouldHaveMemberProperty("name") {
            it.getter.call(destination).shouldBe("Profile")
        }
    }

    @Test
    fun mapEmbeddedWithMapTo() {
        val accountSpec = buildFileSpec("Account", mapOf("name" to PropertySpecInit(STRING)))
        val accountClassName = accountSpec.typeSpecs.first().name!!
        val destinationSpec = buildFileSpec("DestinationObject", mapOf("name" to PropertySpecInit(STRING)))
        val destinationClassName = destinationSpec.typeSpecs.first().name!!
        val sourceSpec = buildFileSpec(
            "SourceObject",
            mapOf("account" to PropertySpecInit(ClassName(packageName, accountClassName))),
            listOf(
                KOMMMap::class to mapOf("to = %L" to listOf("[$destinationClassName::class]")),
                MapEmbedded::class to mapOf("name = %S" to listOf("account"))
            )
        )
        val sourceClassName = sourceSpec.typeSpecs.first().name!!
        val generated = generate(
            accountSpec,
            destinationSpec,
            sourceSpec
        )

        generated.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)

        val accountClass = generated.classLoader.loadClass("$packageName.$accountClassName")
        val sourceClass = generated.classLoader.loadClass("$packageName.$sourceClassName")
        val mappingClass = generated.classLoader.loadClass("$packageName.MappingExtensionsKt")
        val source = sourceClass.constructors.first().newInstance(
            accountClass.constructors.first().newInstance("Account")
        )
        val destination = mappingClass.declaredMethods.first().invoke(null, source)

        destination::class.shouldHaveMemberProperty("name") {
            it.getter.call(destination).shouldBe("Account")
        }
    }
}

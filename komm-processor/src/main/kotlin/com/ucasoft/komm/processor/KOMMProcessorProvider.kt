import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.ucasoft.komm.processor.KOMMSymbolProcessor

class KOMMProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) = KOMMSymbolProcessor(
        environment.codeGenerator,
        environment.logger,
        environment.options
    )
}
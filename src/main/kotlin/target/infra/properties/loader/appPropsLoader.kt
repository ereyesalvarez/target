package target.infra.properties.loader
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ExperimentalHoplite
import com.sksamuel.hoplite.addEnvironmentSource
import com.sksamuel.hoplite.addResourceSource
import target.infra.properties.definition.AppProps

@OptIn(ExperimentalHoplite::class)
val appProps: AppProps = ConfigLoaderBuilder.default()
  .addEnvironmentSource(useUnderscoresAsSeparator = true, allowUppercaseNames = true)
  .addResourceSource("/application-test.properties", optional = true)
  .addResourceSource("/application-local.properties", optional = true)
  .addResourceSource("/application.properties", optional = true)
  .withExplicitSealedTypes()
  .build()
  .loadConfigOrThrow<AppProps>()


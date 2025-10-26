package target.infra.metric

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.jvm.*
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry


private fun bindJVMMetrics(registry: MeterRegistry){
  ClassLoaderMetrics().bindTo(registry)
  JvmMemoryMetrics().bindTo(registry)
  JvmGcMetrics().bindTo(registry)
  ProcessorMetrics().bindTo(registry)
  JvmThreadMetrics().bindTo(registry)
  JvmThreadDeadlockMetrics().bindTo(registry)
}

fun createMeterRegistry(): PrometheusMeterRegistry{
  val meterRegistry =  PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
  bindJVMMetrics(meterRegistry)
  return meterRegistry
}

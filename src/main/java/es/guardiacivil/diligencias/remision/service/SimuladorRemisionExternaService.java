package es.guardiacivil.diligencias.remision.service;

import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Adaptador simulado de remisión a un sistema externo policial.
 *
 * <p>En un entorno real, esta clase sería reemplazada por un cliente HTTP
 * que se conectaría al aplicativo policial correspondiente.</p>
 *
 * <p>Las referencias generadas tienen prefijo {@code SIM-} para distinguirlas
 * expresamente de las referencias reales ({@code REG-}).</p>
 */
@Service
public class SimuladorRemisionExternaService {

    private final AtomicLong secuencia = new AtomicLong(1);

    /**
     * Genera una referencia externa simulada con prefijo SIM-.
     * Formato: SIM-{año}-{secuencia 6 dígitos}
     */
    public String generarReferenciaSimulada() {
        long seq = secuencia.getAndIncrement();
        return "SIM-" + Year.now().getValue() + "-" + String.format("%06d", seq);
    }

    /**
     * Simula el envío de un documento a un sistema externo.
     * En un entorno real, aquí se realizaría la llamada HTTP/SOAP al sistema destino.
     *
     * @return referencia externa simulada generada
     */
    public String enviarDocumentoSimulado() {
        // Simular latencia de red (omitido en tests)
        return generarReferenciaSimulada();
    }
}

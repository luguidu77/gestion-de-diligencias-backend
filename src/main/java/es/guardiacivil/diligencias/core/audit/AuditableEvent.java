package es.guardiacivil.diligencias.core.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditableEvent {
    
    /**
     * Nombre de la acción realizada (ej: CREAR_EXPEDIENTE, MODIFICAR_TAREA, DESCARGAR_OFICIO).
     */
    String action();

    /**
     * Nombre del recurso o entidad afectada (ej: expedientes, gestiones, tareas).
     */
    String entity() default "";
}

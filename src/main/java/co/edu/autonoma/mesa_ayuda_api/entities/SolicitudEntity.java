package co.edu.autonoma.mesa_ayuda_api.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.AccessLevel;
import co.edu.autonoma.mesa_ayuda_api.domain.EstadoSolicitud;
import co.edu.autonoma.mesa_ayuda_api.domain.Impacto;
import co.edu.autonoma.mesa_ayuda_api.domain.Prioridad;
import co.edu.autonoma.mesa_ayuda_api.exception.ReglaNegocioException;

@Entity
@Table(name = "solicitud")
@Getter
@Setter
public class SolicitudEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 140)
    private String titulo;

    @Column(nullable = false, columnDefinition = "text")
    private String descripcion;

    @Column(nullable = false, length = 120)
    private String ubicacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaEntity categoria;

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private Impacto impacto;

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private Prioridad prioridad;

    @Column(nullable = false, length = 12)
    @Enumerated(EnumType.STRING)
    @Setter(AccessLevel.NONE)
    private EstadoSolicitud estado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private UsuarioEntity solicitante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tecnico_id")
    private UsuarioEntity tecnico;

    @Column(name = "creada_en", nullable = false)
    private LocalDateTime creadaEn;

    @Column(name = "cerrada_en")
    private LocalDateTime cerradaEn;

    public void iniciar() {
        if (id != null || estado != null) {
            throw new ReglaNegocioException("La solicitud ya tiene un estado inicial.");
        }
        estado = EstadoSolicitud.ABIERTA;
    }

    public EstadoSolicitud transicionarA(EstadoSolicitud nuevo) {
        if (estado == null || !estado.puedePasarA(nuevo)) {
            String destinos = estado == null
                    ? EstadoSolicitud.ABIERTA.name()
                    : estado.destinosPermitidosComoTexto();
            throw new ReglaNegocioException(
                    "No se puede cambiar de " + (estado == null ? "SIN ESTADO" : estado.name())
                            + " a " + (nuevo == null ? "SIN ESTADO" : nuevo.name())
                            + ". Destinos permitidos: " + destinos + ".");
        }
        EstadoSolicitud anterior = estado;
        estado = nuevo;
        return anterior;
    }

}

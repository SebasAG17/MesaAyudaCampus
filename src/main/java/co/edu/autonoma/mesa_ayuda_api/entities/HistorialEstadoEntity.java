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
import co.edu.autonoma.mesa_ayuda_api.domain.EstadoSolicitud;

@Entity
@Table(name = "historial_estado")
@Getter
@Setter
public class HistorialEstadoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private SolicitudEntity solicitud;

    @Column(name = "estado_anterior", length = 12)
    @Enumerated(EnumType.STRING)
    private EstadoSolicitud estadoAnterior;

    @Column(name = "estado_nuevo", nullable = false, length = 12)
    @Enumerated(EnumType.STRING)
    private EstadoSolicitud estadoNuevo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comentario_id")
    private ComentarioEntity comentario;

    @Column(name = "ocurrido_en", nullable = false)
    private LocalDateTime ocurridoEn;

}

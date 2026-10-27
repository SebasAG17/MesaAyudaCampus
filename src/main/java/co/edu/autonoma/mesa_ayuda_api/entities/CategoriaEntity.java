package co.edu.autonoma.mesa_ayuda_api.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "categoria")
public class CategoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 80, unique = true)
    private String nombre;

    @Column(name = "prioridad_base", nullable = false, length = 10)
    private String prioridadBase;

    @Column(nullable = false)
    private boolean activa;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPrioridadBase() {
        return prioridadBase;
    }

    public void setPrioridadBase(String prioridadBase) {
        this.prioridadBase = prioridadBase;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

}

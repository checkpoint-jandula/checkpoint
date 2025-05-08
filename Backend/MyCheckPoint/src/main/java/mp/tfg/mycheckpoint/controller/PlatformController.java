package mp.tfg.mycheckpoint.controller;

import mp.tfg.mycheckpoint.dto.platform.PlatformDTO;
import mp.tfg.mycheckpoint.exception.ResourceNotFoundException; // Importar
import mp.tfg.mycheckpoint.service.PlatformService; // Usar servicio
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable; // Importar
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/plataformas")
public class PlatformController {

    private final PlatformService platformService; // Inyectar Servicio

    @Autowired
    public PlatformController(PlatformService platformService) {
        this.platformService = platformService;
    }

    @GetMapping
    public ResponseEntity<List<PlatformDTO>> getPlataformas() {
        return ResponseEntity.ok(platformService.getAllPlatforms());
    }

    // Endpoint GET por ID que estaba en tu OpenAPI
    @GetMapping("/{id}")
    public ResponseEntity<PlatformDTO> getPlataformaById(@PathVariable Long id) {
        return platformService.getPlatformById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Plataforma no encontrada con ID: " + id));
    }
}
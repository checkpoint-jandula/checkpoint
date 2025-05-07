package mp.tfg.mycheckpoint.controller;

import jakarta.validation.Valid;
import mp.tfg.mycheckpoint.dto.user.UserCreateDTO;
import mp.tfg.mycheckpoint.dto.user.UserDTO;
import mp.tfg.mycheckpoint.exception.ResourceNotFoundException;
import mp.tfg.mycheckpoint.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/usuarios") // Base path de tu OpenAPI
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // POST /usuarios - Registrar un nuevo usuario
    // operationId: registrarUsuario
    @PostMapping
    public ResponseEntity<UserDTO> registrarUsuario(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        UserDTO createdUser = userService.createUser(userCreateDTO);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    // GET /usuarios/{id} - Obtener usuario por ID (interno)
    // operationId: getUsuarioById
    // Nota: El OpenAPI dice 'id' es Long (interno). Si quieres exponer esto, asegúrate de su propósito.
    // Por ahora, lo implementaremos como un endpoint interno.
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUsuarioById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID interno: " + id));
    }

    // GET /usuarios/public/{publicId} - Obtener perfil público por UUID
    // operationId: getUsuarioByPublicId
    @GetMapping("/public/{publicId}")
    public ResponseEntity<UserDTO> getUsuarioByPublicId(@PathVariable UUID publicId) {
        return userService.getUserByPublicId(publicId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con Public ID: " + publicId));
    }
}
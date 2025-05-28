package mp.tfg.mycheckpoint.dto.enums; // O el paquete que elijas para enums de modelo

public enum FriendshipStatus {
    PENDING,  // Solicitud enviada, esperando respuesta
    ACCEPTED, // Solicitud aceptada, son amigos
    DECLINED, // Solicitud rechazada
    BLOCKED   // Un usuario ha bloqueado al otro (podría ser una mejora futura, por ahora nos centramos en PENDING y ACCEPTED)
    // REMOVED // Opcional, si quieres marcar explícitamente una amistad eliminada en lugar de borrar la fila.
    // Por simplicidad, borrar la fila para una amistad aceptada es más directo para "eliminar amigo".
}
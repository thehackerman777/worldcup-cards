# API REST

## Base URL
```
http://<server>:8080/api/v1
```

## Autenticación

### Registrar
```http
POST /auth/register
Content-Type: application/json

{
  "username": "fanatico",
  "email": "fan@example.com",
  "password": "secure123",
  "displayName": "Fanático del Fútbol"
}
```

### Iniciar Sesión
```http
POST /auth/login
Content-Type: application/json

{
  "username": "fanatico",
  "password": "secure123"
}
```

### Refresh Token
```http
POST /auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJ..."
}
```

## Cartas

### Listar catálogo
```http
GET /cards?page=0&size=50&team=Brasil
Authorization: Bearer <token>
```

### Detalle
```http
GET /cards/{id}
Authorization: Bearer <token>
```

## Álbum

### Obtener álbum
```http
GET /album
Authorization: Bearer <token>
```

### Agregar carta
```http
POST /album/cards
Authorization: Bearer <token>
Content-Type: application/json

{
  "cardId": "uuid",
  "quantity": 1,
  "isInAlbum": true
}
```

### Repetidas
```http
GET /album/repeated
Authorization: Bearer <token>
```

### Eliminar carta del álbum
```http
DELETE /album/cards/{userCardId}
Authorization: Bearer <token>
```

## Intercambios

### Crear solicitud
```http
POST /exchanges
Authorization: Bearer <token>
Content-Type: application/json

{
  "receiverId": "uuid",
  "message": "Te cambio esta...",
  "offeredCards": [{"cardId": "uuid", "quantity": 1}],
  "requestedCards": [{"cardId": "uuid", "quantity": 1}]
}
```

### Listar mis intercambios
```http
GET /exchanges
Authorization: Bearer <token>
```

### Intercambios disponibles
```http
GET /exchanges/available
Authorization: Bearer <token>
```

### Aceptar
```http
PUT /exchanges/{id}/accept
Authorization: Bearer <token>
```

### Rechazar
```http
PUT /exchanges/{id}/reject
Authorization: Bearer <token>
```

### Completar
```http
PUT /exchanges/{id}/complete
Authorization: Bearer <token>
```

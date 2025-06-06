# GameControllerApi

All URIs are relative to *http://localhost:8080*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**buscarJuegosEnIgdb**](#buscarjuegosenigdb) | **GET** /api/juegos/igdb/buscar | Buscar juegos en IGDB por nombre|
|[**filtrarJuegosEnIgdb**](#filtrarjuegosenigdb) | **GET** /api/juegos/igdb/filtrar | Filtrar juegos en IGDB por múltiples criterios|

# **buscarJuegosEnIgdb**
> Array<GameDto> buscarJuegosEnIgdb()

Realiza una búsqueda de juegos en la base de datos de IGDB utilizando un término de búsqueda para el nombre. Devuelve un flujo (o lista) de juegos que coinciden, con un conjunto limitado de campos (nombre, calificación, carátula, fecha de lanzamiento, tipo, resumen, ID). Este endpoint es público.

### Example

```typescript
import {
    GameControllerApi,
    Configuration
} from '@mycheckpoint/api-client';

const configuration = new Configuration();
const apiInstance = new GameControllerApi(configuration);

let nombre: string; //Término de búsqueda para el nombre del juego. (default to undefined)

const { status, data } = await apiInstance.buscarJuegosEnIgdb(
    nombre
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **nombre** | [**string**] | Término de búsqueda para el nombre del juego. | defaults to undefined|


### Return type

**Array<GameDto>**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**400** | Solicitud incorrecta. El parámetro \&#39;nombre\&#39; es obligatorio. |  -  |
|**500** | Error interno del servidor o error al comunicarse con la API de IGDB. |  -  |
|**200** | Búsqueda exitosa. Devuelve una lista de juegos encontrados (puede estar vacía). |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **filtrarJuegosEnIgdb**
> Array<GameDto> filtrarJuegosEnIgdb()

Permite buscar juegos en IGDB aplicando filtros opcionales como rango de fechas de lanzamiento, ID de género, ID de tema, y ID de modo de juego. Devuelve un flujo (o lista) de juegos que coinciden, con un conjunto limitado de campos. Este endpoint es público.

### Example

```typescript
import {
    GameControllerApi,
    Configuration
} from '@mycheckpoint/api-client';

const configuration = new Configuration();
const apiInstance = new GameControllerApi(configuration);

let fechaInicio: number; //Fecha de inicio del rango de lanzamiento (timestamp Unix en segundos). Opcional. (optional) (default to undefined)
let fechaFin: number; //Fecha de fin del rango de lanzamiento (timestamp Unix en segundos). Opcional. (optional) (default to undefined)
let idGenero: number; //ID del género según IGDB para filtrar. Opcional. (optional) (default to undefined)
let idTema: number; //ID del tema según IGDB para filtrar. Opcional. (optional) (default to undefined)
let idModoJuego: number; //ID del modo de juego según IGDB para filtrar. Opcional. (optional) (default to undefined)
let limite: number; //Número máximo de resultados a devolver. Opcional. Valor por defecto es 10, máximo 500. (optional) (default to 10)

const { status, data } = await apiInstance.filtrarJuegosEnIgdb(
    fechaInicio,
    fechaFin,
    idGenero,
    idTema,
    idModoJuego,
    limite
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **fechaInicio** | [**number**] | Fecha de inicio del rango de lanzamiento (timestamp Unix en segundos). Opcional. | (optional) defaults to undefined|
| **fechaFin** | [**number**] | Fecha de fin del rango de lanzamiento (timestamp Unix en segundos). Opcional. | (optional) defaults to undefined|
| **idGenero** | [**number**] | ID del género según IGDB para filtrar. Opcional. | (optional) defaults to undefined|
| **idTema** | [**number**] | ID del tema según IGDB para filtrar. Opcional. | (optional) defaults to undefined|
| **idModoJuego** | [**number**] | ID del modo de juego según IGDB para filtrar. Opcional. | (optional) defaults to undefined|
| **limite** | [**number**] | Número máximo de resultados a devolver. Opcional. Valor por defecto es 10, máximo 500. | (optional) defaults to 10|


### Return type

**Array<GameDto>**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**500** | Error interno del servidor o error al comunicarse con la API de IGDB. |  -  |
|**400** | Solicitud incorrecta. Ocurre si alguno de los parámetros numéricos no puede ser parseado correctamente (ej. texto en lugar de número). |  -  |
|**200** | Búsqueda por filtros exitosa. Devuelve una lista de juegos encontrados (puede estar vacía). |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)


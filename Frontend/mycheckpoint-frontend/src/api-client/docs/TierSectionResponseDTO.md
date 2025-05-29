# TierSectionResponseDTO

Sección especial para ítems (juegos) que aún no han sido clasificados en ninguna tier. Contiene el nombre \'Juegos por Clasificar\' y orden 0.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**internal_id** | **number** |  | [optional] [default to undefined]
**name** | **string** |  | [optional] [default to undefined]
**order** | **number** |  | [optional] [default to undefined]
**is_default_unclassified** | **boolean** |  | [optional] [default to undefined]
**items** | [**Array&lt;TierListItemGameInfoDTO&gt;**](TierListItemGameInfoDTO.md) |  | [optional] [default to undefined]

## Example

```typescript
import { TierSectionResponseDTO } from './api';

const instance: TierSectionResponseDTO = {
    internal_id,
    name,
    order,
    is_default_unclassified,
    items,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)

# PublicGameCommentDTO

Lista de comentarios públicos realizados por otros usuarios sobre este juego. Puede estar vacía si no hay comentarios o si el juego no existe en la base de datos local para asociar comentarios.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**username** | **string** |  | [optional] [default to undefined]
**user_public_id** | **string** |  | [optional] [default to undefined]
**comment_text** | **string** |  | [optional] [default to undefined]
**comment_date** | **string** |  | [optional] [default to undefined]

## Example

```typescript
import { PublicGameCommentDTO } from './api';

const instance: PublicGameCommentDTO = {
    username,
    user_public_id,
    comment_text,
    comment_date,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)

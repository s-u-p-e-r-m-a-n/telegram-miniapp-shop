import type {
    ApiError,
    ValidationApiError
} from '../types/ApiError';

export async function getApiErrorMessage(
    response: Response
): Promise<string> {

    try {
        const data =
            await response.json() as ApiError | ValidationApiError;
//ловим ошибки валидации
        if ('fieldErrors' in data) {
            return Object.values(data.fieldErrors).join(', ');
        }
//ловим ошибки сообщения об ошибке
        if ('message' in data && data.message) {
            return data.message;
        }

    } catch {
        // Ответ backend не удалось разобрать как JSON.
    }

    switch (response.status) {
        case 401:
            return 'Не удалось подтвердить пользователя Telegram';

        case 404:
            return 'Запрашиваемые данные не найдены';

        case 500:
            return 'Ошибка сервера. Попробуйте позже';

        default:
            return 'Не удалось выполнить операцию';
    }
}
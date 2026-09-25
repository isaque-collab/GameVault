import apiFetch from './http.ts'

export function avaliarJogo(
    rawgGameId: number,
    nota: number,
): Promise<unknown> {
    return apiFetch(
        `/usuarios/me/avaliacoes/${rawgGameId}`,
        {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                nota,
            }),
        },
    )
}

export function removerAvaliacao(
    rawgGameId: number,
): Promise<void> {
    return apiFetch(
        `/usuarios/me/avaliacoes/${rawgGameId}`,
        {
            method: 'DELETE',
        },
    )
}
import apiFetch from './http.ts';
import type { ItemColecaoJogo } from '../types/jogo.ts';

export function adicionarFavorito(
    rawgGameId: number,
): Promise<unknown> {
    return apiFetch('/usuarios/me/favoritos', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            rawgGameId,
        }),
    })
}

export function removerFavorito(
    rawgGameId: number,
): Promise<void> {
    return apiFetch(
        `/usuarios/me/favoritos/${rawgGameId}`,
        {
            method: 'DELETE',
        },
    )
}

export function adicionarListaDesejos(
    rawgGameId: number,
): Promise<unknown> {
    return apiFetch('/usuarios/me/lista-desejos', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            rawgGameId,
        }),
    })
}

export function removerListaDesejos(
    rawgGameId: number,
): Promise<void> {
    return apiFetch(
        `/usuarios/me/lista-desejos/${rawgGameId}`,
        {
            method: 'DELETE',
        },
    )
}

export function listarFavoritos(): Promise<ItemColecaoJogo[]> {
    return apiFetch<ItemColecaoJogo[]>(
        '/usuarios/me/favoritos',
    )
}

export function listarListaDesejos(): Promise<ItemColecaoJogo[]> {
    return apiFetch<ItemColecaoJogo[]>(
        '/usuarios/me/lista-desejos',
    )
}

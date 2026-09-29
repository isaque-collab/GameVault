import apiFetch from './http.ts'
import type { Usuario } from '../types/usuario'

export type AtualizarPerfilDados = {
    nome: string
    username: string
    email: string
}

export async function buscarUsuarioAtual(): Promise<Usuario | null> {
    const resposta = await fetch('/api/usuarios/me', {
        credentials: 'include',
    })

    if (resposta.status === 401) {
        return null
    }

    if (!resposta.ok) {
        throw new Error(
            `Não foi possível buscar o usuário atual: ${resposta.status}`,
        )
    }

    return resposta.json() as Promise<Usuario>
}

export async function atualizarPerfil(
    dados: AtualizarPerfilDados,
): Promise<Usuario> {
    return apiFetch<Usuario>('/usuarios/me', {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(dados),
    })
}

export async function atualizarFotoPerfil(
    foto: File,
): Promise<Usuario> {
    const dados = new FormData()

    dados.append('foto', foto)

    return apiFetch<Usuario>('/usuarios/me/foto', {
        method: 'PUT',
        body: dados,
    })
}

export async function removerFotoPerfil(): Promise<void> {
    return apiFetch<void>('/usuarios/me/foto', {
        method: 'DELETE',
    })
}
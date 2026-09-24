const API_BASE_URL = '/api'

async function apiFetch<T>(
    caminho: string,
    opcoes: RequestInit = {},
): Promise<T> {
    const resposta = await fetch(`${API_BASE_URL}${caminho}`, {
        ...opcoes,
        credentials: 'include',
    })

    if (!resposta.ok) {
        throw new Error(
            `Erro ao acessar a API: ${resposta.status} ${resposta.statusText}`,
        )
    }

    if (resposta.status === 204) {
        return undefined as T
    }

    return resposta.json() as Promise<T>
}

export default apiFetch
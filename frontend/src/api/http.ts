const API_BASE_URL = '/api'

function obterCookie(nome: string): string | null {
    const prefixo = `${nome}=`

    const cookie = document.cookie
        .split('; ')
        .find((item) => item.startsWith(prefixo))

    if (!cookie) {
        return null
    }

    return decodeURIComponent(cookie.substring(prefixo.length))
}

function metodoAlteraEstado(metodo?: string): boolean {
    const metodoNormalizado = (metodo ?? 'GET').toUpperCase()

    return !['GET', 'HEAD', 'OPTIONS'].includes(metodoNormalizado)
}

async function apiFetch<T>(
    caminho: string,
    opcoes: RequestInit = {},
): Promise<T> {
    const headers = new Headers(opcoes.headers)

    if (metodoAlteraEstado(opcoes.method)) {
        const csrfToken = obterCookie('XSRF-TOKEN')

        if (csrfToken) {
            headers.set('X-XSRF-TOKEN', csrfToken)
        }
    }

    const resposta = await fetch(`${API_BASE_URL}${caminho}`, {
        ...opcoes,
        headers,
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
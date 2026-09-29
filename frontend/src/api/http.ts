const API_BASE_URL = '/api'

type ProblemaApi = {
    detail?: unknown
}

export class ApiError extends Error {
    status: number
    detail?: string

    constructor(status: number, statusText?: string, detail?: string) {
        super(detail ??
            `Erro ao acessar a API: ${status} ${statusText}`,
        )

        this.name = 'ApiError'
        this.status = status
        this.detail = detail
    }
}

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

async function obterDetalheErro(
    resposta: Response,
): Promise<string | undefined> {
    try {
        const problema =
            await resposta.json() as ProblemaApi

        if (typeof problema.detail === 'string') {
            return problema.detail
        }

        return undefined
    } catch {
        return undefined
    }
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
        const detalhe = await obterDetalheErro(resposta)

        throw new ApiError(
            resposta.status,
            resposta.statusText,
            detalhe,
        )
    }

    if (resposta.status === 204) {
        return undefined as T
    }

    return resposta.json() as Promise<T>
}

export default apiFetch